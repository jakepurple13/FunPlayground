Suit = {
    'SPADES': "♠",
    'CLUBS': "♣",
    'DIAMONDS': "♦",
    'HEARTS': "♥"
}


class Card:
    def __init__(self, value, suit):
        self.value = value
        self.suit = suit
        self.symbol = {
            1: "A",
            11: "J",
            12: "Q",
            13: "K"
        }.get(self.value, self.value)
        self.value_ten = 10 if self.value > 10 else self.value

    def __str__(self):
        return f"{self.symbol}{self.suit}"

    def __repr__(self):
        return f"{self.symbol}{self.suit}"

    def __eq__(self, other):
        return self.value == other.value and self.suit == other.suit

    def __ne__(self, other):
        return self.value != other.value and self.suit != other.suit
