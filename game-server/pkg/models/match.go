package models

import (
	"crypto/rand"
	"fmt"
	"math/big"
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
	ID         string          `json:"id"`
	Players    []*Player       `json:"players"`
	Hands      map[string]Hand `json:"hands"` //map of player username to hand
	MatchState MatchState      `json:"match_state"`
	TurnIndex  int             `json:"turn_index"`
	Winner     Player          `json:"winner"`
	StartTime  int64           `json:"start_time"`
	EndTime    int64           `json:"end_time"`
	Deck       Deck            `json:"deck"`
	CenterPile CenterPile      `json:"center_pile"`
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
		return fmt.Errorf("Player %s has no cards to play", player.Username)
	}

	card := hand.Cards[0]
	hand.Cards = hand.Cards[1:]
	m.Hands[player.Username] = hand

	// put it in the center pile
	m.CenterPile.Cards = append(m.CenterPile.Cards, card)

	// update turn index
	m.TurnIndex = (m.TurnIndex + 1) % len(m.Players)

	return nil
}