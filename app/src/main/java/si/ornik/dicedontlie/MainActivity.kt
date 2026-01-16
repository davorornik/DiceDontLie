package si.ornik.dicedontlie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import si.ornik.dicedontlie.data.DatabaseProvider
import si.ornik.dicedontlie.ui.screens.DieRollScreen
import si.ornik.dicedontlie.ui.screens.GameDetails
import si.ornik.dicedontlie.ui.screens.NewGameScreen
import si.ornik.dicedontlie.ui.screens.RollLogScreen
import si.ornik.dicedontlie.ui.screens.StartScreen
import si.ornik.dicedontlie.ui.screens.StatisticsScreen
import si.ornik.dicedontlie.ui.theme.DiceDontLieTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize the database provider
        DatabaseProvider.initialize(this)


        setContent {
            DiceDontLieTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(R.string.app_name)) }
                        )
                    },
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "start",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("start") {
                            StartScreen(navController = navController)
                        }
                        composable("new_game") {
                            NewGameScreen(
                                onGameCreated = { _ ->
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(
                            "details/{gameId}",
                            arguments = listOf(navArgument("gameId") {
                                type = NavType.StringType
                            })
                        ) { backStackEntry ->
                            val gameId =
                                backStackEntry.arguments?.getString("gameId") ?: return@composable
                            GameDetails(gameId = gameId, navController = navController)
                        }
                        composable(
                            "counting/{gameId}",
                            arguments = listOf(navArgument("gameId") {
                                type = NavType.StringType
                            })
                        ) { backStackEntry ->
                            val gameId =
                                backStackEntry.arguments?.getString("gameId") ?: return@composable
                            DieRollScreen(gameId = gameId)
                        }
                        composable(
                            "statistics/{gameId}",
                            arguments = listOf(navArgument("gameId") {
                                type = NavType.StringType
                            })
                        ) { backStackEntry ->
                            val gameId =
                                backStackEntry.arguments?.getString("gameId") ?: return@composable
                            StatisticsScreen(gameId = gameId)
                        }
                        composable(
                            "rolllog/{gameId}",
                            arguments = listOf(navArgument("gameId") {
                                type = NavType.StringType
                            })
                        ) { backStackEntry ->
                            val gameId =
                                backStackEntry.arguments?.getString("gameId") ?: return@composable
                            RollLogScreen(gameId = gameId)
                        }
                    }
                }
            }
        }
    }
}