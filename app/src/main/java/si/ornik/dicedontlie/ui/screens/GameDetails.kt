package si.ornik.dicedontlie.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import si.ornik.dicedontlie.R
import si.ornik.dicedontlie.data.DatabaseProvider
import si.ornik.dicedontlie.data.GameEntity
import si.ornik.dicedontlie.data.toDieRoll
import si.ornik.dicedontlie.data.toGame

@Composable
fun GameDetails(gameId: String, navController: NavController, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val gameEntity by DatabaseProvider.getGameDao().getGameByIdFlow(gameId).collectAsState(initial = null)
    val rolls by DatabaseProvider.getDieRollDao().getRollsForGame(gameId).collectAsState(initial = emptyList())

    var isGameEnded by rememberSaveable { mutableStateOf(false) }
    var currentGame by remember { mutableStateOf<GameEntity?>(null) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    
    // Update state when gameEntity changes
    if (gameEntity != null && currentGame != gameEntity) {
        currentGame = gameEntity
        isGameEnded = gameEntity?.endTime != null
    }

    val game by remember { derivedStateOf { currentGame?.toGame(rolls.map { it.toDieRoll() }) } }
    
    if (game == null) {
        Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
            Text("Game not found")
        }
        return
    }

    // Calculate total rolls
    val totalRolls = rolls.size
    val scrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = game!!.name,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { showDeleteDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_game),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        Text(
            text = stringResource(R.string.started) + " " + SimpleDateFormat(stringResource(R.string.date_time_format), Locale.getDefault()).format(
                game!!.startTime),
            style = MaterialTheme.typography.bodyMedium,
        )
        if (currentGame?.endTime != null) {
            Text(
                text = stringResource(R.string.ended) + " " + SimpleDateFormat(stringResource(R.string.date_time_format), Locale.getDefault()).format(
                    game!!.endTime),
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            Text(
                text = stringResource(R.string.ended_in_progress),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text(
            text = stringResource(R.string.players) + " " + game!!.players.joinToString(", "),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(stringResource(R.string.total_rolls, totalRolls), style = MaterialTheme.typography.titleMedium)

        // End game checkbox
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = isGameEnded,
                onCheckedChange = { checked ->
                    isGameEnded = checked
                    scope.launch {
                        currentGame?.let { game ->
                            val updatedGame = if (checked) {
                                // Set end time to current time when checkbox is checked
                                game.copy(endTime = System.currentTimeMillis())
                            } else {
                                // Clear end time when checkbox is unchecked
                                game.copy(endTime = null)
                            }
                            DatabaseProvider.getGameDao().updateGame(updatedGame)
                        }
                    }
                },
            )
            Text(stringResource(R.string.end_game), style = MaterialTheme.typography.bodyMedium)
        }

        // Navigation to Die Roll Screen
        Button(
            onClick = { navController.navigate("counting/$gameId") },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.go_to_die_roll_screen),
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Navigation to Statistics Screen
        Button(
            onClick = { navController.navigate("statistics/$gameId") },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.view_statistics),
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Navigation to Roll Log Screen
        Button(
            onClick = { navController.navigate("rolllog/$gameId") },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.roll_log),
                style = MaterialTheme.typography.labelLarge
            )
        }
        // Delete confirmation dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(R.string.delete_game)) },
                text = { Text(stringResource(R.string.delete_game_confirmation)) },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                DatabaseProvider.getGameDao().deleteGameById(gameId)
                                navController.popBackStack()
                            }
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}
