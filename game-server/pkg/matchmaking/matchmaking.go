package matchmaking

import (
	"log"
	"sync"
	"time"

	"github.com/gorilla/websocket"
	"github.com/simonlewi5/csci201-group10/game-server/internal/models"
	"github.com/simonlewi5/csci201-group10/game-server/pkg/db"
)

type Matcher struct {
	QueuedPlayers []*models.Player         // players waiting to be matched
	Matches       []*models.Match          // matches that have been created
	PlayerConns   map[string]*websocket.Conn // map of player IDs to their connections
	MatchLock     sync.Mutex               // lock for queue and matches
	DBService     db.DBService
}

func NewMatcher(dbService db.DBService) *Matcher {
	return &Matcher{
		QueuedPlayers: make([]*models.Player, 0),
		Matches:       make([]*models.Match, 0),
		PlayerConns:   make(map[string]*websocket.Conn),
		DBService:     dbService,
	}
}

// enqueues a player to be matched
func (m *Matcher) AddPlayer(p *models.Player, conn *websocket.Conn) {
	m.MatchLock.Lock()
	defer m.MatchLock.Unlock()
	m.QueuedPlayers = append(m.QueuedPlayers, p)
	m.PlayerConns[p.ID] = conn
	// notify the player they've been added to the queue
	conn.WriteJSON(models.Message{
		Type: "QUEUE_UPDATE",
		Data: "queued",
	})
}

func (m *Matcher) StartMatching() {
	for {
		time.Sleep(5 * time.Second)
		m.MatchLock.Lock()
		
		queueLength := len(m.QueuedPlayers)
		
		if queueLength >= 2 {
			// there's enough players to make a match,
			// but we will wait for more players to join to try to start
			// a match with 4 players
			targetLength := min(4, queueLength)
			matchPlayers := m.QueuedPlayers[:targetLength]

			if queueLength == 2 {
				m.MatchLock.Unlock()
				time.Sleep(30 * time.Second)
				m.MatchLock.Lock()

				queueLength = len(m.QueuedPlayers)
				if queueLength > 2 {
					targetLength = min(4, queueLength)
				}

				matchPlayers = m.QueuedPlayers[:targetLength]
			}

			if queueLength == 3 {
				m.MatchLock.Unlock()
				time.Sleep(20 * time.Second)
				m.MatchLock.Lock()

				queueLength = len(m.QueuedPlayers)
				targetLength = min(4, queueLength)


				matchPlayers = m.QueuedPlayers[:targetLength]
			}

			newMatch := models.NewMatch(matchPlayers)
			m.Matches = append(m.Matches, newMatch)
			m.sendMatchFound(newMatch)
			m.QueuedPlayers = m.QueuedPlayers[targetLength:]
		}
		m.MatchLock.Unlock()
	}
}

func (m *Matcher) GetMatches() []*models.Match {
	m.MatchLock.Lock()
	defer m.MatchLock.Unlock()
	return m.Matches
}

func (m *Matcher) GetQueuedPlayers() []*models.Player {
	m.MatchLock.Lock()
	defer m.MatchLock.Unlock()
	return m.QueuedPlayers
}

func (m *Matcher) GetPlayerConns() map[string]*websocket.Conn {
	m.MatchLock.Lock()
	defer m.MatchLock.Unlock()
	return m.PlayerConns
}

func (m *Matcher) GetMatchByID(matchID string) *models.Match {
	m.MatchLock.Lock()
	defer m.MatchLock.Unlock()
	for _, match := range m.Matches {
		if match.ID == matchID {
			return match
		}
	}
	return nil
}

func (m *Matcher) sendMatchFound(match *models.Match) {
	message := models.Message{
		Type: models.MessageTypeMatchFound,
		Data: match,
	}

	for _, player := range match.Players {
		if conn, ok := m.PlayerConns[player.ID]; ok {
			err := conn.WriteJSON(message)
			if err != nil {
				log.Printf("Error sending MATCH_FOUND message to player %s: %v", player.ID, err)
			}
		} else {
			log.Printf("No connection found for player %s", player.ID)
		}
	}
}

func (m *Matcher) EndMatch(matchID string, winner *models.Player) {
	m.MatchLock.Lock()
	defer m.MatchLock.Unlock()

	for _, match := range m.Matches {
		if match.ID == matchID {
			match.Winner = *winner
			match.MatchState = models.MatchStateComplete
			if err := m.DBService.RecordMatchEnd(match); err != nil {
				log.Printf("Error recording match end: %v", err)
				return
			}
			m.sendMatchEnd(match)
			break
		}
	}
}

func (m *Matcher) sendMatchEnd(match *models.Match) {
	message := models.Message{
		Type: models.MessageTypeMatchEnd,
		Data: match,
	}

	for _, player := range match.Players {
		if conn, ok := m.PlayerConns[player.ID]; ok {
			conn.WriteJSON(message)
		} else {
			log.Printf("No connection found for player %s", player.ID)
		}
	}
}

