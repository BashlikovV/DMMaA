import domain.model.Cluster
import domain.model.Distance
import domain.model.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.random.Random

fun <T> Array<T>.randomElement(): T? = (if (this.isEmpty()) null else this[Random.nextInt(this.size)])

suspend fun clusterByMaximin(
    points: Array<Point>,
    progressCallback: (clusters: Array<Cluster>) -> Unit,
    callback: (Boolean) -> Unit
): Array<Cluster> = withContext(Dispatchers.Default) {
    callback.invoke(true)

    var newSite = points.randomElement()
    val sites = mutableListOf(
        newSite!!, newSite.farthestPointOf(points)
    )
    var clusters = splitForClusters(points, sites)
    while (newSite != null) {
        newSite = chooseNewSite(clusters)
        if (newSite != null) {
            sites.add(newSite)
            clusters = splitForClusters(points, sites)
        }
        progressCallback.invoke(clusters)
        delay(500)
    }

    callback.invoke(false)
    return@withContext clusters
}

fun chooseNewSite(clusters: Array<Cluster>): Point? {
    val candidateDistances = mutableListOf<Distance>()
    for ((site, clusterPoints) in clusters) {
        candidateDistances.add(Distance(site, site.farthestPointOf(clusterPoints)))
    }

    val leadCandidateDistance = candidateDistances.maxWith { a, _ -> (a.length - a.length).toInt() }
    val halfAverageSitesDistance = averageSitesDistanceOf(Array(clusters.size) { index -> clusters[index].site }) / 2
    return if (leadCandidateDistance.length > halfAverageSitesDistance) leadCandidateDistance.b else null
}

fun averageSitesDistanceOf(sites: Array<Point>): Double {
    var distanceSum = 0.0
    sites.forEach { a -> sites.forEach { b -> distanceSum += a.distanceTo(b) } }
    return distanceSum / (sites.size * sites.size)
}

fun splitForClusters(points: Array<Point>, sites: List<Point>): Array<Cluster> {
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