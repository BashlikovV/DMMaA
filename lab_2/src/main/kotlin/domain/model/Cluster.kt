package domain.model

data class Cluster(val site: Point, val points: Array<Point>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }

        other as Cluster

        if (site != other.site) {
            return false
        }
        if (!points.contentEquals(other.points)) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = site.hashCode()
        result = 31 * result + points.contentHashCode()
        return result
    }
}