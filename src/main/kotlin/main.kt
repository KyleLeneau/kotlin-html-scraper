import com.beust.jcommander.JCommander
import com.github.kittinunf.fuel.httpGet
import org.jsoup.Jsoup
import org.jsoup.select.Elements

internal class Main {
    private val pageToParse = "http://www.charleswysockipuzzles.com/Wysocki/Master-Checklist.aspx"

    fun run() {
        val responseString = getHtmlResponse()
        var elements = findElements(html = responseString)
        val puzzles = parseElements(elements = elements)

        // TODO: print these out to JSON instead
        print(puzzles.toString())
    }

    private fun getHtmlResponse(): String {
        val (_, _, result) = pageToParse.httpGet().responseString()
        return result.get()
    }

    private fun findElements(html: String): Elements {
        return Jsoup.parse(html)
                .select("#MainContent_lblPuzzles")
                .select("div.4u")
    }

    private fun parseElements(elements: Elements): Array<Puzzle> {
        val results = elements.mapNotNull { it.toPuzzle() }
        return results.toTypedArray()
    }

    companion object {
        @JvmStatic
        fun main(argv: Array<String>) {
            val main = Main()
            JCommander.newBuilder()
                    .addObject(main)
                    .build()
                    .parse(*argv)
            main.run()
        }
    }
}