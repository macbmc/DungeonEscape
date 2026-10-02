package com.dungeonescape.engine

import android.content.Context
import com.dungeonescape.audio.AudioManager
import com.dungeonescape.audio.SoundType
import com.dungeonescape.entities.Coin
import com.dungeonescape.entities.EnemyVariant
import com.dungeonescape.entities.ExitPortal
import com.dungeonescape.entities.HealthPotion
import com.dungeonescape.entities.KeyItem
import com.dungeonescape.entities.MerchantAltar
import com.dungeonescape.entities.Player
import com.dungeonescape.entities.Skeleton
import com.dungeonescape.entities.TrapSpike
import com.dungeonescape.entities.TreasureChest
import com.dungeonescape.models.DungeonTheme
import com.dungeonescape.models.GameState
import com.dungeonescape.models.LevelState
import com.dungeonescape.models.ScreenState
import com.dungeonescape.models.ThemeMode
import com.dungeonescape.models.Tile
import com.dungeonescape.models.Vector2D
import com.dungeonescape.utils.Constants
import com.dungeonescape.utils.GameBalance
import com.dungeonescape.utils.VibrationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

class GameEngine(private val context: Context) {

    val audioManager = AudioManager(context)
    val stateManager = GameStateManager(context)
    val themeManager = ThemeManager()

    private val dungeonGenerator = DungeonGenerator()
    private val collisionSystem = CollisionSystem()
    private val cameraSystem = CameraSystem()
    private val lightingSystem = LightingSystem()
    private val particleSystem = ParticleSystem()

    private var currentLevel = 1
    private var totalScore = 0
    private var currentThemeMode = ThemeMode.PROGRESSIVE
    private var chosenLockedTheme = DungeonTheme.ANCIENT_RUINS

    private var player = Player()
    private var skeletons = mutableListOf<Skeleton>()
    private var coins = mutableListOf<Coin>()
    private var potions = mutableListOf<HealthPotion>()
    private var chests = mutableListOf<TreasureChest>()
    private var traps = mutableListOf<TrapSpike>()
    private var merchantAltar: MerchantAltar? = null
    private var keyItem: KeyItem? = null
    private var exitPortal: ExitPortal? = null
    private var tiles: Array<Array<Tile>> = emptyArray()

    private val pendingAttack = AtomicBoolean(false)
    private val pendingDash = AtomicBoolean(false)
    private val isTransitioningLevel = AtomicBoolean(false)
    private var moveInput = Vector2D.ZERO
    private var currentScreen = ScreenState.MAIN_MENU
    private var isPaused = false
    private var isMerchantDialogOpen = false
    private var healFeedbackText: String? = null
    private var healFeedbackTimer: Float = 0f

    private val _gameStateFlow = MutableStateFlow(GameState())
    val gameStateFlow: StateFlow<GameState> = _gameStateFlow.asStateFlow()

    init {
        applySettings()
    }

    fun applySettings() {
        audioManager.updateSettings(
            musicVol = stateManager.musicVolume,
            sfxVol = stateManager.sfxVolume,
            musicOn = stateManager.musicEnabled,
            sfxOn = stateManager.sfxEnabled
        )
    }

    fun startNewGame(
        themeMode: ThemeMode = ThemeMode.PROGRESSIVE,
        lockedTheme: DungeonTheme = DungeonTheme.ANCIENT_RUINS
    ) {
        applySettings()
        currentLevel = 1
        totalScore = 0
        currentThemeMode = themeMode
        chosenLockedTheme = lockedTheme
        player = Player(health = Constants.PLAYER_MAX_HEALTH)
        loadLevel(currentLevel)
        currentScreen = ScreenState.IN_GAME
        audioManager.setTheme(getCurrentTheme())
        audioManager.startBgm()
        saveSession()
        publishState()
    }

    fun resumeSavedGame() {
        applySettings()
        val saved = stateManager.loadSavedGameSession() ?: return
        currentLevel = saved.level
        totalScore = saved.score
        currentThemeMode = saved.themeMode
        chosenLockedTheme = saved.lockedTheme
        player = Player(health = saved.playerHealth, coinsCollected = saved.coins)
        loadLevel(currentLevel)
        currentScreen = ScreenState.IN_GAME
        audioManager.setTheme(getCurrentTheme())
        audioManager.startBgm()
        publishState()
    }

    fun nextLevel() {
        if (isTransitioningLevel.getAndSet(true)) return
        try {
            currentLevel++
            totalScore += Constants.LEVEL_COMPLETE_BONUS
            audioManager.playSound(SoundType.VICTORY)
            VibrationManager.vibrateVictory(context, stateManager.vibrationEnabled)

            // Fair level completion recovery: +10 HP (GameBalance.LEVEL_COMPLETE_HEAL)
            player.heal(GameBalance.LEVEL_COMPLETE_HEAL)
            healFeedbackText = "Dungeon cleared • +${GameBalance.LEVEL_COMPLETE_HEAL.toInt()} HP"
            healFeedbackTimer = 2.5f
            player.hasKey = false

            loadLevel(currentLevel)
            audioManager.setTheme(getCurrentTheme())
            saveSession()
            publishState()
        } finally {
            isTransitioningLevel.set(false)
        }
    }

    fun restartGame() {
        startNewGame(currentThemeMode, chosenLockedTheme)
    }

    fun restartCurrentLevel() {
        isPaused = false
        player = Player(health = Constants.PLAYER_MAX_HEALTH, coinsCollected = player.coinsCollected)
        loadLevel(currentLevel)
        audioManager.setTheme(getCurrentTheme())
        audioManager.startBgm()
        publishState()
    }

    fun setScreen(screen: ScreenState) {
        currentScreen = screen
        if (screen == ScreenState.MAIN_MENU || screen == ScreenState.GAME_OVER) {
            audioManager.stopBgm()
            if (screen == ScreenState.MAIN_MENU && !player.isDead && currentLevel > 0) {
                saveSession()
            }
        } else if (screen == ScreenState.IN_GAME) {
            audioManager.setTheme(getCurrentTheme())
            audioManager.startBgm()
        }
        publishState()
    }

    fun pauseGame() {
        isPaused = true
        saveSession()
        publishState()
    }

    fun resumeGame() {
        isPaused = false
        publishState()
    }

    fun openMerchantDialog() {
        isMerchantDialogOpen = true
        publishState()
    }

    fun closeMerchantDialog() {
        isMerchantDialogOpen = false
        publishState()
    }

    fun buyPotion(cost: Int, healAmount: Float): Boolean {
        if (player.health < player.maxHealth && player.coinsCollected >= cost) {
            player.coinsCollected -= cost
            val actualHeal = min(healAmount, player.maxHealth - player.health)
            player.heal(actualHeal)
            healFeedbackText = "+${actualHeal.toInt()} HP"
            healFeedbackTimer = 2.0f
            audioManager.playSound(SoundType.POTION_PURCHASE)
            VibrationManager.vibrateLight(context, stateManager.vibrationEnabled)
            particleSystem.spawnHealEffect(player.position)
            publishState()
            return true
        }
        return false
    }

    fun handleJoystickInput(direction: Vector2D) {
        moveInput = direction
    }

    fun handleAttack() {
        if (currentScreen == ScreenState.IN_GAME && !isPaused && !player.isDead) {
            pendingAttack.set(true)
        }
    }

    fun handleDash() {
        if (currentScreen == ScreenState.IN_GAME && !isPaused && !player.isDead) {
            pendingDash.set(true)
        }
    }

    private fun getCurrentTheme(): DungeonTheme {
        return themeManager.getThemeForLevel(currentLevel, currentThemeMode, chosenLockedTheme)
    }

    private fun saveSession() {
        if (!player.isDead && currentLevel >= 1) {
            stateManager.saveGameSession(
                level = currentLevel,
                score = totalScore,
                playerHealth = player.health,
                coins = player.coinsCollected,
                themeMode = currentThemeMode,
                lockedTheme = chosenLockedTheme
            )
        }
    }

    private fun executeAttack() {
        if (player.triggerAttack()) {
            audioManager.playSound(SoundType.ATTACK)

            // Check Attack on Skeletons
            collisionSystem.checkPlayerAttack(player, skeletons) { skeleton, damage ->
                val tookDmg = skeleton.takeDamage(damage)
                if (tookDmg) {
                    audioManager.playSound(SoundType.ENEMY_HIT)
                    particleSystem.spawnPlayerHit(skeleton.position)

                    if (!skeleton.isAlive) {
                        val isElite = skeleton.variant != EnemyVariant.NORMAL
                        val bonusCoins = skeleton.bonusCoinDrop
                        val scoreGain = if (isElite) 50 else 25
                        totalScore += scoreGain

                        if (bonusCoins > 0) {
                            player.coinsCollected += bonusCoins
                            particleSystem.spawnCoinPickup(skeleton.position)
                        }

                        particleSystem.spawnEnemyDeath(skeleton.position, isElite)
                    }
                }
            }

            // Check Attack on Secret Breakable Walls
            collisionSystem.checkSecretWallAttack(player, tiles) { tile, isBroken ->
                val tileCenter = Vector2D(tile.x + 0.5f, tile.y + 0.5f)
                if (isBroken) {
                    audioManager.playSound(SoundType.SECRET_WALL_BREAK)
                    VibrationManager.vibrateLight(context, stateManager.vibrationEnabled)
                    particleSystem.spawnWallHitDebris(tileCenter, isBroken = true)
                    lightingSystem.updateLighting(player.position, tiles)
                } else {
                    audioManager.playSound(SoundType.SECRET_WALL_HIT)
                    particleSystem.spawnWallHitDebris(tileCenter, isBroken = false)
                }
            }
        }
    }

    private fun executeDash() {
        val dashDir = if (moveInput.lengthSquared() > 0.01f) moveInput.normalized() else player.facingDirection
        if (player.triggerDash(dashDir)) {
            audioManager.playSound(SoundType.DASH)
            particleSystem.spawnDashTrail(player.position, dashDir)
        }
    }

    private fun loadLevel(level: Int) {
        particleSystem.clear()
        val dungeonData = dungeonGenerator.generateDungeon(level, player.health)
        tiles = dungeonData.tiles
        player.position = dungeonData.playerSpawn
        player.velocity = Vector2D.ZERO
        player.hasKey = false
        keyItem = dungeonData.key
        exitPortal = dungeonData.portal
        coins = dungeonData.coins.toMutableList()
        potions = dungeonData.potions.toMutableList()
        chests = dungeonData.chests.toMutableList()
        traps = dungeonData.traps.toMutableList()
        merchantAltar = dungeonData.merchantAltar
        skeletons = dungeonData.skeletons.toMutableList()

        cameraSystem.reset(player.position)
        lightingSystem.updateLighting(player.position, tiles)
    }

    fun update(deltaTime: Float) {
        if (currentScreen != ScreenState.IN_GAME || isPaused) return

        val clampedDelta = min(deltaTime, Constants.MAX_DELTA_TIME)

        // Update heal feedback timer
        if (healFeedbackTimer > 0f) {
            healFeedbackTimer -= clampedDelta
            if (healFeedbackTimer <= 0f) {
                healFeedbackText = null
            }
        }

        if (moveInput.lengthSquared() > 0.01f) {
            player.facingDirection = moveInput.normalized()
            player.isMoving = true
        } else {
            player.isMoving = false
        }

        if (pendingAttack.getAndSet(false)) {
            executeAttack()
        }

        if (pendingDash.getAndSet(false)) {
            executeDash()
        }

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

        // 2. Update Enemies AI, Separation & Physics
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

        collisionSystem.resolveEnemySeparation(skeletons)

        // 3. Update Pickups, Chests, Traps, Altar, Potions
        coins.forEach { it.update(clampedDelta) }
        potions.forEach { it.update(clampedDelta) }
        chests.forEach { it.update(clampedDelta) }
        traps.forEach { it.update(clampedDelta) }
        merchantAltar?.update(clampedDelta)
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

        // Potion Pickups (Only consumed if player is injured)
        collisionSystem.checkPotionPickups(player, potions) { collectedPotion ->
            val actualHeal = min(collectedPotion.healAmount, player.maxHealth - player.health)
            player.heal(actualHeal)
            audioManager.playSound(SoundType.POTION_PURCHASE)
            VibrationManager.vibrateLight(context, stateManager.vibrationEnabled)
            particleSystem.spawnHealEffect(collectedPotion.position)
            healFeedbackText = "+${actualHeal.toInt()} HP"
            healFeedbackTimer = 2.0f
        }

        // Chest Interactions
        collisionSystem.checkChestInteraction(player.position, chests) { chest, coinsReward ->
            totalScore += coinsReward * 2
            player.coinsCollected += coinsReward
            audioManager.playSound(SoundType.CHEST_OPEN)
            VibrationManager.vibrateLight(context, stateManager.vibrationEnabled)
            particleSystem.spawnChestOpen(chest.position)
        }

        // Trap Collisions
        collisionSystem.checkTrapCollisions(player, traps) { trap, dmg ->
            val damaged = player.takeDamage(dmg)
            if (damaged) {
                audioManager.playSound(SoundType.TRAP_HIT)
                VibrationManager.vibrateImpact(context, stateManager.vibrationEnabled)
                particleSystem.spawnPlayerHit(player.position)
                if (player.isDead) {
                    onPlayerDeath()
                }
            }
        }

        // Key Pickup
        collisionSystem.checkKeyPickup(player.position, keyItem) {
            player.hasKey = true
            exitPortal?.isActive = true
            audioManager.playSound(SoundType.KEY_PICKUP)
            VibrationManager.vibrateLight(context, stateManager.vibrationEnabled)
            exitPortal?.let {
                particleSystem.spawnPortalActivation(it.position)
            }
        }

        // Enemy Contact Damage
        collisionSystem.checkEnemyPlayerContact(player, skeletons) { damage ->
            val damaged = player.takeDamage(damage)
            if (damaged) {
                audioManager.playSound(SoundType.PLAYER_DAMAGE)
                VibrationManager.vibrateImpact(context, stateManager.vibrationEnabled)
                particleSystem.spawnPlayerHit(player.position)
                if (player.isDead) {
                    onPlayerDeath()
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

    private fun onPlayerDeath() {
        audioManager.playSound(SoundType.GAME_OVER)
        VibrationManager.vibrateImpact(context, stateManager.vibrationEnabled)
        stateManager.recordScore(totalScore, currentLevel, player.coinsCollected)
        currentScreen = ScreenState.GAME_OVER
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

        val isNearMerchant = collisionSystem.checkMerchantProximity(player.position, merchantAltar)

        _gameStateFlow.value = GameState(
            screenState = currentScreen,
            player = player.copy(),
            skeletons = skeletons.map { it.copy() },
            coins = coins.map { it.copy() },
            potions = potions.map { it.copy() },
            chests = chests.map { it.copy() },
            merchantAltar = merchantAltar?.copy(),
            traps = traps.map { it.copy() },
            key = keyItem?.copy(),
            portal = exitPortal?.copy(),
            tiles = tiles,
            particles = particleSystem.getActiveParticles().map { it.copy() },
            levelState = levelState,
            score = totalScore,
            cameraPosition = cameraSystem.position,
            isPaused = isPaused,
            theme = getCurrentTheme(),
            isMerchantNear = isNearMerchant,
            isMerchantDialogOpen = isMerchantDialogOpen,
            healFeedbackText = healFeedbackText
        )
    }

    fun release() {
        audioManager.release()
    }
}
