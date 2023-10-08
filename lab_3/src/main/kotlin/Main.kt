import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Slider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.skia.Bitmap
import org.jetbrains.skiko.toImage
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.title.TextTitle
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import util.mean
import util.standardDeviation
import java.io.File
import java.util.*
import javax.imageio.ImageIO

const val STEP: Double = 0.001
const val NUMBERS_COUNT: Int = 10000

var PROBABILITY_1: Double = 0.8
const val MEAN_1: Double = 1.0
const val DERIVATION_1: Double = 3.0

val PROBABILITY_2: Double = 1.0 - PROBABILITY_1
const val MEAN_2: Double = 4.0
const val DERIVATION_2: Double = 3.0

fun main() = application {
    var firstSliderValue by remember { mutableStateOf(0.1f) }
    PROBABILITY_1 = firstSliderValue.toDouble()
    val firstVector = generateVector(NUMBERS_COUNT, MEAN_1, DERIVATION_1)
    val secondVector = generateVector(NUMBERS_COUNT, MEAN_2, DERIVATION_2)

    var xValues = doubleArrayOf()
    var y1Values = doubleArrayOf()
    var y2Values = doubleArrayOf()
    var areas = 0.0 to 0.0
    LaunchedEffect(firstVector, secondVector) {
        // Создание функций плотности вероятности на основе сгенерированных векторов и вероятностей.
        val firstFunction = generateProbabilityDensityFunction(
            firstVector.mean(), firstVector.standardDeviation(), PROBABILITY_1
        )
        val secondFunction = generateProbabilityDensityFunction(
            secondVector.mean(), secondVector.standardDeviation(), PROBABILITY_2
        )

        // Определение интервала значений X на основе объединения обоих векторов.
        val interval = getInterval(firstVector, secondVector)

        // Генерация значений X с заданным шагом.
        xValues = doubleArrayFromRange(interval.first, interval.second, STEP)

        // Вычисление значений Y для обеих функций на основе X.
        y1Values = xValues.map(firstFunction).toDoubleArray()
        y2Values = xValues.map(secondFunction).toDoubleArray()

        // Нахождение места пересечения колоколов вероятности
        val firstIsBigger = y1Values[0] > y2Values[0]
        var separatorI = 0
        for (i in xValues.indices) {
            val rule = if (firstIsBigger) y2Values[i] >= y1Values[i] else y1Values[i] > y2Values[i]

            if (rule) {
                separatorI = i
                println("arr[$i]=${xValues[i]}")
                break
            }
        }

        // Вычисление областей под кривыми и отрисовка графика с информацией о долях ошибок.
        areas = getAreas(y1Values, y2Values, STEP, xValues, separatorI)
        draw(xValues, y1Values, y2Values, areas)
    }

    var bitmap = ImageBitmap(100, 100)
    LaunchedEffect(xValues, y1Values, y2Values, areas) {
        val f = File("/home/bashlikovvv/Bsuir/DmMaA/lab_3/src/main/resources/clustering-mistake-${String.format(Locale.ROOT, "%.1f", PROBABILITY_1)}.jpg")
        if (f.exists()) {
            val image = ImageIO.read(f)
            bitmap = Bitmap.makeFromImage(image.toImage()).asImageBitmap()
        }
    }

    Window(onCloseRequest = ::exitApplication) {
        MaterialTheme {
            Scaffold(
                topBar = {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Slider(
                            value = firstSliderValue,
                            onValueChange = { firstSliderValue = it },
                            valueRange = 0f..1f
                        )
                    }
                }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

fun draw(xValues: DoubleArray, y1Values: DoubleArray, y2Values: DoubleArray, areas: Pair<Double, Double>) {
    // Создание набора данных для графика.
    val dataset = XYSeriesCollection()
    val series1 = XYSeries("First Function")
    val series2 = XYSeries("Second Function")

    // Заполнение данных для первой и второй функции.
    for (i in xValues.indices) {
        series1.add(xValues[i], y1Values[i])
        series2.add(xValues[i], y2Values[i])
    }

    dataset.addSeries(series1)
    dataset.addSeries(series2)

    // Создание графика с заданными параметрами.
    val chart = ChartFactory.createXYAreaChart(
        "Probabilistic Classification", "X", "Y", dataset, PlotOrientation.VERTICAL, true, false, false
    )

    // Добавление подзаголовков с информацией о доле ошибок.
    chart.addSubtitle(TextTitle("Detection mistake: ${String.format("%.3f", areas.first * 100)}%"))
    chart.addSubtitle(TextTitle("False positive: ${String.format("%.3f", areas.second * 100)}%"))
    chart.addSubtitle(TextTitle("Summary mistake: ${String.format("%.3f", (areas.first + areas.second) * 100)}%"))

    val file = File("/home/bashlikovvv/Bsuir/DmMaA/lab_3/src/main/resources/clustering-mistake-${String.format(Locale.ROOT, "%.1f", PROBABILITY_1)}.jpg")
    if (!file.exists()) {
        file.createNewFile()
    }

    // Сохранение графика как изображения PNG.
    ChartUtils.saveChartAsPNG(
        File("/home/bashlikovvv/Bsuir/DmMaA/lab_3/src/main/resources/clustering-mistake-${String.format(Locale.ROOT, "%.1f", PROBABILITY_1)}.jpg"), chart, 800, 500
    )
}