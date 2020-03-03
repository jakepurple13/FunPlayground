import random
import Card as Card


class Deck:
    def __init__(self):
        self._deck = list()

    def draw(self) -> Card:
        return self._deck.pop()

    def size(self) -> int:
        return len(self._deck)

    def shuffle(self):
        random.shuffle(self._deck)

    def add_card(self, card: Card):
        self._deck.append(card)

    def add_cards(self, *argv: Card):
        for c in argv:
            self._deck.append(c)

    # noinspection PyProtectedMember
    def add_deck(self, deck):
        self.add_cards(*deck._deck)

    def draw_cards(self, amount: int):
        cards = list()
        for i in range(0, amount):
            cards.append(self.draw())
        return cards

    def random_card(self):
        return random.choice(self._deck)

    def draw_random_card(self):
        c = random.choice(self._deck)
        self._deck.remove(c)
        return c

    def __iadd__(self, other: Card):
        self.add_card(other)
        return self

    def __isub__(self, other: Card):
        self._deck.remove(other)
        return self

    def __contains__(self, item: Card):
        return item in self._deck

    def print_deck(self):
        print(self._deck)


def default_deck(shuffle: bool = False) -> Deck:
    d = Deck()
    for i in Card.Suit.values():
        for j in range(1, 14):
            d.add_card(Card.Card(j, i))
    if shuffle:
        d.shuffle()
    return d
