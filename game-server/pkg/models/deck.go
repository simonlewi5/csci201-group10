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