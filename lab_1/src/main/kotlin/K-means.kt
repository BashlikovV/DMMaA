import domain.model.Cluster
import domain.model.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun <T> Array<T>.deepEquals(other: Array<T>): Boolean = this.contentDeepEquals(other)

private fun centroidOf(points: Array<Point>): Point {
	var centerX = 0.0
	var centerY = 0.0
	for ((x, y) in points) {
		centerX += x
		centerY += y
	}
	centerX /= points.size
	centerY /= points.size

	return Point(centerX.toInt(), centerY.toInt())
}

private fun splitForClusters(points: Array<Point>, sites: Array<Point>): Array<Cluster> {
	val clusters = Array(sites.size) { mutableListOf<Point>() }

	for (point in points) {
		var n = 0
		for (i in 0..sites.lastIndex) {
			if (sites[i].distanceTo(point) < sites[n].distanceTo(point)) {
				n = i
			}
		}
		clusters[n].add(point)
	}

	return Array(sites.size) { index ->
		Cluster(sites[index], clusters[index].toTypedArray())
	}
}

suspend fun clusterByKMeans(
	points: Array<Point>,
	sites: Array<Point>,
	callback: (Boolean) -> Unit
): Array<Cluster> = withContext(Dispatchers.Default) {
	callback.invoke(true)
	var clusters: Array<Cluster>

	var tmpSites: Array<Point> = sites
	var i = 0
	do {
		i++
		val oldSites = tmpSites
		clusters = splitForClusters(points, oldSites)

		tmpSites = Array(oldSites.size) { centroidOf(clusters[it].points) }
	} while (!oldSites.deepEquals(tmpSites) && i < 100)

	callback.invoke(false)
	return@withContext clusters
}