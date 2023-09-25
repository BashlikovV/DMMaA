package domain.model

data class Distance(val a: Point, val b: Point) {
    val length: Double = a.distanceTo(b)
}