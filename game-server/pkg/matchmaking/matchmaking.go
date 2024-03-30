package matchmaking

import (
	"sync"

	"github.com/gorilla/websocket"
)

type Player struct {
	ID string
	Conn *websocket.Conn
}

type Match struct {
	Players []Player // players in the match
	// other match data
}

type Matcher struct {
	QueuedPlayers []Player // players waiting to be matched
	Matches []Match // matches that have been created
	MatchLock sync.Mutex // lock for queue and matches
}

func NewMatcher() *Matcher {
	return &Matcher{
		QueuedPlayers: make([]Player, 0),
		Matches: make([]Match, 0),
	}
}

// enqueues a player to be matched
func (m *Matcher) AddPlayer(p Player) {
	m.MatchLock.Lock()
	defer m.MatchLock.Unlock()
	m.QueuedPlayers = append(m.QueuedPlayers, p)
}

// attempts to pair players in the queue

