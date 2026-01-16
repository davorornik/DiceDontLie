package si.ornik.dicedontlie.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import si.ornik.dicedontlie.R
import androidx.navigation.NavController
import si.ornik.dicedontlie.data.DatabaseProvider
import si.ornik.dicedontlie.data.GameEntity
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun StartScreen(navController: NavController, modifier: Modifier = Modifier) {
    val games by DatabaseProvider.getGameDao().getAllGames().collectAsState(initial = emptyList())
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (games.isEmpty()) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(R.string.no_games_to_display))
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(games, key = { it.id }) { game ->
                    GameListItem(game = game, onClick = { navController.navigate("details/${game.id}") })
                }
            }
        }
        Button(
            onClick = { navController.navigate("new_game") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.new_game),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun GameListItem(game: GameEntity, onClick: () -> Unit) {
    val formatString = stringResource(R.string.date_time_format)
    val dateFormat = remember(formatString) { SimpleDateFormat(formatString, Locale.getDefault()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(game.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = dateFormat.format(game.startTime),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(pluralStringResource(R.plurals.players_count, game.players.size, game.players.size))
    }
}
