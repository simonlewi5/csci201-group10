package matchmaking

import (
	"sync"

	socketio "github.com/googollee/go-socket.io"
	"github.com/simonlewi5/csci201-group10/game-server/internal/models"
)

type Matcher struct {
	QueuedPlayers []*models.Player         // players waiting to be matched
	Matches       []*models.Match          // matches that have been created
	PlayerConns   map[string]socketio.Conn // map of player IDs to their connections
	MatchLock     sync.Mutex               // lock for queue and matches
}

func NewMatcher() *Matcher {
	return &Matcher{
		QueuedPlayers: make([]*models.Player, 0),
		Matches:       make([]*models.Match, 0),
		PlayerConns:   make(map[string]socketio.Conn),
	}
}

// enqueues a player to be matched
func (m *Matcher) AddPlayer(p *models.Player, conn socketio.Conn) {
	m.MatchLock.Lock()
	defer m.MatchLock.Unlock()
	m.QueuedPlayers = append(m.QueuedPlayers, p)
	m.PlayerConns[p.ID] = conn
}

// attempts to pair players in the queue
