package matchmaking

import (
	"fmt"
	"log"
	"sync"
	"time"

	"github.com/gorilla/websocket"
	"github.com/simonlewi5/csci201-group10/game-server/pkg/db"
	"github.com/simonlewi5/csci201-group10/game-server/pkg/models"
)

type Matcher struct {
	QueuedPlayers []*models.Player         // players waiting to be matched
	Matches       []*models.Match          // matches that have been created
	PlayerConns   map[string]*websocket.Conn // map of player IDs to their connections
	PlayerMatches map[string]*models.Match  // map of player IDs to their matches
	MatchLock     sync.Mutex               // lock for queue and matches
	DBService     db.DBService
	Timer		  Timer
}

type Timer interface {
    Sleep(duration time.Duration)
}

type RealTimer struct{}

func (rt *RealTimer) Sleep(duration time.Duration) {
    time.Sleep(duration)
}

type MockTimer struct {
    C chan bool
}

func (mt *MockTimer) Sleep(duration time.Duration) {
    <-mt.C 
}

func NewMatcher(dbService db.DBService, timer Timer) *Matcher {
	return &Matcher{
		QueuedPlayers: make([]*models.Player, 0),
		Matches:       make([]*models.Match, 0),
		PlayerConns:   make(map[string]*websocket.Conn),
		PlayerMatches: make(map[string]*models.Match),
		DBService:     dbService,
		Timer:         timer,
	}
}

// enqueues a player to be matched
func (m *Matcher) QueuePlayer(p *models.Player, conn *websocket.Conn) {
	m.MatchLock.Lock()
	defer m.MatchLock.Unlock()
	m.QueuedPlayers = append(m.QueuedPlayers, p)
	m.PlayerConns[p.ID] = conn
	// notify the player they've been added to the queue
	queueLength := len(m.QueuedPlayers)
	conn.WriteJSON(models.Message{
		Type: "QUEUE_UPDATE",
		Data: fmt.Sprintf("Queue size: %d", queueLength),
	})

	// handle ungraceful disconnects
	conn.SetCloseHandler(func(code int, text string) error {
        m.MatchLock.Lock()
        defer m.MatchLock.Unlock()
        for i, queuedPlayer := range m.QueuedPlayers {
            if queuedPlayer.ID == p.ID {
                m.QueuedPlayers = append(m.QueuedPlayers[:i], m.QueuedPlayers[i+1:]...)
                break
            }
        }
        delete(m.PlayerConns, p.ID)
        log.Printf("Player %s disconnected and removed from the queue", p.ID)
        return nil
    })
}

func (m *Matcher) DequeuePlayerByConn(conn *websocket.Conn) {
	m.MatchLock.Lock()
	defer m.MatchLock.Unlock()
	for i, player := range m.QueuedPlayers {
		if _, ok := m.PlayerConns[player.ID]; ok {
			if m.PlayerConns[player.ID] == conn {
				m.QueuedPlayers = append(m.QueuedPlayers[:i], m.QueuedPlayers[i+1:]...)
				delete(m.PlayerConns, player.ID)
				break
			}
		}
	}
}

// dequeues a player from the queue
func (m *Matcher) DequeuePlayer(p *models.Player, conn *websocket.Conn) {
	m.MatchLock.Lock()
	defer m.MatchLock.Unlock()
	for i, player := range m.QueuedPlayers {
		if player.ID == p.ID {
			m.QueuedPlayers = append(m.QueuedPlayers[:i], m.QueuedPlayers[i+1:]...)
			break
		}
	}
	if _, ok := m.PlayerConns[p.ID]; ok {
		conn.Close()
		delete(m.PlayerConns, p.ID)
	}
}

func (m *Matcher) StartMatching() {
	for {
		m.Timer.Sleep(5 * time.Second)
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
				// time.Sleep(30 * time.Second)
				m.Timer.Sleep(5 * time.Second) // for testing
				m.MatchLock.Lock()

				queueLength = len(m.QueuedPlayers)
				if queueLength > 2 {
					targetLength = min(4, queueLength)
				}

				matchPlayers = m.QueuedPlayers[:targetLength]
			}

			if queueLength == 3 {
				m.MatchLock.Unlock()
				m.Timer.Sleep(20 * time.Second)
				m.MatchLock.Lock()

				queueLength = len(m.QueuedPlayers)
				targetLength = min(4, queueLength)


				matchPlayers = m.QueuedPlayers[:targetLength]
			}

			newMatch := models.NewMatch(matchPlayers)
			m.Matches = append(m.Matches, newMatch)
			for _, player := range matchPlayers {
				m.PlayerMatches[player.ID] = newMatch
			}
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

// player id to match object
func (m *Matcher) GetMatchByPlayerID(playerID string) *models.Match {
	m.MatchLock.Lock()
	defer m.MatchLock.Unlock()
	return m.PlayerMatches[playerID]
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
