package com.dungeonescape

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.dungeonescape.engine.GameEngine
import com.dungeonescape.engine.GameLoop
import com.dungeonescape.models.ScreenState
import com.dungeonescape.ui.GameOverScreen
import com.dungeonescape.ui.GameScreen
import com.dungeonescape.ui.MainMenuScreen
import com.dungeonescape.ui.theme.DungeonDark
import com.dungeonescape.ui.theme.DungeonEscapeTheme

class MainActivity : ComponentActivity() {

    private lateinit var gameEngine: GameEngine
    private lateinit var gameLoop: GameLoop

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        gameEngine = GameEngine(applicationContext)
        gameLoop = GameLoop(gameEngine)

        setContent {
            DungeonEscapeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DungeonDark
                ) {
                    val gameState by gameEngine.gameStateFlow.collectAsState()

                    when (gameState.screenState) {
                        ScreenState.MAIN_MENU, ScreenState.INSTRUCTIONS -> {
                            MainMenuScreen(
                                stateManager = gameEngine.stateManager,
                                onResumeGame = {
                                    gameEngine.resumeSavedGame()
                                    gameLoop.start()
                                },
                                onStartNewGame = {
                                    gameEngine.startNewGame()
                                    gameLoop.start()
                                },
                                onToggleAudio = {
                                    gameEngine.audioManager.toggleMute()
                                },
                                onExit = {
                                    finish()
                                }
                            )
                        }
                        ScreenState.IN_GAME, ScreenState.LEVEL_CLEARED -> {
                            GameScreen(
                                gameState = gameState,
                                gameEngine = gameEngine,
                                onMainMenu = {
                                    gameEngine.setScreen(ScreenState.MAIN_MENU)
                                }
                            )
                        }
                        ScreenState.GAME_OVER -> {
                            GameOverScreen(
                                gameState = gameState,
                                stateManager = gameEngine.stateManager,
                                onRetry = {
                                    gameEngine.restartGame()
                                },
                                onMainMenu = {
                                    gameEngine.setScreen(ScreenState.MAIN_MENU)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (::gameLoop.isInitialized) {
            gameLoop.resume()
        }
    }

    override fun onStop() {
        super.onStop()
        if (::gameLoop.isInitialized) {
            gameLoop.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::gameLoop.isInitialized) {
            gameLoop.stop()
        }
        if (::gameEngine.isInitialized) {
            gameEngine.release()
        }
    }
}
