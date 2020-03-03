import Deck as Deck
import Card as Card

if __name__ == '__main__':
    deck = Deck.default_deck(shuffle=True)
    print(deck.size())
    deck += Card.Card(1, Card.Suit['SPADES'])
    # c = deck.draw()
    # print(c)
    deck.print_deck()
    # cards = deck.draw_cards(5)
    # print(cards)
    print(deck.random_card())
    deck.print_deck()
    print(deck.draw_random_card())
    deck.print_deck()
    deck -= Card.Card(1, Card.Suit['SPADES'])
    deck.print_deck()
    print(Card.Card(1, Card.Suit['SPADES']) in deck)
    deck.shuffle()
    deck.print_deck()
    deck.add_deck(Deck.default_deck())
    deck.print_deck()
