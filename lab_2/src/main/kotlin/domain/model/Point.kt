package domain.model

import kotlin.math.hypot

data class Point(val x: Int, val y: Int) {

    fun distanceTo(p: Point): Double = distanceTo(p.x, p.y)

    private fun distanceTo(x: Int, y: Int): Double = hypot((this.x - x).toDouble(), (this.y - y).toDouble())

    fun farthestPointOf(points: Array<Point>): Point = points.maxBy { this.distanceTo(it) }
}