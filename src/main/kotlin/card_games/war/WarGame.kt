@file:Suppress("ConstantConditionIf")

package card_games.war

import AnsiColor
import DEBUG_MODE
import card_games.Card
import card_games.Deck
import card_games.DeckException
import kotlinx.coroutines.runBlocking
import playingGame
import utils.Colors
import utils.stringForTime
import java.awt.Color
import java.util.*
import kotlin.random.Random
import kotlin.system.measureTimeMillis

fun main() {
    WarGame.playWar()
}

object WarGame {

    fun playWar() = runBlocking {
        val time = measureTimeMillis { warGame() }
        println("The game took ${time.stringForTime()}")
    }.let { Unit }

    private fun AnsiColor.colorText(text: String, color: Color) = colorText(text, color.rgb)
    private fun String.color(color: Color) = AnsiColor.colorText(this, color)
    private fun Card.toSymbol() = "${symbol}${suit.unicodeSymbol}"
    private fun Random.nextColor() = Colors.RGB(nextInt(0, 255), nextInt(0, 255), nextInt(0, 255)).let { Color(it.r, it.g, it.b) }

    enum class Players { PLAYER, COMPUTER, NO_ONE }

    private val compName = "Computer".color(Color.GREEN)
    private val playerName = "Player".color(Color.CYAN)

    private fun warGame() {
        if (DEBUG_MODE) playingGame("War")
        val mainDeck = Deck.defaultDeck()
        mainDeck.trueRandomShuffle()
        val playerDeck = Deck(mainDeck.draw(26))
        val computerDeck = Deck(mainDeck.draw(26))
        var playerScore = 0
        var compScore = 0
        gameLoop@ while (playerDeck.isNotEmpty && computerDeck.isNotEmpty) {
            println("-".repeat(50).color(Random.nextColor()))
            val comp = computerDeck.draw()
            val player = playerDeck.draw()
            when (scoreCheck(player, comp, playerDeck, computerDeck)) {
                Players.PLAYER -> {
                    println("$playerName won!")
                    playerScore++
                }
                Players.COMPUTER -> {
                    println("$compName won!")
                    compScore++
                }
                Players.NO_ONE -> Unit
            }
            println("Score is: $playerName: $playerScore | $compName: $compScore")
            println("$playerName cards: ${playerDeck.size.toString().color(Color.YELLOW)}")
            println("$compName cards: ${computerDeck.size.toString().color(Color.YELLOW)}")
            Thread.sleep(500)
            require(computerDeck.size + playerDeck.size == 52) { "Something went wrong! ${computerDeck.size + playerDeck.size}" }
        }
        val winner = if (playerDeck.isNotEmpty) playerName else if (computerDeck.isNotEmpty) compName else "No one"
        println("$playerName cards: ${playerDeck.size}")
        println("$compName cards: ${computerDeck.size}")
        println("Final Score is: $playerName: $playerScore | $compName: $compScore | $winner won!")
    }

    private fun scoreCheck(playerCard: Card, computerCard: Card, playerDeck: Deck<Card>, computerDeck: Deck<Card>): Players {
        println("$compName played: ${computerCard.toSymbol().color(Color.RED)}")
        println("$playerName played: ${playerCard.toSymbol().color(Color.GREEN)}")
        val playedCards = listOf(playerCard, computerCard)
        if (computerCard.value < playerCard.value || (playerCard.value == 1 && computerCard.value != 1)) {
            playerDeck += playedCards
            return Players.PLAYER
        } else if (computerCard.value > playerCard.value || (playerCard.value != 1 && computerCard.value == 1)) {
            computerDeck += playedCards
            return Players.COMPUTER
        } else if (computerCard.value == playerCard.value) {
            println("WAR!".color(Color.RED))
            val value = if (playerCard.value == 1) 14 else playerCard.value
            fun getCards(deck: Deck<Card>): MutableList<Card> {
                val cards = mutableListOf<Card>()
                warLoop@ for (i in 1 until value) {
                    try {
                        cards += deck.draw()
                    } catch (e: DeckException) {
                        break@warLoop
                    }
                }
                return cards
            }

            val play = getCards(playerDeck)
            println("$playerName has placed down ${play.size} cards")
            val com = getCards(computerDeck)
            println("$compName has placed down ${com.size} cards")
            val pCard = try {
                if (playerDeck.isEmpty) play.removeAt(play.lastIndex) else playerDeck.draw()
            } catch (e: IndexOutOfBoundsException) {
                playerCard
            }
            val cCard = try {
                if (computerDeck.isEmpty) com.removeAt(com.lastIndex) else computerDeck.draw()
            } catch (e: IndexOutOfBoundsException) {
                computerCard
            }
            val allCards = listOf(play, com, playedCards).flatten()
            val score = scoreCheck(pCard, cCard, playerDeck, computerDeck)
            when (score) {
                Players.PLAYER -> playerDeck += allCards
                Players.COMPUTER -> computerDeck += allCards
                Players.NO_ONE -> Unit
            }
            return score
        } else {
            println("It was a tie!".color(Color.ORANGE))
            return Players.NO_ONE
        }
    }
}