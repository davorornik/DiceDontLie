package si.ornik.dicedontlie.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.point
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.Position
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import si.ornik.dicedontlie.R
import si.ornik.dicedontlie.data.DatabaseProvider
import si.ornik.dicedontlie.data.EventDie
import si.ornik.dicedontlie.data.NumberFrequency
import si.ornik.dicedontlie.data.RollDistribution
import si.ornik.dicedontlie.data.RollDistributionSimple
import si.ornik.dicedontlie.data.toDieRoll
import si.ornik.dicedontlie.data.toGame
import si.ornik.dicedontlie.ui.theme.RedDie
import si.ornik.dicedontlie.ui.theme.EventDieBlue
import si.ornik.dicedontlie.ui.theme.EventDieGreen
import si.ornik.dicedontlie.ui.theme.EventDieYellow
import si.ornik.dicedontlie.ui.theme.getEventDieColor
import java.util.Locale

@Composable
fun StatisticsScreen(gameId: String, modifier: Modifier = Modifier) {
  val gameEntity by
      DatabaseProvider.getGameDao().getGameByIdFlow(gameId).collectAsState(initial = null)
  val rolls by
      DatabaseProvider.getDieRollDao().getRollsForGame(gameId).collectAsState(initial = emptyList())

  val game by remember { derivedStateOf { gameEntity?.toGame(rolls.map { it.toDieRoll() }) } }

  Column(
      modifier = modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
      Text(
          text = stringResource(R.string.statistics_screen),
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.padding(bottom = 8.dp)
      )

    if (game != null) {
      Text("Game: ${game!!.name}", style = MaterialTheme.typography.bodyMedium)
      Text(
          "Players: ${game!!.players.joinToString(", ")}",
          style = MaterialTheme.typography.bodyMedium,
      )
      Text("Total rolls: ${rolls.size}", style = MaterialTheme.typography.bodyMedium)

      if (rolls.isNotEmpty()) {
        if (game!!.eventDieEnabled) {
          RollDistributionChartCitiesAndKnights(gameId = gameId)
        } else {
          RollDistributionChart(gameId = gameId)
        }
        InterestingFactsSection(gameId = gameId)
      }
    } else {
      Text("Game not found", style = MaterialTheme.typography.bodyMedium)
    }
  }
}

@Composable
private fun RollDistributionChart(gameId: String) {
  val rollDistribution by
      DatabaseProvider.getDieRollDao()
          .getRollDistribution(gameId)
          .collectAsState(initial = emptyList())

  if (rollDistribution.isEmpty()) return

  val rollCounts = createSimpleRollCountArray(rollDistribution)
  val totalRolls = rollCounts.sum().toFloat()

  val modelProducer = remember { CartesianChartModelProducer() }

  LaunchedEffect(rollCounts) {
    modelProducer.runTransaction {
      columnSeries { series(rollCounts.toList()) }
      lineSeries { series(calculateExpectedWeights(totalRolls)) }
    }
  }

  RollDistributionChartContent(
      modelProducer = modelProducer,
      columnColors = listOf(EventDieBlue),
      legend = { ChartLegendSimple() },
  )
}

@Composable
private fun RollDistributionChartCitiesAndKnights(gameId: String) {
  val rollDistribution by
      DatabaseProvider.getDieRollDao()
          .getRollDistributionCitiesAndKnights(gameId)
          .collectAsState(initial = emptyList())

  if (rollDistribution.isEmpty()) return

  val rollCounts = createCitiesAndKnightsRollMap(rollDistribution)

  // Calculate total rolls per die type for expected weight scaling
  val totalsPerDie =
      mapOf(
          EventDie.POLITICS to rollCounts[EventDie.POLITICS]!!.sum().toFloat(),
          EventDie.SCIENCE to rollCounts[EventDie.SCIENCE]!!.sum().toFloat(),
          EventDie.TRADE to rollCounts[EventDie.TRADE]!!.sum().toFloat(),
          EventDie.PIRATES to rollCounts[EventDie.PIRATES]!!.sum().toFloat(),
      )

  val modelProducer = remember { CartesianChartModelProducer() }

  LaunchedEffect(rollCounts) {
    modelProducer.runTransaction {
      columnSeries {
        series(rollCounts[EventDie.POLITICS]!!.toList())
        series(rollCounts[EventDie.SCIENCE]!!.toList())
        series(rollCounts[EventDie.TRADE]!!.toList())
        series(rollCounts[EventDie.PIRATES]!!.toList())
      }
      lineSeries {
        series(
            calculateExpectedWeights(
                totalsPerDie[EventDie.POLITICS]!! +
                    totalsPerDie[EventDie.SCIENCE]!! +
                    totalsPerDie[EventDie.TRADE]!! +
                    totalsPerDie[EventDie.PIRATES]!!
            )
        )
      }
    }
  }

  RollDistributionChartContent(
      modelProducer = modelProducer,
      columnColors =
          listOf(
              EventDieBlue,
              EventDieGreen,
              EventDieYellow,
              getEventDieColor(eventDie = EventDie.PIRATES),
          ),
      legend = { ChartEventDie() },
  )
}

@Composable
private fun RollDistributionChartContent(
    modelProducer: CartesianChartModelProducer,
    columnColors: List<Color>,
    legend: @Composable () -> Unit,
) {
  val lineDataLabel =
      rememberTextComponent(
          color = Color.Red,
          textSize = 12.sp,
      )

  CartesianChartHost(
      modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp).padding(top = 16.dp, bottom = 8.dp),
      chart =
          rememberCartesianChart(
              rememberColumnCartesianLayer(
                  columnProvider =
                      ColumnCartesianLayer.ColumnProvider.series(
                          columnColors.map { color ->
                            rememberLineComponent(
                                fill = fill(color),
                                thickness = 14.dp,
                            )
                          }
                      ),
                  mergeMode = { ColumnCartesianLayer.MergeMode.Stacked },
              ),
              rememberLineCartesianLayer(
                  lineProvider =
                      LineCartesianLayer.LineProvider.series(
                          List(columnColors.size) {
                            LineCartesianLayer.rememberLine(
                                fill = LineCartesianLayer.LineFill.single(Fill.Transparent),
                                areaFill = null,
                                pointProvider =
                                    LineCartesianLayer.PointProvider.single(
                                        LineCartesianLayer.point(
                                            component =
                                                rememberShapeComponent(
                                                    fill = fill(Color.Red),
                                                    shape = CorneredShape.Pill,
                                                )
                                        )
                                    ),
                                dataLabel = lineDataLabel,
                                dataLabelPosition = Position.Vertical.Top,
                            )
                          }
                      ),
              ),
              startAxis = VerticalAxis.rememberStart(),
              bottomAxis =
                  HorizontalAxis.rememberBottom(
                      valueFormatter = { _, value, _ -> (value.toInt() + 2).toString() }
                  ),
          ),
      modelProducer = modelProducer,
  )

  legend()
}

private fun createSimpleRollCountArray(rollDistribution: List<RollDistributionSimple>): IntArray {
  val rollCounts = IntArray(11)
  rollDistribution.forEach { distribution ->
    val index = distribution.sum - 2
    if (index in 0..10) {
      rollCounts[index] = distribution.count
    }
  }
  return rollCounts
}

private fun createCitiesAndKnightsRollMap(
    rollDistribution: List<RollDistribution>
): Map<EventDie, IntArray> {
  val result =
      mapOf(
          EventDie.POLITICS to IntArray(11),
          EventDie.SCIENCE to IntArray(11),
          EventDie.TRADE to IntArray(11),
          EventDie.PIRATES to IntArray(11),
      )

  rollDistribution.forEach { distribution ->
    val index = distribution.sum - 2
    if (index in 0..10) {
      distribution.eventDie?.let { eventDie ->
        result[eventDie]?.let { arr -> arr[index] = distribution.count }
      }
    }
  }

  return result
}

/**
 * Calculates expected weights for dice rolls (2-12) based on standard 2-dice probability. The
 * probabilities are: 2: 1/36, 3: 2/36, 4: 3/36, 5: 4/36, 6: 5/36, 7: 6/36, 8: 5/36, 9: 4/36, 10:
 * 3/36, 11: 2/36, 12: 1/36
 */
private fun calculateExpectedWeights(totalRolls: Float): List<Float> {
  val probabilities =
      listOf(
          1f / 36f, // 2
          2f / 36f, // 3
          3f / 36f, // 4
          4f / 36f, // 5
          5f / 36f, // 6
          6f / 36f, // 7
          5f / 36f, // 8
          4f / 36f, // 9
          3f / 36f, // 10
          2f / 36f, // 11
          1f / 36f, // 12
      )
  return probabilities.map { it * totalRolls }
}

@Composable
fun LegendItem(color: Color, label: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.size(12.dp).background(color, CircleShape))
    Spacer(Modifier.width(2.dp))
    Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    Spacer(Modifier.width(2.dp))
  }
}

@Composable
fun ChartLegend(items: List<Pair<Color, String>>) {
  FlowRow(modifier = Modifier.fillMaxWidth(), maxItemsInEachRow = 3, horizontalArrangement = Arrangement.SpaceEvenly) {
    items.forEach { (color, label) -> LegendItem(color, label) }
  }
}

@Composable
fun ChartLegendSimple() {
  ChartLegend(
      listOf(EventDieBlue to "Rolls", RedDie to "expected probability")
  )
}

@Composable
fun ChartEventDie() {
  ChartLegend(
      listOf(
          EventDieBlue to "Politics",
          EventDieGreen to "Science",
          EventDieYellow to "Trade",
          getEventDieColor(eventDie = EventDie.PIRATES) to "Pirates",
          RedDie to "expected probability",
      )
  )
}

@Composable
fun InterestingFactsSection(gameId: String) {
  val dao = DatabaseProvider.getDieRollDao()
  val hotNumbers = remember(gameId) { mutableStateOf<List<NumberFrequency>>(emptyList()) }
  val coldNumbers = remember(gameId) { mutableStateOf<List<NumberFrequency>>(emptyList()) }
  val longestStreak = remember(gameId) { mutableStateOf<Pair<Int, Int>?>(null) } // Pair<sum, length>
  val avgTimeBetweenRolls = remember(gameId) { mutableStateOf<Double?>(null) }

  LaunchedEffect(gameId) {
      hotNumbers.value = dao.getHotNumbers(gameId)
      coldNumbers.value = dao.getColdNumbers(gameId)
      longestStreak.value = dao.getLongestStreak(gameId)?.let { Pair(it.sum, it.streak_length) }
      avgTimeBetweenRolls.value = dao.getAverageTimeBetweenRolls(gameId)
  }

  Text(
      "Interesting Facts",
      style = MaterialTheme.typography.titleSmall,
      modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
  )

  // Hot Numbers
  if (hotNumbers.value.isNotEmpty()) {
      Text("🔥 Hot Numbers:", style = MaterialTheme.typography.bodyMedium)
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          hotNumbers.value.forEach { nf ->
              val percentageText = String.format(Locale.getDefault(), "%.1f", nf.percentage)
              FactChip("${nf.sum}: ${nf.count}x ($percentageText%)")
          }
      }
  }

  // Cold Numbers
  if (coldNumbers.value.isNotEmpty()) {
      Text("❄️ Cold Numbers:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          coldNumbers.value.forEach { nf ->
              val percentageText = String.format(Locale.getDefault(), "%.1f", nf.percentage)
              FactChip("${nf.sum}: ${nf.count}x ($percentageText%)")
          }
      }
  }

  // Longest Streak
  longestStreak.value?.let { (streakSum, streakLength) ->
      Text("📈 Longest Streak:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
      FactChip("Sum $streakSum - $streakLength consecutive rolls")
  }

  // Average Time Between Rolls
  avgTimeBetweenRolls.value?.let { avgMs ->
      val avgSeconds = avgMs / 1000.0
      val avgSecondsText = String.format(Locale.getDefault(), "%.1f", avgSeconds)
      Text("⏱️ Average Time Between Rolls:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
      FactChip("$avgSecondsText seconds")
  }
}

@Composable
private fun FactChip(text: String) {
  Box(
      modifier = Modifier
          .background(
              MaterialTheme.colorScheme.primaryContainer,
              MaterialTheme.shapes.small
          )
          .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
      Text(text, style = MaterialTheme.typography.bodySmall)
  }
}
