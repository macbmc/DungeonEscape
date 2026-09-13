package com.dungeonescape.engine

import android.content.Context
import com.dungeonescape.audio.AudioManager
import com.dungeonescape.audio.SoundType
import com.dungeonescape.entities.Coin
import com.dungeonescape.entities.ExitPortal
import com.dungeonescape.entities.KeyItem
import com.dungeonescape.entities.Player
import com.dungeonescape.entities.Skeleton
import com.dungeonescape.models.GameState
import com.dungeonescape.models.LevelState
import com.dungeonescape.models.ScreenState
import com.dungeonescape.models.Tile
import com.dungeonescape.models.Vector2D
import com.dungeonescape.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.min

class GameEngine(private val context: Context) {

    val audioManager = AudioManager(context)
    val stateManager = GameStateManager(context)

    private val dungeonGenerator = DungeonGenerator()
    private val collisionSystem = CollisionSystem()
    private val cameraSystem = CameraSystem()
    private val lightingSystem = LightingSystem()
    private val particleSystem = ParticleSystem()

    private var currentLevel = 1
    private var totalScore = 0

    private var player = Player()
    private var skeletons = mutableListOf<Skeleton>()
    private var coins = mutableListOf<Coin>()
    private var keyItem: KeyItem? = null
    private var exitPortal: ExitPortal? = null
    private var tiles: Array<Array<Tile>> = emptyArray()

    private var moveInput = Vector2D.ZERO
    private var currentScreen = ScreenState.MAIN_MENU
    private var isPaused = false

    private val _gameStateFlow = MutableStateFlow(GameState())
    val gameStateFlow: StateFlow<GameState> = _gameStateFlow.asStateFlow()

    fun startNewGame() {
        currentLevel = 1
        totalScore = 0
        player = Player(health = Constants.PLAYER_MAX_HEALTH)
        loadLevel(currentLevel)
        currentScreen = ScreenState.IN_GAME
        audioManager.startBgm()
        publishState()
    }

    fun nextLevel() {
        currentLevel++
        totalScore += Constants.LEVEL_COMPLETE_BONUS
        audioManager.playSound(SoundType.VICTORY)
        
        // Heal player partially on clearing level
        player.heal(30f)
        player.hasKey = false
        
        loadLevel(currentLevel)
        publishState()
    }

    fun restartGame() {
        startNewGame()
    }

    fun setScreen(screen: ScreenState) {
        currentScreen = screen
        if (screen == ScreenState.MAIN_MENU || screen == ScreenState.GAME_OVER) {
            audioManager.stopBgm()
        } else if (screen == ScreenState.IN_GAME) {
            audioManager.startBgm()
        }
        publishState()
    }

    fun pauseGame() {
        isPaused = true
        publishState()
    }

    fun resumeGame() {
        isPaused = false
        publishState()
    }

    fun handleJoystickInput(direction: Vector2D) {
        moveInput = direction
        if (direction.lengthSquared() > 0.01f) {
            player.facingDirection = direction.normalized()
            player.isMoving = true
        } else {
            player.isMoving = false
        }
    }

    fun handleAttack() {
        if (currentScreen != ScreenState.IN_GAME || isPaused || player.isDead) return
        if (player.triggerAttack()) {
            audioManager.playSound(SoundType.ATTACK)
            
            // Immediately test attack collision against enemies
            collisionSystem.checkPlayerAttack(player, skeletons) { skeleton, damage ->
                val tookDmg = skeleton.takeDamage(damage)
                if (tookDmg) {
                    audioManager.playSound(SoundType.ENEMY_HIT)
                    particleSystem.spawnPlayerHit(skeleton.position)
                    
                    if (!skeleton.isAlive) {
                        totalScore += 25
                        particleSystem.spawnEnemyDeath(skeleton.position)
                    }
                }
            }
        }
    }

    fun handleDash() {
        if (currentScreen != ScreenState.IN_GAME || isPaused || player.isDead) return
        val dashDir = if (moveInput.lengthSquared() > 0.01f) moveInput.normalized() else player.facingDirection
        if (player.triggerDash(dashDir)) {
            audioManager.playSound(SoundType.DASH)
            particleSystem.spawnDashTrail(player.position, dashDir)
        }
    }

    private fun loadLevel(level: Int) {
        particleSystem.clear()
        val dungeonData = dungeonGenerator.generateDungeon(level)
        tiles = dungeonData.tiles
        player.position = dungeonData.playerSpawn
        player.velocity = Vector2D.ZERO
        player.hasKey = false
        keyItem = dungeonData.key
        exitPortal = dungeonData.portal
        coins = dungeonData.coins.toMutableList()
        skeletons = dungeonData.skeletons.toMutableList()

        cameraSystem.reset(player.position)
        lightingSystem.updateLighting(player.position, tiles)
    }

    fun update(deltaTime: Float) {
        if (currentScreen != ScreenState.IN_GAME || isPaused) return

        val clampedDelta = min(deltaTime, Constants.MAX_DELTA_TIME)

        // 1. Update Player Entity & Physics
        player.update(clampedDelta)

        if (player.isDashing) {
            player.velocity = player.dashDirection * (player.speed * Constants.DASH_SPEED_MULTIPLIER)
            particleSystem.spawnDashTrail(player.position, player.dashDirection)
        } else if (moveInput.lengthSquared() > 0.01f) {
            player.velocity = moveInput * player.speed
        } else {
            player.velocity = Vector2D.ZERO
        }

        player.position = collisionSystem.resolveEntityWallCollision(
            currentPos = player.position,
            velocity = player.velocity,
            deltaTime = clampedDelta,
            radius = Constants.PLAYER_COLLISION_RADIUS,
            tiles = tiles
        )

        // 2. Update Enemies AI & Physics
        val isTileWalkable = { tx: Int, ty: Int ->
            if (tx in 0 until Constants.DUNGEON_SIZE && ty in 0 until Constants.DUNGEON_SIZE) {
                tiles[ty][tx].isWalkable
            } else false
        }

        for (skeleton in skeletons) {
            if (!skeleton.isAlive) continue
            skeleton.updateAI(clampedDelta, player.position, isTileWalkable)
            
            skeleton.position = collisionSystem.resolveEntityWallCollision(
                currentPos = skeleton.position,
                velocity = skeleton.velocity,
                deltaTime = clampedDelta,
                radius = Constants.SKELETON_COLLISION_RADIUS,
                tiles = tiles
            )
        }

        // 3. Update Pickups & Objects
        coins.forEach { it.update(clampedDelta) }
        keyItem?.update(clampedDelta)
        exitPortal?.update(clampedDelta)

        // 4. Collisions
        // Coin Pickups
        collisionSystem.checkCoinPickups(player.position, coins) { collectedCoin ->
            totalScore += Constants.COIN_SCORE_VALUE
            player.coinsCollected++
            audioManager.playSound(SoundType.COIN_PICKUP)
            particleSystem.spawnCoinPickup(collectedCoin.position)
        }

        // Key Pickup
        collisionSystem.checkKeyPickup(player.position, keyItem) {
            player.hasKey = true
            exitPortal?.isActive = true
            audioManager.playSound(SoundType.KEY_PICKUP)
            exitPortal?.let {
                particleSystem.spawnPortalActivation(it.position)
            }
        }

        // Enemy Contact Damage
        collisionSystem.checkEnemyPlayerContact(player, skeletons) { damage ->
            val damaged = player.takeDamage(damage)
            if (damaged) {
                audioManager.playSound(SoundType.PLAYER_DAMAGE)
                particleSystem.spawnPlayerHit(player.position)
                
                if (player.isDead) {
                    audioManager.playSound(SoundType.GAME_OVER)
                    stateManager.recordScore(totalScore, currentLevel)
                    currentScreen = ScreenState.GAME_OVER
                }
            }
        }

        // Portal Entry -> Advance Level
        if (collisionSystem.checkPortalEntry(player.position, exitPortal)) {
            nextLevel()
            return
        }

        // 5. Update Particles
        particleSystem.update(clampedDelta)

        // 6. Camera Smooth Follow
        cameraSystem.update(player.position, clampedDelta)

        // 7. Dynamic Lighting & Fog of War
        lightingSystem.updateLighting(player.position, tiles)

        // 8. Emit State
        publishState()
    }

    private fun publishState() {
        val levelState = LevelState(
            levelNumber = currentLevel,
            dungeonSize = Constants.DUNGEON_SIZE,
            hasKey = player.hasKey,
            portalActive = exitPortal?.isActive == true,
            totalCoinsInLevel = coins.size,
            coinsCollectedInLevel = coins.count { it.isCollected },
            enemiesDefeatedInLevel = skeletons.count { !it.isAlive }
        )

        _gameStateFlow.value = GameState(
            screenState = currentScreen,
            player = player.copy(),
            skeletons = skeletons.map { it.copy() },
            coins = coins.map { it.copy() },
            key = keyItem?.copy(),
            portal = exitPortal?.copy(),
            tiles = tiles,
            particles = particleSystem.getActiveParticles().map { it.copy() },
            levelState = levelState,
            score = totalScore,
            cameraPosition = cameraSystem.position,
            isPaused = isPaused
        )
    }

    fun release() {
        audioManager.release()
    }
}
