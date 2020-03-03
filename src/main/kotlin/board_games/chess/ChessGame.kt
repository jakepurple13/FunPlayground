package board_games.chess

import DEBUG_MODE
import playingGame
import java.util.*

fun main() = ChessGame.chess()

object ChessGame {
    @Suppress("ConstantConditionIf")
    fun chess() {
        if (DEBUG_MODE) playingGame("Chess")
        val scan = Scanner(System.`in`)
        val b = Board {
            boardUpdate {
                println(it.joinToString("\n") {
                    it.joinToString("|") { "${it.character}${it.y} - ${it.piece?.unicodeSymbol ?: "  "}" }
                })
            }
            pawnPromotion {
                println("Choose a Piece:")
                when (scan.nextLine()) {
                    "P" -> Pawn(it)
                    "R" -> Rook(it)
                    "N" -> Knight(it)
                    "B" -> Bishop(it)
                    "Q" -> Queen(it)
                    else -> null
                }
            }
            checkListen { p, k -> println("$p can take $k") }
            capture { println("$it was ${it.value} points") }
        }
        b.setup()
        println(b.publicBoard.joinToString("\n") {
            it.joinToString("|") { "${it.character}${it.y} - ${it.piece?.unicodeSymbol ?: "  "}" }
        })
        do {
            println("Make your move: (e.g. a1 b1)")
            val move = scan.nextLine().split(" ")
            b.move(move[0] to move[1])
        } while (b.publicBoard.count { it.any { it.piece is King } } == 2)
        /*println(b.getCaptured(Color.BLACK).joinToString { it.unicodeSymbol })
        println(b.getCaptured(Color.WHITE).joinToString { it.unicodeSymbol })
        println(b.getCaptured(Color.BLACK).sumBy { it.value })
        println(b.getCaptured(Color.WHITE).sumBy { it.value })
        println(b.toFEN())*/
    }
}