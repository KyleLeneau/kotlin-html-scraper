import com.beust.jcommander.JCommander
import com.github.kittinunf.fuel.httpGet
import org.jsoup.Jsoup
import org.jsoup.select.Elements

internal class Main {
    private val pageToParse = "http://www.charleswysockipuzzles.com/Wysocki/Master-Checklist.aspx"

    fun run() {
        getHtmlResponse {
            findElements(it) {
                parseElements(it) {
                    // TODO: print these out to JSON instead
                    print(it.toString())
                }
            }
        }
    }

    private fun getHtmlResponse(then: (String) -> Unit) {
        val (_, _, result) = pageToParse.httpGet().responseString()
        then(result.get())
    }

    private fun findElements(html: String, then: (Elements) -> Unit) {
        val elements = Jsoup.parse(html)
                .select("#MainContent_lblPuzzles")
                .select("div.4u")
        then(elements)
    }

    private fun parseElements(elements: Elements, then: (Array<Puzzle>) -> Unit) {
        val results = elements.mapNotNull { it.toPuzzle() }
        then(results.toTypedArray())
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