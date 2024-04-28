package models

import (
	"math/rand"
)

type Deck struct {
	Cards []Card `json:"cards"`
}

type Card struct {
	Value int    `json:"value"`
	Suit  Suit   `json:"suit"`
}

type Hand struct {
	Cards []Card `json:"cards"`
}

type CenterPile struct {
	Cards []Card `json:"cards"`
}

type Suit string

const (
	Spades   Suit = "SPADES"
	Hearts   Suit = "HEARTS"
	Diamonds Suit = "DIAMONDS"
	Clubs    Suit = "CLUBS"
)

func NewDeck() Deck {
	cards := make([]Card, 0)
	for i := 1; i <= 13; i++ {
		cards = append(cards, Card{Value: i, Suit: Spades})
		cards = append(cards, Card{Value: i, Suit: Hearts})
		cards = append(cards, Card{Value: i, Suit: Diamonds})
		cards = append(cards, Card{Value: i, Suit: Clubs})
	}
	return Deck{Cards: cards}
}

func (d Deck) Shuffle() {
	for i := range d.Cards {
		j := rand.Intn(i + 1)
		d.Cards[i], d.Cards[j] = d.Cards[j], d.Cards[i]
	}
}

func (d *Deck) DrawCard() Card {
	card := d.Cards[0]
	d.Cards = d.Cards[1:]
	return card
}

func (d *Deck) DrawCards(n int) []Card {
	cards := make([]Card, n)
	for i := 0; i < n; i++ {
		cards[i] = d.DrawCard()
	}
	return cards
}

func (c *CenterPile) VerifyPattern () bool {
	if len(c.Cards) < 3 {
		return false
	}
	if c.Cards[len(c.Cards) - 1].Value == c.Cards[len(c.Cards) - 2].Value && c.Cards[len(c.Cards) - 2].Value == c.Cards[len(c.Cards) - 3].Value {
		return true
	}
	if c.Cards[len(c.Cards) - 1].Value != c.Cards[len(c.Cards) - 2].Value && c.Cards[len(c.Cards) - 2].Value != c.Cards[len(c.Cards) - 3].Value && c.Cards[len(c.Cards) - 1].Value != c.Cards[len(c.Cards) - 3].Value {
		return true
	}
	return false
}

func (c *CenterPile) CheckSlappable() bool {
    // Ensure there are enough cards in the pile to check slappable conditions
    if len(c.Cards) < 2 {
        return false // Not enough cards to check any condition
    }

    lastCard := c.Cards[len(c.Cards) - 1]

    // Double: same value in a row e.g., 5,5
    if len(c.Cards) > 1 && lastCard.Value == c.Cards[len(c.Cards) - 2].Value {
        return true
    }

    // Additional checks require more cards
    if len(c.Cards) < 3 {
        return false
    }

    // Sandwich: same value with one card in between e.g., 5,7,5
    if lastCard.Value == c.Cards[len(c.Cards) - 3].Value {
        return true
    }

    // Tens sandwich: tens but with one letter card in between
    if lastCard.Value + c.Cards[len(c.Cards) - 3].Value == 10 {
        return true
    }

    // Top bottom: last card = first card
    if lastCard.Value == c.Cards[0].Value && lastCard.Suit == c.Cards[0].Suit {
        return true
    }

    // Tens: two in a row add up to 10; ace counts as 1 for this check
    if lastCard.Value + c.Cards[len(c.Cards) - 2].Value == 10 {
        return true
    }

    // Check for sequences of four in a row, both ascending and descending
    if len(c.Cards) < 4 {
        return false
    }

    if checkSequence(c.Cards, true) || checkSequence(c.Cards, false) {
        return true
    }

    // Marriage: Q,K or K,Q
    if (lastCard.Value == 12 && c.Cards[len(c.Cards) - 2].Value == 13) ||
       (lastCard.Value == 13 && c.Cards[len(c.Cards) - 2].Value == 12) {
        return true
    }

    return false
}

func checkSequence(cards []Card, ascending bool) bool {
    for i := 1; i < 4; i++ {
        current := cards[len(cards)-i-1]
        next := cards[len(cards)-i]
        expectedNextValue := current.Value
        if ascending {
            expectedNextValue++
            if expectedNextValue > 13 { expectedNextValue = 1 }
        } else {
            expectedNextValue--
            if expectedNextValue < 1 { expectedNextValue = 13 }
        }
        if next.Value != expectedNextValue {
            return false
        }
    }
    return true
}

func (c *CenterPile) CheckFaceCardChallenge() (bool, bool) {
	if len(c.Cards) < 2 {
		return false, false
	}
	lastCard := c.Cards[len(c.Cards) - 1]
	secondToLastCard := c.Cards[len(c.Cards) - 2]

	if isFaceOrAce(lastCard) && isFaceOrAce(secondToLastCard) {
		return true, true
	} else if isFaceOrAce(lastCard) {
		return true, false
	} else {
		return false, false
	}
}


func isFaceOrAce(card Card) bool {
	switch card.Value {
	case 11, 12, 13, 1:
		return true
	default:
		return false
	}
}