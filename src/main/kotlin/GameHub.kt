@file:Suppress("ConstantConditionIf")

import board_games.chess.ChessGame
import card_games.Suit
import card_games.blackjack.BlackjackGame
import card_games.matching.MatchingGame
import card_games.poker.PokerGame
import card_games.war.WarGame
import kotlinx.coroutines.runBlocking
import reddit.RedditGame
import reddit.RedditLevels

const val DEBUG_MODE = false

fun main(vararg args: String) = runBlocking {
    if (DEBUG_MODE) {
        println(args.toList())
        /*mainGame("-g", "poker", "-decks", "5", "-continuePlay", "true")
        mainGame("-g", "blackjack", "-decks", "5", "-continuePlay", "true")
        mainGame("-g", "reddit", "-subreddit", "kotlin", "-level", "hot")
        mainGame("-g", "chess")*/
        mainGame()
    } else mainGame(*args)
}.let { Unit }

fun Games.Companion.choices(vararg games: Games) = games.joinToString(", ", prefix = "[", postfix = "]") { it.name.toLowerCase() }

private fun printHelpScreen() {
    val redditLevels = RedditLevels.values().joinToString(", ", prefix = "[", postfix = "]") { it.name.toLowerCase() }
    """
Help Screen
    -h -> Brings up this screen
    -g -> Game selector. Choices are: ${Games.choices(*Games.values().filter { it == Games.HELP }.toTypedArray())}
    -subreddit -> For ${Games.choices(Games.REDDIT)}. Choose what subreddit to get data from.
    -level -> For ${Games.choices(Games.REDDIT)}. Choose the level. $redditLevels. Default is new.
    -decks -> For ${Games.choices(Games.POKER, Games.BLACKJACK)}. # of decks to play with. Default is 1 and anything below 1 will be 1.
    -suit -> For ${Games.choices(Games.MATCHING)}. Choose a suit. Choices are ${Suit.values()
        .joinToString(", ", prefix = "[", postfix = "]") { it.name.toLowerCase() }}. Default is Spades.
    -jacks -> For ${Games.choices(Games.POKER)}. Play with pairs needing to be jacks or better. Default is false.
    -continuePlay -> For ${Games.choices(
        Games.POKER, Games.BLACKJACK
    )}. If you want to continue to play after the deck is empty. Default is false for poker, true for blackjack.
""".trimIndent().let { println(it) }
}

enum class Games {
    POKER,
    BLACKJACK,
    MATCHING,
    WAR,
    CHESS,
    REDDIT,
    HELP;

    companion object {
        operator fun invoke(string: String) = try {
            valueOf(string.toUpperCase())
        } catch (e: IllegalArgumentException) {
            HELP
        }
    }
}

fun playingGame(type: String, vararg otherOptions: String) = println("Playing $type with \n${otherOptions.joinToString("\n")}")

fun mainGame(vararg args: String) = runBlocking {
    val choice = Games(
        try {
            if (args[0] == "-g") args[1] else throw Exception("Help")
        } catch (e: Exception) {
            if (DEBUG_MODE) e.printStackTrace()
            "help"
        }
    )
    when (choice) {
        Games.CHESS -> ChessGame.chess()
        Games.POKER -> PokerGame.playPoker(args.deckCount(), args.continuePlay(false), args.jacksOrBetter())
        Games.BLACKJACK -> BlackjackGame.blackjack(args.deckCount(), args.continuePlay(true))
        Games.MATCHING -> MatchingGame.playMatching(args.suit())
        Games.REDDIT -> RedditGame.pythonPlay(args.subreddit(), args.subLevel().name.toLowerCase())
        Games.WAR -> WarGame.playWar()
        Games.HELP -> printHelpScreen()
    }
}

//card functions
fun Array<out String>.deckCount(defaultValue: Int = 1) =
    indexOf("-decks").let { if (it == -1) null else this[it + 1].toIntOrNull() }?.let { if (it <= 0) null else it } ?: defaultValue

fun Array<out String>.continuePlay(defaultValue: Boolean) = indexOf("-continuePlay").let { if (it == -1) defaultValue else this[it + 1].toBoolean() }
fun Array<out String>.jacksOrBetter() = indexOf("-jacks").let { if (it == -1) false else this[it + 1].toBoolean() }
fun Array<out String>.suit() = indexOf("-suit").let { if (it == -1) null else toSuit(this[it + 1].toUpperCase()) } ?: Suit.SPADES

private fun toSuit(string: String) = try {
    Suit.valueOf(string)
} catch (e: Exception) {
    null
}

//reddit functions
fun Array<out String>.subreddit() = indexOf("-subreddit").let { if (it == -1) null else this[it + 1] } ?: "no sub"
fun Array<out String>.subLevel() = RedditLevels(indexOf("-level").let { if (it == -1) "nope" else this[it + 1] })