import java.util.*
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

fun doubleArrayFromRange(start: Double, end: Double, step: Double): DoubleArray {
    return generateSequence(start) { it + step }.takeWhile { it <= end }.toList().toDoubleArray()
}

fun gaussianRandomNumber(mean: Double, derivation: Double): Double = Random().nextGaussian() * derivation + mean

fun generateVector(length: Int, mean: Double, derivation: Double): DoubleArray {
    return DoubleArray(length) { gaussianRandomNumber(mean, derivation) }
}

fun getInterval(firstVector: DoubleArray, secondVector: DoubleArray): Pair<Double, Double> {
    val allPoints = firstVector.plus(secondVector)

    return allPoints.min() to allPoints.max()
}

fun gaussian(x: Double, mean: Double, derivation: Double): Double {
    val sqrt2PI: Double = sqrt(2 * Math.PI)
    var result = 1 / (derivation * sqrt2PI)
    result *= exp(-0.5 * ((x - mean) / derivation).pow(2.0))

    return result
}

fun generateProbabilityDensityFunction(
    vectorMean: Double, vectorDerivation: Double, probability: Double
): (Double) -> Double {
    return { x: Double -> gaussian(x, vectorMean, vectorDerivation) * probability }
}

fun getAreas(
    y1Values: DoubleArray, y2Values: DoubleArray, step: Double, xValues: DoubleArray, separatorI: Int
): Pair<Double, Double> {
    var detectionMistake = 0.0
    var falseAlarm = 0.0

    for (i in xValues.indices) {
        if (i < separatorI) {
            falseAlarm += step * y2Values[i]
        } else {
            detectionMistake += step * y1Values[i]
        }
    }

    return detectionMistake to falseAlarm
}