import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import reddit.printr
import utils.Colors
import java.awt.Color
import kotlin.random.Random

fun mains() {
    println(0x00EBEB.valueOf())
    println(Color.yellow.rgb.valueOf())
    Loged.p("Hello")
    Loged.p("World".color(LogColors.DEBUG))
}

fun Int.valueOf(): Triple<Int, Int, Int> {
    val r = (this shr 16 and 0xff)// / 255.0f
    val g = (this shr 8 and 0xff)// / 255.0f
    val b = (this and 0xff)// / 255.0f
    return Triple(r, g, b)
}

private fun Random.nextColor() = Colors.RGB(nextInt(0, 255), nextInt(0, 255), nextInt(0, 255)).let { Color(it.r, it.g, it.b) }

fun main() {
    for (i in 0..100) {
        println(Random.nextColor())
    }
}

fun mained() = runBlocking {
    val shows = RunPython.runPythonCodeAsync("showapistuff.py")
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
    val pythonString = shows.await()
    val waiter = pythonString.fromJson<Shows>()
    waiting.cancel()
    println(waiter?.shows?.size)
    waiter?.shows?.forEach { println(it) }
}.let { Unit }

data class Shows(val shows: List<Show>)
data class Show(val name: String?, val url: String?)