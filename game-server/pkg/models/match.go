package models

import (
	"crypto/rand"
	"fmt"
	"math/big"
	mrand "math/rand"
	"sync"
	"time"
)

type MatchState string

const (
	MatchStateInit       MatchState = "INIT"
	MatchStateWaiting    MatchState = "WAITING"
	MatchStateInProgress MatchState = "IN_PROGRESS"
	MatchStateComplete   MatchState = "COMPLETE"
)

type Match struct {
	ID                    string          `json:"id"`
	Players               []*Player       `json:"players"`
	Hands                 map[string]Hand `json:"hands"` // map of player username to hand
	MatchState            MatchState      `json:"match_state"`
	TurnIndex             int             `json:"turn_index"`
	Winner                Player          `json:"winner"`
	StartTime             int64           `json:"start_time"`
	EndTime               int64           `json:"end_time"`
	Deck                  Deck            `json:"deck"`
	CenterPile            CenterPile      `json:"center_pile"`
	LastSuccessfulSlapper string          `json:"last_successful_slapper"` // usernme of latest successful slapper
	DeadMansSlaps         map[string]int  `json:"dead_mans_slaps"`         // map username -> number of slaps they can do with a no cards left
	GameLock              sync.Mutex
}

type MatchRequest struct {
	PlayerID string `json:"player_id"`
}

type MatchResponse struct {
	Match Match `json:"match"`
}

type MatchUpdate struct {
	Match Match `json:"match"`
}

type MatchEndRequest struct {
	MatchID string `json:"match_id"`
	Winner  Player `json:"winner"`
}

type MatchEndResponse struct {
	Match Match `json:"match"`
}

type MatchEndUpdate struct {
	Match Match `json:"match"`
}

func NewMatch(players []*Player) *Match {
	deck := NewDeck()
	deck.Shuffle()

	matchID := generateMatchID()

	firstPlayerIdx, err := getRandomIndex(len(players))
	if err != nil {
		fmt.Println(err)
	}

	playerHands := make(map[string]Hand)

	match := &Match{
		ID:         matchID,
		Players:    players,
		Hands:      playerHands,
		MatchState: MatchStateInProgress,
		TurnIndex:  firstPlayerIdx,
		//Winner is set to an empty player to indicate that the match is still in progress
		Winner:    Player{},
		StartTime: time.Now().Unix(),
		//EndTime is set to 0 to indicate that the match is still in progress
		EndTime: 0,
		Deck:    deck,
		CenterPile: CenterPile{
			Cards: []Card{},
		},
		LastSuccessfulSlapper: "",
		DeadMansSlaps:         make(map[string]int),
	}

	for len(match.Deck.Cards) > 0 {
		for i := 0; i < len(players); i++ {
			currentPlayerIndex := (firstPlayerIdx + 1 + i) % len(players)
			player := players[currentPlayerIndex]

			if len(match.Deck.Cards) == 0 {
				break
			}
			card := match.Deck.DrawCard()

			hand := match.Hands[player.Username]
			hand.Cards = append(hand.Cards, card)
			match.Hands[player.Username] = hand
		}
	}

	for _, player := range players {
		match.DeadMansSlaps[player.Username] = 3
	}

	return match
}

func generateMatchID() string {
	return fmt.Sprintf("%d", time.Now().UnixNano())
}

func getRandomIndex(n int) (int, error) {
	if n == 0 {
		return 0, fmt.Errorf("getRandomIndex: the slice is empty")
	}
	bigN := big.NewInt(int64(n))
	bigIdx, err := rand.Int(rand.Reader, bigN)
	if err != nil {
		return 0, err
	}
	return int(bigIdx.Int64()), nil
}

func (m *Match) GetPlayerByID(playerID string) *Player {
	for _, player := range m.Players {
		if player.ID == playerID {
			return player
		}
	}
	return nil
}

func (m *Match) PlayCard(playerID string) error {
	player := m.GetPlayerByID(playerID)
	if player == nil {
		return fmt.Errorf("player with ID %s not found in match", playerID)
	}

	if m.MatchState != MatchStateInProgress {
		return fmt.Errorf("match is not in progress")
	}

	if m.Players[m.TurnIndex].ID != player.ID {
		return fmt.Errorf("it is not player %s's turn", player.Username)
	}

	hand := m.Hands[player.Username]
	if len(hand.Cards) == 0 {
		return fmt.Errorf("player %s has no cards to play", player.Username)
	}

	// update player's hand
	card := hand.Cards[0]
	hand.Cards = hand.Cards[1:]
	m.Hands[player.Username] = hand

	// put it in the center pile
	m.CenterPile.Cards = append(m.CenterPile.Cards, card)

	m.TurnIndex = (m.TurnIndex + 1) % len(m.Players)

	return nil
}

func (m *Match) AddPileToHand(playerID string) {
	player := m.GetPlayerByID(playerID)
	m.DeadMansSlaps[player.Username] = 3

	// shuffle the pile before adding
	for i := range m.CenterPile.Cards {
		j := mrand.Intn(i + 1)
		m.CenterPile.Cards[i], m.CenterPile.Cards[j] = m.CenterPile.Cards[j], m.CenterPile.Cards[i]
	}

	// add center pile to bottom of hand of player who slapped / won the pile
	hand := m.Hands[player.Username]
	hand.Cards = append(m.Hands[player.Username].Cards, m.CenterPile.Cards...)
	m.Hands[player.Username] = hand
	m.LastSuccessfulSlapper = player.Username

	// clear center pile
	m.CenterPile.Cards = make([]Card, 0)

	// update turn index to the player who slapped/ won the pile
	for i, p := range m.Players {
		if p.ID == playerID {
			m.TurnIndex = i
			break
		}
	}
}

func (m *Match) PunishBadSlap(playerID string) {
	player := m.GetPlayerByID(playerID)

	if len(m.Hands[player.Username].Cards) == 0 {
		m.DeadMansSlaps[player.Username]--
		return
	}
	if len(m.Hands[player.Username].Cards) == 1 {
		m.CenterPile.Cards = append(m.CenterPile.Cards, m.Hands[player.Username].Cards[0])
		return
	}
	m.CenterPile.Cards = append(m.CenterPile.Cards, m.Hands[player.Username].Cards[0], m.Hands[player.Username].Cards[1])
}

func (m *Match) CheckEndGame() bool {
	for _, player := range m.Players {
		if len(m.Hands[player.Username].Cards) == 52 {
			m.MatchState = MatchStateComplete
			m.Winner = *player
			m.EndTime = time.Now().Unix()
			return true
		}
	}
	return false
}

/*
The Play
Starting to the left of the dealer players pull the top card off their pile and place it face-up in the middle. If the card played is a number card, the next player puts down a card, too. This continues around the table until somebody puts down a face card or an Ace (J, Q, K, or A).

When a face card or an ace is played, the next person in the sequence must play another face card or an ace in order for play to continue.

If the next person in the sequence does not play a face card or an ace within their allotted chance, the person who played the last face card or an ace wins the round and the whole pile goes to them. The winner begins the next round of play.

The only thing that overrides the face card or an ace rule is the slap rule. The first person to slap the pile of cards when the slap rule is put into effect is the winner of that round. If it cannot be determined who was the first to slap the pile, the person with the most fingers on top wins.

Slap Rules
Double – When two cards of equivalent value are laid down consecutively. Ex: 5, 5
Sandwich – When two cards of equivalent value are laid down consecutively, but with one card of different value between them. Ex: 5, 7, 5
Top Bottom – When the same card as the first card of the set is laid down.
Tens – When two cards played consecutively (or with a letter card in between) add up to 10. For this rule, an ace counts as one. Ex: 3, 7 or A, K, 9
Jokers – When jokers are used in the game, which should be determined before game play begins. Anytime someone lays down a joker, the pile can be slapped.
Four in a row – When four cards with values in consistent ascending or descending order is placed. Ex: 5, 6, 7, 8 or Q, K, A, 2
Marriage – When a queen is placed over or under a king. Ex: Q, K or K,Q

You must add one or two cards to the bottom of the pile if you slap the pile when it was not slappable.

Continue playing even if you have run out of cards. As long as you don't slap at the wrong time, you are still allowed to "slap in" and get cards! Everyone should try to stay in the game until you have a single winner who obtains all the cards
*/
