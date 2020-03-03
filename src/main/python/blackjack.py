import random
import time

Suit = {
    'SPADES': "♠",
    'CLUBS': "♣",
    'DIAMONDS': "♦",
    'HEARTS': "♥"
}


class Card:
    value: int
    suit: Suit
    symbol: str
    value_ten: int

    def __init__(self, value, suit):
        self.value = value
        self.suit = suit
        self.symbol = {
            1: "A",
            11: "J",
            12: "Q",
            13: "K"
        }.get(self.value, self.value)
        if self.value > 10:
            self.value_ten = 10
        else:
            self.value_ten = self.value

    def __str__(self):
        return f"{self.symbol}{self.suit}"

    def __repr__(self):
        return f"{self.symbol}{self.suit}"


class Deck:
    deck = []

    def draw(self):
        return self.deck.pop()

    def __init__(self):
        for i in Suit.values():
            for j in range(1, 13):
                self.deck.append(Card(j, i))
        random.shuffle(self.deck)


def current_value(card_list):
    def take_item(card):
        return card.value_ten

    sorted_list = card_list.copy()
    sorted_list.sort(key=take_item)
    value = 0
    for c in sorted_list:
        if c.value == 1 and value + 11 < 22:
            amount = 11
        elif c.value == 1:
            amount = 1
        else:
            amount = c.value_ten
        value += amount

    return value


def getColoredText(string, r=255, g=255, b=255):
    return f"\u001B[38;2;{r};{g};{b}m{string}\u001B[0m"


def printDealerInfo(dealerCards, r=255, g=255, b=255):
    print(getColoredText(f"Dealer has: {' + '.join(map(lambda c: c.__str__(), dealerCards))} = {current_value(dealerCards)}", r, g, b))


def printPlayerInfo(playerCards, r=255, g=255, b=255):
    print(getColoredText(f"Player has: {' + '.join(map(lambda c: c.__str__(), playerCards))} = {current_value(playerCards)}", r, g, b))


def game():
    deck = Deck()
    highScore = 20
    money = 20
    while money > 0:
        if money > highScore:
            highScore = money
        print(getColoredText("------------------", 0, 0, 0))
        playerTotal = []
        dealerTotal = []
        bet = money + 1
        while bet > money:
            try:
                bet = int(input(getColoredText(f"You have ${money}. Place your bet: ", 0, 255, 255)))
                break
            except ValueError:
                continue
        print(getColoredText(f"You bet ${bet}", 0, 255, 255))
        playerTotal.append(deck.draw())
        dealerTotal.append(deck.draw())
        playerTotal.append(deck.draw())
        printDealerInfo(dealerTotal, 255, 255, 0)
        printPlayerInfo(playerTotal, 0, 255, 255)

        if current_value(playerTotal) == 21:
            print(getColoredText("Player got 21!", 0, 255, 255))
            money += bet * 1.5
            continue

        while current_value(playerTotal) <= 21:
            hitStay = input(getColoredText("(H)it or (S)tay: ", 0, 255, 255))
            if hitStay.lower() == "hit" or hitStay.lower() == "h":
                playerTotal.append(deck.draw())
                printPlayerInfo(playerTotal, 0, 255, 255)
            elif hitStay.lower() == "stay" or hitStay.lower() == "s":
                break

        print(getColoredText("Dealer's turn", 255, 255, 0))

        while current_value(dealerTotal) <= 16 and current_value(playerTotal) <= 21:
            dealerTotal.append(deck.draw())
            printDealerInfo(dealerTotal, 255, 255, 0)
            time.sleep(.5)
            if len(dealerTotal) == 2 and current_value(dealerTotal) == 21:
                print(getColoredText("Dealer got 21!", 255, 255, 0))
                money -= bet
                continue

        player = current_value(playerTotal)
        dealer = current_value(dealerTotal)
        if player > dealer or (dealer > 21 and player <= 21):
            print(getColoredText("Player Wins!", 0, 255, 0))
            money += bet
        elif dealer >= player or (dealer <= 21 and player > 21):
            print(getColoredText("Dealer Wins!", 255, 0, 0))
            money -= bet

    print(getColoredText(f"You are out of money. Your high score was {highScore}. Game over.", 255, 0, 0))


game()
