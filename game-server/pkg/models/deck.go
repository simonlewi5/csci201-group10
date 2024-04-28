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
	// TODO: verify that value checks are using correct numbers for face cards
	card := c.Cards[len(c.Cards) - 1]

	// Double: same value in a row e.g. 5,5
	if card.Value == c.Cards[len(c.Cards) - 2].Value { return true }

	// Sandwich: same value with 1 card in between e.g. 5,7,5
	if card.Value == c.Cards[len(c.Cards) - 3].Value { return true }

	// Top bottom: last card = first card
	if card.Value == c.Cards[0].Value && card.Suit == c.Cards[0].Suit { return true }

	// Tens: two in a row add up to 10; ace counts as 1 for this
	// are aces already valued at 1? if not, need to do extra check here
	if card.Value + c.Cards[len(c.Cards) - 2].Value == 10 { return true }

	// Tens sandwich: tens but with 1 letter card in between
	if card.Value + c.Cards[len(c.Cards) - 3].Value == 10 { return true }

	// Jokers: joker = slap
	// are we using jokers? if so need to implement this

	// Four in a row: 4 ascending in a row (includes wrapping) e.g. Q,K,A,2
	isFour := true
	for i := 4; i >= 2; i-- {
		tempCard := c.Cards[len(c.Cards) - i]
		nextVal := tempCard.Value + 1
		if nextVal > 13 { nextVal = 1 }

		if c.Cards[len(c.Cards) - i + 1].Value != nextVal {
			isFour = false
			break
		}
	}
	if isFour { return true }

	// Four in a row: descending
	isFour = true
	for i := 4; i >= 2; i-- {
		tempCard := c.Cards[len(c.Cards) - i]
		nextVal := tempCard.Value - 1
		if nextVal < 1 { nextVal = 13 }

		if c.Cards[len(c.Cards) - i + 1].Value != nextVal {
			isFour = false
			break
		}
	}
	if isFour { return true }

	// Marriage: Q,K or K,Q
	// need to confirm that Q = 12 and K = 13
	if card.Value == 12 && c.Cards[len(c.Cards) - 2].Value == 13 { return true }
	if card.Value == 13 && c.Cards[len(c.Cards) - 2].Value == 12 { return true }

	return false;
}