package card_games.matching

import card_games.Card
import card_games.Suit
import color
import kotlinx.coroutines.runBlocking
import utils.Colors
import utils.stringForTime
import java.awt.Color
import java.util.*
import kotlin.random.Random
import kotlin.system.measureTimeMillis

fun main() {
    MatchingGame.playMatching(Suit.SPADES)
}

object MatchingGame {

    fun playMatching(suit: Suit) = runBlocking {
        val time = measureTimeMillis { matching(suit) }
        println("The game took ${time.stringForTime()}")
    }.let { Unit }

    private fun <T> MutableList<T>.randomRemove(): T = removeAt(Random.nextInt(size))
    private fun Card.toSymbol() = "${symbol}${suit.unicodeSymbol}"
    data class Cell(val x: Int, val y: Int, var card: Card, var flipped: Boolean) {
        var matched = false
        fun getCard() = if (flipped || matched) card.toSymbol().color(if (flipped) Color.GREEN else Color.CYAN) else "[]"
        fun flip() = run { flipped = !flipped }
    }

    private fun untilInt(scanner: Scanner, range: IntRange, print: () -> Unit = {}): Int {
        var temp: Int?
        do {
            print()
            temp = scanner.nextLine().toIntOrNull()
            if (temp != null && temp !in range) temp = null
        } while (temp == null)
        return temp
    }

    private fun Random.nextColor() = Colors.RGB(nextInt(0, 255), nextInt(0, 255), nextInt(0, 255)).let { Color(it.r, it.g, it.b) }

    private fun matching(suit: Suit) {
        val scan = Scanner(System.`in`)
        val deck = ((1..13).map { value -> Card(value, Suit.SPADES) } + (1..13).map { value -> Card(value, suit) }).toMutableList()
        val field = Array(2) { x -> Array(13) { y -> Cell(x, y, deck.randomRemove(), false) } }
        fun showField() = println(field.joinToString("\n") { it.joinToString { c -> c.getCard() } })
        fun getCell(): Cell {
            val x = untilInt(scan, 1..2) { println("Enter an ${"x (1-2)".color(Color.ORANGE)}: ") } - 1
            val y = untilInt(scan, 1..13) { println("Enter a ${"y (1-13)".color(Color.ORANGE)}: ") } - 1
            return field[x][y]
        }
        gameLoop@ while (field.any { it.any { c -> !c.matched } }) {
            println("-".repeat(50).color(Random.nextColor()))
            showField()
            val matcher: Cell = getCell()
            matcher.flip()
            showField()
            var matchee: Cell
            do matchee = getCell() while (matchee.x == matcher.x && matchee.y == matcher.y)
            matchee.flip()
            showField()
            Thread.sleep(500)
            if (matcher.card.value == matchee.card.value && matcher.card.suit == matchee.card.suit) {
                matcher.matched = true
                matchee.matched = true
            }
            if (!matcher.matched && !matchee.matched) {
                matcher.flip()
                matchee.flip()
            }
        }
    }
}