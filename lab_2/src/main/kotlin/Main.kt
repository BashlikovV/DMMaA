import Colors.COLORS
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import domain.model.Cluster
import domain.model.Point
import java.util.*
import kotlin.math.absoluteValue

fun main() = application {
	var progressVisibility by remember { mutableStateOf(true) }
	var pointCount by remember { mutableStateOf(1_000f) }
	var clusters by remember { mutableStateOf<Array<Cluster>>(emptyArray()) }

	LaunchedEffect(pointCount) {
		progressVisibility = true
		val random = Random()
		val points = Array(pointCount.toInt()) { Point(random.nextInt(1920), random.nextInt(1000)) }
		clusters = clusterByMaximin(points, { clusters = it }) { progressVisibility = it }
	}

	Window(onCloseRequest = ::exitApplication) {
		MaterialTheme {
			Scaffold(
				topBar = { Column(Modifier.height(100.dp).fillMaxWidth()) {
					Row(Modifier.fillMaxHeight(0.5f).fillMaxWidth()) {
						Slider(
							valueRange = 1_000f..100_000f,
							value = pointCount,
							onValueChange = { pointCount = it },
							modifier = Modifier.fillMaxWidth(0.5f)
						)
					}
					Row(
						Modifier.fillMaxHeight(0.5f).fillMaxWidth(),
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.SpaceEvenly
					) {
						Column { Text("points count: ${pointCount.absoluteValue}") }
						if (progressVisibility) { Column { LinearProgressIndicator() } }
					}
				} }
			) {
				Column(
					modifier = Modifier.fillMaxSize(),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.Center
				) {
					Canvas(modifier = Modifier.fillMaxSize()) {
						val colors = Array(clusters.size) { index -> COLORS[index % COLORS.size] }
						clusters.forEachIndexed { i, (site, points) ->
							points.forEach {
								val color = colors[i]
								drawCircle(
									color = Color(red = color.red, green = color.green, blue = color.green),
									radius = 2f,
									center = Offset(it.x.toFloat(), it.y.toFloat())
								)
							}
							drawCircle(
								color = Color.Black,
								radius = 5f,
								center = Offset(site.x.toFloat(), site.y.toFloat())
							)
						}
					}
				}
			}
		}
	}
}