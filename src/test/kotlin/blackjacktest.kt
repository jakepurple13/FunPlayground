import java.util.*

enum class Suit(val symbol: String) {
    SPADES("♠"),
    CLUBS("♣"),
    DIAMONDS("♦"),
    HEARTS("♥")
}

class Card(val value: Int, private val suit: Suit) {
    val valueTen = if (value > 10) 10 else value
    private val symbol = when (value) {
        1 -> "A"
        11 -> "J"
        12 -> "Q"
        13 -> "K"
        else -> "$value"
    }

    override fun toString(): String = "$symbol${suit.symbol}"
}

class Deck {
    private val deck = mutableListOf<Card>().apply {
        for (s in Suit.values())
            for (v in 1..13)
                add(Card(v, s))
        shuffle()
    }

    fun draw() = deck.removeAt(deck.lastIndex)
}

fun List<Card>.total() =
    sortedByDescending(Card::value).fold(0) { total, c -> total + if (c.value == 1 && total + 11 < 22) 11 else if (c.value == 1) 1 else c.valueTen }

fun getColoredText(string: String, r: Int, g: Int, b: Int) = "\u001B[38;2;$r;$g;${b}m$string\u001B[0m"

fun printPlayerInfo(playerCards: List<Card>, r: Int, g: Int, b: Int) = printInfo("Player", playerCards, r, g, b)
fun printDealerInfo(dealerCards: List<Card>, r: Int, g: Int, b: Int) = printInfo("Dealer", dealerCards, r, g, b)
fun printInfo(type: String, cards: List<Card>, r: Int, g: Int, b: Int) =
    println(getColoredText("$type has: ${cards.joinToString(" + ")} = ${cards.total()}", r, g, b))

fun main() {
    val scan = Scanner(System.`in`)
    val deck = Deck()
    var highScore = 20
    var money = 20
    gameLoop@ while (money > 0) {
        if (money > highScore) highScore = money
        println(getColoredText("------------------", 0, 0, 0))
        val playerTotal = mutableListOf<Card>()
        val dealerTotal = mutableListOf<Card>()
        var bet = money + 1
        betLoop@ while (bet > money) {
            try {
                print(getColoredText("You have \$$money. Place your bet: ", 0, 255, 255))
                bet = scan.nextLine().toInt()
            } catch (e: Exception) {
            }
        }
        println(getColoredText("You bet \$$bet", 0, 255, 255))
        playerTotal += (deck.draw())
        dealerTotal += (deck.draw())
        playerTotal += (deck.draw())
        printDealerInfo(dealerTotal, 255, 255, 0)
        printPlayerInfo(playerTotal, 0, 255, 255)

        if (playerTotal.total() == 21) {
            println(getColoredText("Player got 21!", 0, 255, 255))
            money += (bet * 1.5).toInt()
            continue@gameLoop
        }

        playerLoop@ while (playerTotal.total() <= 21) {
            print(getColoredText("(H)it or (S)tay: ", 0, 255, 255))
            val hitStay = scan.nextLine().toLowerCase()
            if (hitStay == "hit" || hitStay == "h") {
                playerTotal += (deck.draw())
                printPlayerInfo(playerTotal, 0, 255, 255)
            } else if (hitStay == "stay" || hitStay == "s") break@playerLoop
        }
        val player = playerTotal.total()
        println(getColoredText("Dealer's turn", 255, 255, 0))
        dealerLoop@ while (dealerTotal.total() <= 16 && player <= 21) {
            Thread.sleep(500)
            dealerTotal += (deck.draw())
            printDealerInfo(dealerTotal, 255, 255, 0)
            if (dealerTotal.size == 2 && dealerTotal.total() == 21) {
                println(getColoredText("Dealer got 21!", 255, 255, 0))
                money -= bet
                continue@gameLoop
            }
        }
        val dealer = dealerTotal.total()
        if (player in (dealer + 1)..21 || (dealer > 21 && player <= 21)) {
            println(getColoredText("Player Wins!", 0, 255, 0))
            money += bet
        } else if (dealer in player..21 || (dealer <= 21 && player > 21)) {
            println(getColoredText("Dealer Wins!", 255, 0, 0))
            money -= bet
        }
    }
    println(getColoredText("You are out of money. Your high score was $highScore. Game over.", 255, 0, 0))
}