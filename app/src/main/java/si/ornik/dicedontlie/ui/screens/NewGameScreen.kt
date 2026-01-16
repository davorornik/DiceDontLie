package si.ornik.dicedontlie.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import si.ornik.dicedontlie.R
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import si.ornik.dicedontlie.data.DatabaseProvider
import si.ornik.dicedontlie.data.GameEntity
import java.util.Collections

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGameScreen(modifier: Modifier = Modifier, onGameCreated: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val players = remember { mutableStateListOf<String>() }
    var playerName by remember { mutableStateOf("") }
    var gameName by remember { mutableStateOf("") }
    var useEventDie by remember { mutableStateOf(false) }
    val maxPlayerNameLength = 25

    fun movePlayer(from: Int, to: Int) {
        val toIndex = to.coerceIn(0, players.size - 1)
        if (from != toIndex) {
            Collections.swap(players, from, toIndex)
        }
    }

    val playerAlreadyExistsError = stringResource(R.string.player_already_exists)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.create_new_game),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        TextField(
            value = gameName,
            onValueChange = { gameName = it },
            label = { Text(stringResource(R.string.game_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = playerName,
                onValueChange = {
                    if (it.length <= maxPlayerNameLength) {
                        playerName = it
                    }
                },
                label = { Text(stringResource(R.string.player_name_max_chars, maxPlayerNameLength)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                supportingText = { Text(stringResource(R.string.player_name_count, playerName.length, maxPlayerNameLength)) }
            )
            Button(
                onClick = {
                    if (playerName.isNotBlank()) {
                        val isDuplicatePlayer = players.any { it.equals(playerName, ignoreCase = true) }
                        if (isDuplicatePlayer) {
                            Toast.makeText(context, playerAlreadyExistsError, Toast.LENGTH_LONG).show()
                        } else {
                            players.add(playerName)
                            playerName = ""
                        }
                    }
                },
                enabled = players.size < 6 && playerName.isNotBlank(),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(stringResource(R.string.add))
            }
        }

        if (players.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.players),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn {
                    itemsIndexed(players, key = { _, player -> player }) { index, player ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { movePlayer(index, index - 1) },
                                enabled = index > 0
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.move_up))
                            }
                            IconButton(
                                onClick = { movePlayer(index, index + 1) },
                                enabled = index < players.size - 1
                            ) {
                                Icon(
                                    Icons.Default.ArrowDownward,
                                    contentDescription = stringResource(R.string.move_down)
                                )
                            }
                            Text(player, modifier = Modifier.padding(start = 8.dp))
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = { players.removeAt(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.remove_player))
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.add_2_to_6_players))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { useEventDie = !useEventDie },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Checkbox(
                checked = useEventDie,
                onCheckedChange = { useEventDie = it }
            )
            Text(stringResource(R.string.event_die_option))
        }

        Button(
            onClick = {
                scope.launch {
                    val gameEntity = GameEntity(
                        name = gameName,
                        players = players.toList(),
                        eventDieEnabled = useEventDie
                    )
                    DatabaseProvider.getGameDao().insertGame(gameEntity)
                    onGameCreated(gameEntity.id)
                }
            },
            enabled = players.size in 2..6 && gameName.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.start_game),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
