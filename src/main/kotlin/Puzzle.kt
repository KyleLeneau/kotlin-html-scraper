import org.jsoup.nodes.Element

data class Puzzle(
        val collection: String,
        val title: String,
        val series: Int,
        val number: Int,
        val notes: String,
        val thumbnail: String,
        val largeImage: String) {

    companion object {
        class Builder {
            private var collection: String = ""
            private var title: String = ""
            private var series: Int = 0
            private var number: Int = 0
            private var notes: String = ""
            private var thumbnail: String = ""
            private var largeImage: String = ""

            fun images(imageData: ImageData): Builder {
                thumbnail = imageData.thumbnail
                largeImage = imageData.largeImage
                return this
            }

            fun titleInfo(titleCollection: TitleCollection): Builder {
                collection = titleCollection.collection
                title = titleCollection.title
                return this
            }

            fun seriesInfo(seriesInfo: SeriesInfo): Builder {
                series = seriesInfo.series
                number = seriesInfo.number
                return this
            }

            fun notes(newNotes: String): Builder {
                notes = newNotes
                return this
            }

            fun build() = Puzzle(
                    collection = collection,
                    title = title,
                    series = series,
                    number = number,
                    notes = notes,
                    thumbnail = thumbnail,
                    largeImage = largeImage
            )
        }
    }
}

fun Element.toPuzzle(): Puzzle? {
    if (!hasClass("4u"))
        return null

    val builder = Puzzle.Companion.Builder()

    val img = selectFirst("img")
    if (img != null) {
        val images = img.parseImages(baseUrl = "http://www.charleswysockipuzzles.com/Wysocki/")
        builder.images(images)

        val titleCollection = img.parseTitleCollection()
        builder.titleInfo(titleCollection)
    }

    val font = selectFirst("div > font")
    if (font != null) {
        val seriesInfo = font.parseSeriesInfo()
        builder.seriesInfo(seriesInfo)
    }

    val notesElement = selectFirst("td > font")
    if (notesElement != null) {
        val cleanNotes = notesElement.text().replace(oldValue = "Notes:  ", newValue = "")
        builder.notes(cleanNotes)
    }

    return builder.build()
}

data class ImageData(val thumbnail: String, val largeImage: String)
private fun Element.parseImages(baseUrl: String): ImageData {
    val src = attr("src")
    if (src != null && src.isNotEmpty()) {
        val largeImage = src.replace(oldValue = "-150", newValue = "-300")
        return ImageData(thumbnail = baseUrl + src, largeImage = baseUrl + largeImage)
    }
    return ImageData(thumbnail = "", largeImage = "")
}

data class TitleCollection(val title: String, val collection: String)
private fun Element.parseTitleCollection(): TitleCollection {
    val alt = attr("alt")
    if (alt != null && alt.isNotEmpty()) {
        val title = alt.substringBefore(" - ")
        val collection = alt.substringAfter(delimiter = " - ")
        return TitleCollection(title = title, collection = collection)
    }
    return TitleCollection(title = "", collection = "")
}

data class SeriesInfo(val series: Int, val number: Int)
private fun Element.parseSeriesInfo(): SeriesInfo {
    val text = text()
    if (text != null && text.isNotEmpty()) {
        val match = """^.*(\d+).*#(\d+).*""".toRegex().matchEntire(text)

        val groups = match?.groupValues.orEmpty()
        val series = groups.getOrElse(index = 1, defaultValue = { "-1" }).toInt()
        val number = groups.getOrElse(index = 2, defaultValue = { "-1" }).toInt()
        return SeriesInfo(series = series, number = number)
    }

    return SeriesInfo(series = -1, number = -1)
}
