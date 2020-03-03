package reddit

import AnsiColor
import RunPython
import colorText
import fromJson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.awt.Color
import java.util.*

enum class RedditLevels {
    NEW,
    HOT,
    TOP,
    RISING,
    CONTROVERSIAL,
    BEST;
    companion object {
        operator fun invoke(string: String) = try {
            valueOf(string)
        } catch (e: IllegalArgumentException) {
            NEW
        }
    }
}

object RedditGame {
    fun pythonPlay(vararg args: String): Unit = runBlocking {
        val sub = if (args.contains("no sub")) {
            val scan = Scanner(System.`in`)
            print("Choose a SubReddit: ")
            scan.nextLine()
        } else args[0]
        val python = RunPython.runPythonCodeAsync("latestLatestReddit.py", sub, "new")
        @Suppress("BlockingMethodInNonBlockingContext") val waiting = launch {
            var count = 0
            while (true) {
                printr("Loading${".".repeat(count % 4)}")
                count++
                delay(500)
                System.out.flush()
            }
        }
        waiting.start()
        val pythonString = python.await()
        val waiter = pythonString.fromJson<List<LatestReddit>>()
        waiting.cancel()
        fun LatestReddit.toUsableString() = "$title: Upvotes: $upvotes - $fullUrl"
        waiter?.forEachIndexed { index, latestReddit ->
            println("$index. ${AnsiColor.colorText(latestReddit.toUsableString(), if (index % 2 == 0) Color.CYAN else Color.RED)}")
        }
    }.let { Unit }
}

fun printr(msg: Any?) = print("$msg\r")

data class LatestReddit(val title: String?, val upvotes: Number?, val url: String?) {
    val fullUrl get() = "https://www.reddit.com$url"
}
