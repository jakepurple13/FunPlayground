import kotlinx.coroutines.*
import reddit.RedditGame.pythonPlay
import java.awt.Color
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import java.util.stream.Collectors

fun main() = runBlocking {
    pythonPlay()
}.let { Unit }

object RunPython {
    @Suppress("BlockingMethodInNonBlockingContext")
    fun runPythonCodeAsync(fileName: String, vararg args: String) = GlobalScope.async {
        val command = "python3 src/main/python/$fileName ${args.joinToString(" ")}"
        val process = Runtime.getRuntime().exec(command)
        process.waitFor()
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        reader.lines().collect(Collectors.joining("\n"))
    }
}