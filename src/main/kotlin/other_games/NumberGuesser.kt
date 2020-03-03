package other_games

import java.util.*

fun main() {
    NumberGuesser.number()
}

object NumberGuesser {
    private fun untilInt(scanner: Scanner, print: () -> Unit = {}): Int {
        var temp: Int?
        do temp = print().let { scanner.nextLine().toIntOrNull() } while (temp == null)
        return temp
    }

    fun number(start: Int = 1, end: Int = 20) {
        val scan = Scanner(System.`in`)
        val range = start..end
        val number = range.random()
        var count = 0
        println("I have a number between ${range.first} and ${range.last}")
        do {
            val guess = untilInt(scan) { println("Guess my number! ") }
            count++
        } while (guess != number)
        println("You got it! It was $number. It took $count tr${if (count == 1) "y" else "ies"}.")
    }
}