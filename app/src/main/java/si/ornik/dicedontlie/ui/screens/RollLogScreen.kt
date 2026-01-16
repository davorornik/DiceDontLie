package si.ornik.dicedontlie.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import si.ornik.dicedontlie.R
import si.ornik.dicedontlie.data.DatabaseProvider
import si.ornik.dicedontlie.data.DieRollEntity
import si.ornik.dicedontlie.data.EventDie

@Composable
fun RollLogScreen(gameId: String, modifier: Modifier = Modifier) {
    val rolls by DatabaseProvider.getDieRollDao().getRollsForGame(gameId).collectAsState(initial = emptyList())

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = stringResource(R.string.roll_log),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (rolls.isEmpty()) {
            Text(
                text = stringResource(R.string.no_rolls_yet),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            LazyColumn {
                itemsIndexed(rolls, key = { _, roll -> roll.id }) { index, roll ->
                    RollLogItem(
                        roll = roll,
                        rollNumber = index + 1
                    )
                }
            }
        }
    }
}

@Composable
private fun RollLogItem(
    roll: DieRollEntity,
    rollNumber: Int
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Roll number with fixed width
        Box(modifier = Modifier.width(48.dp)) {
            Text(
                text = "$rollNumber.",
                style = MaterialTheme.typography.titleMedium,
            )
        }

        // Red die icon
        val redDieIcon = when (roll.redDie) {
            1 -> R.drawable.die_red_one
            2 -> R.drawable.die_red_two
            3 -> R.drawable.die_red_three
            4 -> R.drawable.die_red_four
            5 -> R.drawable.die_red_five
            6 -> R.drawable.die_red_six
            else -> R.drawable.die_red_one
        }
        Icon(
            painter = painterResource(id = redDieIcon),
            contentDescription = stringResource(R.string.red_die, roll.redDie),
            modifier = Modifier.size(64.dp),
            tint = Color.Unspecified
        )

        // Yellow die icon
        val yellowDieIcon = when (roll.yellowDie) {
            1 -> R.drawable.die_yellow_one
            2 -> R.drawable.die_yellow_two
            3 -> R.drawable.die_yellow_three
            4 -> R.drawable.die_yellow_four
            5 -> R.drawable.die_yellow_five
            6 -> R.drawable.die_yellow_six
            else -> R.drawable.die_yellow_one
        }
        Icon(
            painter = painterResource(id = yellowDieIcon),
            contentDescription = stringResource(R.string.yellow_die, roll.yellowDie),
            modifier = Modifier.size(64.dp).padding(start = 8.dp),
            tint = Color.Unspecified
        )

        if (roll.eventDie != null) {
            val eventDieIcon = when (roll.eventDie) {
                EventDie.POLITICS -> R.drawable.die_event_blue
                EventDie.SCIENCE -> R.drawable.die_event_green
                EventDie.TRADE -> R.drawable.die_event_yellow
                EventDie.PIRATES -> R.drawable.die_event_black
            }
            Icon(
                painter = painterResource(id = eventDieIcon),
                contentDescription = stringResource(R.string.event_die, roll.eventDie.name),
                modifier = Modifier.size(64.dp).padding(start = 8.dp),
                tint = Color.Unspecified
            )
        }

        // Spacer to push menu to the right
        Spacer(modifier = Modifier.weight(1f))

        // Cogwheel menu
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.more_options)
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.edit)) },
                    onClick = {
                        showMenu = false
                        showEditDialog = true
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.delete)) },
                    onClick = {
                        showMenu = false
                        showDeleteDialog = true
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    }
                )
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_roll)) },
            text = { Text(stringResource(R.string.delete_roll_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            DatabaseProvider.getDieRollDao().deleteDieRollById(roll.id)
                        }
                        showDeleteDialog = false
                    }
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

    // Edit dialog
    if (showEditDialog) {
        EditRollDialog(
            roll = roll,
            onDismiss = { showEditDialog = false },
            onSave = { newRedDie, newYellowDie, newEventDie ->
                CoroutineScope(Dispatchers.IO).launch {
                    DatabaseProvider.getDieRollDao().updateDieRoll(
                        roll.copy(
                            redDie = newRedDie,
                            yellowDie = newYellowDie,
                            eventDie = newEventDie
                        )
                    )
                }
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun EditRollDialog(
    roll: DieRollEntity,
    onDismiss: () -> Unit,
    onSave: (Int, Int, EventDie?) -> Unit
) {
    var redDie by remember { mutableStateOf(roll.redDie.toString()) }
    var yellowDie by remember { mutableStateOf(roll.yellowDie.toString()) }
    var eventDie by remember { mutableStateOf(roll.eventDie) }
    var redError by remember { mutableStateOf<String?>(null) }
    var yellowError by remember { mutableStateOf<String?>(null) }

    // Validate input
    fun validateRed(input: String): Boolean {
        val value = input.toIntOrNull()
        return when {
            input.isBlank() -> { redError = null; true }
            value == null -> { redError = "Invalid number"; false }
            value < 1 -> { redError = "Must be 1-6"; false }
            value > 6 -> { redError = "Must be 1-6"; false }
            else -> { redError = null; true }
        }
    }

    fun validateYellow(input: String): Boolean {
        val value = input.toIntOrNull()
        return when {
            input.isBlank() -> { yellowError = null; true }
            value == null -> { yellowError = "Invalid number"; false }
            value < 1 -> { yellowError = "Must be 1-6"; false }
            value > 6 -> { yellowError = "Must be 1-6"; false }
            else -> { yellowError = null; true }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_roll)) },
        text = {
            Column {
                OutlinedTextField(
                    value = redDie,
                    onValueChange = { 
                        redDie = it.filter { char -> char.isDigit() }.take(1)
                        validateRed(redDie)
                    },
                    label = { Text(stringResource(R.string.red_die_label)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    isError = redError != null,
                    supportingText = redError?.let { { Text(it) } }
                )
                OutlinedTextField(
                    value = yellowDie,
                    onValueChange = { 
                        yellowDie = it.filter { char -> char.isDigit() }.take(1)
                        validateYellow(yellowDie)
                    },
                    label = { Text(stringResource(R.string.yellow_die_label)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    isError = yellowError != null,
                    supportingText = yellowError?.let { { Text(it) } }
                )
                if (eventDie != null) {
                    ImprovedEventDieDropdown(
                        selectedEventDie = eventDie,
                        onEventDieSelected = { eventDie = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val redValid = validateRed(redDie)
                    val yellowValid = validateYellow(yellowDie)
                    if (redValid && yellowValid) {
                        val red = redDie.toIntOrNull() ?: 1
                        val yellow = yellowDie.toIntOrNull() ?: 1
                        onSave(red.coerceIn(1, 6), yellow.coerceIn(1, 6), eventDie)
                    }
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImprovedEventDieDropdown(
    selectedEventDie: EventDie?,
    onEventDieSelected: (EventDie?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedEventDie?.name ?: stringResource(R.string.no_event),
            onValueChange = {},
            label = { Text(stringResource(R.string.event_die_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.no_event)) },
                onClick = {
                    onEventDieSelected(null)
                    expanded = false
                }
            )
            EventDie.entries.forEach { eventDie ->
                DropdownMenuItem(
                    text = { Text(eventDie.name) },
                    onClick = {
                        onEventDieSelected(eventDie)
                        expanded = false
                    }
                )
            }
        }
    }
}
