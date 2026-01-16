package si.ornik.dicedontlie.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import si.ornik.dicedontlie.R
import si.ornik.dicedontlie.data.*
import si.ornik.dicedontlie.ui.theme.*

@Composable
fun DieRollScreen(gameId: String, modifier: Modifier = Modifier) {
  val scope = rememberCoroutineScope()
  val rolls by
      DatabaseProvider.getDieRollDao().getRollsForGame(gameId).collectAsState(initial = emptyList())
  val gameEntity by
      DatabaseProvider.getGameDao().getGameByIdFlow(gameId).collectAsState(initial = null)

  var redDie: Int? by remember { mutableStateOf(null) }
  var yellowDie: Int? by remember { mutableStateOf(null) }
  var eventDie: EventDie? by remember { mutableStateOf(null) }

  val isEventDieEnabled = gameEntity?.eventDieEnabled == true
  // Custom snackbar state

  var lastInsertedRoll by remember { mutableStateOf<DieRollEntity?>(null) }
  var showCustomSnackbar by remember { mutableStateOf(false) }
  var snackbarRedDie by remember { mutableIntStateOf(0) }
  var snackbarYellowDie by remember { mutableIntStateOf(0) }
  var snackbarEventDie by remember { mutableStateOf<EventDie?>(null) }
  var totalRolls by remember { mutableStateOf(0) }

  // Icon button size - 1.5x bigger than original 80.dp
  val iconButtonSize = 120.dp
  val iconSize = 72.dp // 1.5x bigger than original 48.dp to maintain proportions

  // Save the roll when user clicks the confirm button or after all dice are selected
  fun saveRoll() {
    if (redDie != null && yellowDie != null) {
      // For event die, require event die to be selected
      if (isEventDieEnabled && eventDie == null) {
          return
      }

      // Capture values before resetting state
      val savedRedDie = redDie!!
      val savedYellowDie = yellowDie!!
      val savedEventDie = eventDie

      // Reset selection immediately (outside launch block for instant UI update)
      redDie = null
      yellowDie = null
      eventDie = null

      scope.launch {
        val newRoll =
            DieRoll(redDie = savedRedDie, yellowDie = savedYellowDie, eventDie = savedEventDie)
        val entity = newRoll.toEntity(gameId)
        val insertId = DatabaseProvider.getDieRollDao().insertDieRoll(entity)
        lastInsertedRoll = entity.copy(id = insertId)

        // Update total rolls counter
        totalRolls = rolls.size + 1

        // Store values for custom styled snackbar
        snackbarRedDie = savedRedDie
        snackbarYellowDie = savedYellowDie
        snackbarEventDie = savedEventDie
        showCustomSnackbar = true
      }
    }
  }

  // Check if all required dice are selected and auto-save
  fun tryAutoSave() {
    if (redDie != null && yellowDie != null) {
      // For event die, require event die to be selected
      if (isEventDieEnabled && eventDie == null) {
          return
      }
      saveRoll()
    }
  }

  // Toggle die selection - if already selected, unselect it
  fun toggleRedDie(value: Int) {
    redDie = if (redDie == value) null else value
    tryAutoSave()
  }

  fun toggleYellowDie(value: Int) {
    yellowDie = if (yellowDie == value) null else value
    tryAutoSave()
  }

  fun toggleEventDie(value: EventDie) {
    eventDie = if (eventDie == value) null else value
    tryAutoSave()
  }

  // Text(stringResource(R.string.total_rolls, totalRolls), style =
  // MaterialTheme.typography.titleMedium)

  // Update total rolls counter when rolls change
  LaunchedEffect(rolls) {
    totalRolls = rolls.size
  }

  Box(modifier = modifier.fillMaxSize().padding(16.dp)) {
      Column(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
      // Total rolls counter in the top right corner
      Box(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = stringResource(R.string.total_rolls, totalRolls),
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.align(Alignment.TopEnd)
        )
      }
      // Die buttons row - all buttons on the same horizontal line
      Row(
          modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.Top,
      ) {
        // Red die buttons (1-6) - using VectorDrawables
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          (1..6).forEach { dieValue ->
            val diceIcon =
                when (dieValue) {
                  1 -> R.drawable.die_red_one
                  2 -> R.drawable.die_red_two
                  3 -> R.drawable.die_red_three
                  4 -> R.drawable.die_red_four
                  5 -> R.drawable.die_red_five
                  6 -> R.drawable.die_red_six
                  else -> R.drawable.die_red_one
                }

            IconButton(
                onClick = { toggleRedDie(dieValue) },
                modifier = Modifier.size(iconButtonSize),
                colors =
                    IconButtonDefaults.iconButtonColors(
                        containerColor =
                            if (redDie == dieValue) MaterialTheme.colorScheme.primaryContainer
                            else Color.Unspecified
                    ),
            ) {
              Icon(
                  painter = painterResource(id = diceIcon),
                  contentDescription = stringResource(R.string.red_die, dieValue),
                  modifier = Modifier.size(iconSize),
                  tint = Color.Unspecified,
              )
            }
          }
        }

        // Yellow die buttons (1-6) - using VectorDrawables
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          (1..6).forEach { dieValue ->
            val diceIcon =
                when (dieValue) {
                  1 -> R.drawable.die_yellow_one
                  2 -> R.drawable.die_yellow_two
                  3 -> R.drawable.die_yellow_three
                  4 -> R.drawable.die_yellow_four
                  5 -> R.drawable.die_yellow_five
                  6 -> R.drawable.die_yellow_six
                  else -> R.drawable.die_yellow_one
                }

            IconButton(
                onClick = { toggleYellowDie(dieValue) },
                modifier = Modifier.size(iconButtonSize),
                colors =
                    IconButtonDefaults.iconButtonColors(
                        containerColor =
                            if (yellowDie == dieValue) MaterialTheme.colorScheme.primaryContainer
                            else Color.Unspecified
                    ),
            ) {
              Icon(
                  painter = painterResource(id = diceIcon),
                  contentDescription = stringResource(R.string.yellow_die, dieValue),
                  modifier = Modifier.size(iconSize),
                  tint = Color.Unspecified,
              )
            }
          }
        }

        if (isEventDieEnabled) {
          Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            EventDie.entries.forEach { dieValue ->
              val diceIcon =
                  when (dieValue) {
                    EventDie.POLITICS -> R.drawable.die_event_blue
                    EventDie.SCIENCE -> R.drawable.die_event_green
                    EventDie.TRADE -> R.drawable.die_event_yellow
                    EventDie.PIRATES -> R.drawable.die_event_black
                  }

              IconButton(
                  onClick = { toggleEventDie(dieValue) },
                  modifier = Modifier.size(iconButtonSize),
                  colors =
                      IconButtonDefaults.iconButtonColors(
                          containerColor =
                              if (eventDie == dieValue) MaterialTheme.colorScheme.primaryContainer
                              else Color.Unspecified
                      ),
              ) {
                Icon(
                    painter = painterResource(id = diceIcon),
                    contentDescription = stringResource(R.string.event_die, dieValue.name),
                    modifier = Modifier.size(iconSize),
                    tint = Color.Unspecified,
                )
              }
            }
          }
        } else {
          // Reserve space for event die buttons when not using expansion
          Spacer(modifier = Modifier.width(iconButtonSize))
        }
      }
      LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
        itemsIndexed(rolls, key = { _, roll -> roll.id }) { index, roll ->
          val rollNumber = index + 1
          val annotatedText = buildAnnotatedString {
            // Show move ID first
            append(stringResource(R.string.move_number, rollNumber))

            // Add colored die values using DiceDontLieTheme
            withStyle(style = SpanStyle(color = RedDie)) { append("${roll.redDie}") }
            append(" ")
            withStyle(style = SpanStyle(color = YellowDie)) {
              append("${roll.yellowDie}")
            }

            // Add event die if present using DiceDontLieTheme
            if (roll.eventDie != null) {
              append(" ")
              val eventColor = getEventDieColor(roll.eventDie)
              withStyle(style = SpanStyle(color = eventColor)) {
                append("${roll.eventDie.name.first()}")
              }
            }

            // Add total
            append(stringResource(R.string.dice_total, roll.redDie + roll.yellowDie))
          }
          Text(annotatedText, style = MaterialTheme.typography.bodyMedium)
        }
      }
    }

    // Snackbar positioned at the bottom
    if (showCustomSnackbar) {
      Snackbar(
          modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
          containerColor = MaterialTheme.colorScheme.surfaceContainer,
          contentColor = MaterialTheme.colorScheme.onSurface,
          action = {
            TextButton(
                onClick = {
                  if (lastInsertedRoll != null) {
                    scope.launch {
                      DatabaseProvider.getDieRollDao().deleteDieRoll(lastInsertedRoll!!)
                      lastInsertedRoll = null
                    }
                  }
                  showCustomSnackbar = false
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
              Text("Undo")
            }
          },
      ) {
        // Capture event die for smart cast
        val currentEventDie = snackbarEventDie
        val annotatedText = buildAnnotatedString {
          // Style "Roll:" using theme-aware color
          withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) { append("Roll: ") }
          withStyle(style = SpanStyle(color = RedDie)) { append("$snackbarRedDie") }
          append(" + ")
          withStyle(style = SpanStyle(color = YellowDie)) {
            append("$snackbarYellowDie")
          }
          if (isEventDieEnabled && currentEventDie != null) {
              append(" + ")
              val eventColor = getEventDieColor(currentEventDie)
              withStyle(style = SpanStyle(color = eventColor)) { append(currentEventDie.name) }
          }
          // Style "=" and total using theme-aware color
          withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
            append(" = ${snackbarRedDie + snackbarYellowDie}")
          }
        }
        Text(text = annotatedText, color = MaterialTheme.colorScheme.onSurface)
      }
    }
  }

  LaunchedEffect(showCustomSnackbar) {
    if (showCustomSnackbar) {
      delay(2000)
      showCustomSnackbar = false
    }
  }
}
