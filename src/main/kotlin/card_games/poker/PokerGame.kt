package card_games.poker

import AnsiColor
import DEBUG_MODE
import FlowItem
import FrameType
import card_games.Card
import card_games.Deck
import color
import colorText
import frame
import kotlinx.coroutines.*
import minusAssign
import playingGame
import plusAssign
import java.awt.Color
import java.util.*
import kotlin.math.absoluteValue

fun main() = runBlocking {
    PokerGame.playPoker(1, continuePlay = true, jacksOrBetter = false)
}

private fun Scores.printScores() = println(values.frame(FrameType.BOX.copy(top = "Scores")))
private fun Scores.possibleWinnings(hand: List<Card>, bet: Int) = println(ansiValues(getWinningHand(hand), bet))

private enum class Continue {
    YES, NO;

    companion object {
        operator fun invoke(s: String) = when (s.toUpperCase()) {
            "Y", "YES" -> YES
            "N", "NO" -> NO
            else -> null
        }
    }
}

private class Player {
    val money = FlowItem(20)
    val hand = mutableListOf<Card>()
}

private fun launch(block: suspend CoroutineScope.() -> Unit) = GlobalScope.launch(block = block)

private enum class PokerPlay {
    CONTINUE, STOP;

    companion object {
        operator fun invoke(s: String) = when (s.toUpperCase()) {
            "STOP", "S" -> STOP
            "CONTINUE", "C" -> CONTINUE
            else -> null
        }
    }
}

private fun List<Card?>.toSymbol() = joinToString { if (it == null) "[]" else "${it.symbol}${it.suit.unicodeSymbol}".color(Color.CYAN) }

object PokerGame {
    @Suppress("ConstantConditionIf")
    suspend fun playPoker(deckSize: Int, continuePlay: Boolean, jacksOrBetter: Boolean) {
        if (DEBUG_MODE) playingGame("Poker", "-decks $deckSize decks", "-continuePlay $continuePlay")
        val scan = Scanner(System.`in`)
        val scores = Scores()
        scores.jacksOrBetter = jacksOrBetter
        var canContinue = true
        val player = Player()

        scores.printScores()
        println("Welcome to Poker!")
        println("You are starting with ${"\$${player.money.getValue()}".color(Color.GREEN)}")

        launch {
            player.money.collect {
                if (it < 0) {
                    println("You're out of money".color(Color.RED))
                    canContinue = false
                } else println("You have ${"\$$it".color(Color.GREEN)}")
            }
        }

        val deck = Deck<Card>()
        repeat(deckSize) { deck.addDeck(Deck.defaultDeck()) }
        deck.trueRandomShuffle()
        deck.addDeckListener {
            onDraw { _, i -> if (i <= 5 && continuePlay) repeat(deckSize) { deck(Deck.defaultDeck()) }.also { deck.trueRandomShuffle() } }
        }

        gameLoop@ while (canContinue) {
            println(AnsiColor.colorText("-".repeat(50), Color.cyan))
            if (player.money.getValue() <= 0) break@gameLoop
            player.hand.clear()
            var currentBet = 0
            betLoop@ do {
                println("How much would you like to bet? ${"(1-5)".color(Color.GREEN)}")
                currentBet = scan.nextLine().toIntOrNull() ?: continue@betLoop
            } while (currentBet !in 1..5)

            fun playerHand() {
                scores.possibleWinnings(player.hand, currentBet)
                scores.getWinningHand(player.hand).let {
                    if (it == PokerHand.NOTHING) println("You: ${player.hand.toSymbol()}")
                    else println("You have a ${it.stringName.color(Color.CYAN)}: ${player.hand.toSymbol()}")
                }
            }

            player.money -= currentBet
            player.hand.addAll(deck.draw(5))

            playerHand()

            println(
                "Choose what ${"cards".color(Color.ORANGE)} to ${"discard".color(Color.RED)} via ${"index".color(Color.YELLOW)}. " +
                        "${"(Start at 1-5)".color(Color.CYAN)} " +
                        "(Or type \"(${"S".color(Color.RED)})top\" to stop ${"discarding".color(Color.RED)})"
            )

            var choice = PokerPlay.CONTINUE
            val tempHand: MutableList<Card?> = player.hand.toMutableList()
            println(tempHand.toSymbol())
            do {
                val choose = scan.nextLine()
                PokerPlay(choose)?.let { choice = it } ?: choose.toIntOrNull()?.let { if (it in 1..5) tempHand[it - 1] = null } ?: continue
                println(tempHand.toSymbol())
            } while (choice != PokerPlay.STOP && !tempHand.all { it == null })

            tempHand.removeIf { it == null }
            tempHand.addAll(deck.draw(5 - tempHand.size))
            player.hand.clear()
            player.hand.addAll(tempHand.filterNotNull())

            playerHand()
            delay(500)

            val winnings = scores.winCheck(player.hand, currentBet)
            println("You ${if (winnings > 0) "won" else "lost"} \$${winnings.absoluteValue}".color(if (winnings > 0) Color.GREEN else Color.RED))
            player.money += if (winnings > 0) winnings else 0
            println("Would you like to keep playing? (${"Y".color(Color.GREEN)})es/(${"N".color(Color.RED)})o?")
            var playing: Continue?
            do playing = Continue(scan.nextLine()) while (playing == null)
            when (playing) {
                Continue.YES -> continue@gameLoop
                Continue.NO -> break@gameLoop
            }
        }
    }
}