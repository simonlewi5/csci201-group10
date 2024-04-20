package models

import (
	"fmt"
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
	ID         string     `json:"id"`
	Players    []*Player  `json:"players"`
	MatchState MatchState `json:"match_state"`
	TurnIndex  int        `json:"turn_index"`
	Winner     Player     `json:"winner"`
	StartTime  int64      `json:"start_time"`
	EndTime    int64      `json:"end_time"`
	Deck       Deck       `json:"deck"`
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

	match := &Match{
		ID:         matchID,
		Players:    players,
		MatchState: MatchStateInit,
		TurnIndex:  0,
		//Winner is set to an empty player to indicate that the match is still in progress
		Winner:     Player{},
		StartTime:  time.Now().Unix(),
		//EndTime is set to 0 to indicate that the match is still in progress
		EndTime: 	0,
		Deck:       deck,
	}

	for _, player := range players {
		player.CurrentMatchID = matchID
		player.CurrentMatch = match
		player.Hand.Cards = deck.DrawCards(5)
		player.CurrScore = 0
	}

	return match
}

func generateMatchID() string {
	return fmt.Sprintf("%d", time.Now().UnixNano())
}