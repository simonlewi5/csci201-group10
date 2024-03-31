package models

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
