package com.dorino.game.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dorino.game.data.model.GameStatus
import com.dorino.game.ui.GameViewModel
import com.dorino.game.ui.gameoptions.GameOptionsScreen
import com.dorino.game.ui.gameplay.GamePlayScreen
import com.dorino.game.ui.history.HistoryScreen
import com.dorino.game.ui.home.HomeScreen
import com.dorino.game.ui.modeselect.ModeSelectScreen
import com.dorino.game.ui.playercount.PlayerCountScreen
import com.dorino.game.ui.playernames.PlayerNamesScreen
import com.dorino.game.ui.roundresult.GameResultScreen
import com.dorino.game.ui.seating.SeatingScreen
import com.dorino.game.ui.settings.SettingsScreen
import com.dorino.game.ui.turntransition.TurnTransitionScreen
import com.dorino.game.ui.tutorial.TutorialScreen

@Composable
fun DorinoNavHost(viewModel: GameViewModel) {
    val navController: NavHostController = rememberNavController()

    val settings by viewModel.settings.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val setupDraft by viewModel.setupDraft.collectAsState()
    val history by viewModel.history.collectAsState()
    val lastResult by viewModel.lastResult.collectAsState()
    val passCooldownRemaining by viewModel.passCooldownRemaining.collectAsState()

    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                hasSavedGame = gameState != null && gameState?.status != GameStatus.FINISHED,
                onStartGame = { navController.navigate(Routes.MODE_SELECT) },
                onContinueGame = {
                    viewModel.prepareResume()
                    navController.navigate(Routes.TURN_TRANSITION)
                },
                onTutorial = { navController.navigate(Routes.TUTORIAL) },
                onHistory = { navController.navigate(Routes.HISTORY) },
                onSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.MODE_SELECT) {
            ModeSelectScreen(
                onModeSelected = { mode ->
                    viewModel.selectMode(mode)
                    navController.navigate(Routes.GAME_OPTIONS)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.GAME_OPTIONS) {
            GameOptionsScreen(
                settings = settings,
                onUpdate = viewModel::updateSettings,
                onContinue = { navController.navigate(Routes.PLAYER_COUNT) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PLAYER_COUNT) {
            PlayerCountScreen(
                initialCount = setupDraft.playerCount,
                onConfirm = { count ->
                    viewModel.setPlayerCount(count)
                    navController.navigate(Routes.PLAYER_NAMES)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PLAYER_NAMES) {
            PlayerNamesScreen(
                playerCount = setupDraft.playerCount,
                onConfirm = { names ->
                    viewModel.setPlayerNames(names)
                    viewModel.startNewGame()
                    navController.navigate(Routes.SEATING) {
                        popUpTo(Routes.HOME)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SEATING) {
            val state = gameState
            if (state == null) {
                LaunchedEffect(Unit) { navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } } }
            } else {
                SeatingScreen(
                    state = state,
                    onContinue = {
                        navController.navigate(Routes.TURN_TRANSITION) {
                            popUpTo(Routes.SEATING) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Routes.TURN_TRANSITION) {
            val state = gameState
            if (state == null) {
                LaunchedEffect(Unit) { navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } } }
            } else {
                TurnTransitionScreen(
                    mode = state.mode,
                    currentPlayer = state.currentPlayer,
                    currentTeam = state.currentTeam,
                    round = state.round,
                    totalRounds = state.totalRounds,
                    privacyModeEnabled = state.settings.privacyModeEnabled,
                    onReady = {
                        viewModel.confirmReadyStartTurn()
                        navController.navigate(Routes.GAMEPLAY) {
                            popUpTo(Routes.TURN_TRANSITION) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Routes.GAMEPLAY) {
            val state = gameState
            if (state == null) {
                LaunchedEffect(Unit) { navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } } }
            } else {
                GamePlayScreen(
                    state = state,
                    passCooldownRemaining = passCooldownRemaining,
                    onCorrect = viewModel::markCorrect,
                    onPass = viewModel::markPass,
                    onFinishTurnManually = viewModel::finishTurnManually
                )
                LaunchedEffect(state.status) {
                    when (state.status) {
                        GameStatus.TURN_TRANSITION -> navController.navigate(Routes.TURN_TRANSITION) {
                            popUpTo(Routes.GAMEPLAY) { inclusive = true }
                        }
                        GameStatus.FINISHED -> navController.navigate(Routes.GAME_RESULT) {
                            popUpTo(Routes.GAMEPLAY) { inclusive = true }
                        }
                        else -> {}
                    }
                }
            }
        }

        composable(Routes.GAME_RESULT) {
            val result = lastResult
            if (result == null) {
                LaunchedEffect(Unit) { navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } } }
            } else {
                GameResultScreen(
                    result = result,
                    onPlayAgain = {
                        viewModel.playAgainSamePlayers()
                        navController.navigate(Routes.TURN_TRANSITION) {
                            popUpTo(Routes.HOME)
                        }
                    },
                    onBackHome = {
                        viewModel.clearFinishedGame()
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                settings = settings,
                onUpdate = viewModel::updateSettings,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.TUTORIAL) {
            TutorialScreen(onFinish = { navController.popBackStack() })
        }

        composable(Routes.HISTORY) {
            HistoryScreen(history = history, onBack = { navController.popBackStack() })
        }
    }
}
