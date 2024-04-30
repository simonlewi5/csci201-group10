package matchmaking

import (
	"io"
	"os"
	"testing"

	"log"
	"net/http"
	"net/http/httptest"

	"github.com/gorilla/websocket"
	"github.com/simonlewi5/csci201-group10/game-server/pkg/models"
)


var upgrader = websocket.Upgrader{
    CheckOrigin: func(r *http.Request) bool {
        return true
    },
}

func echoHandler(w http.ResponseWriter, r *http.Request) {
    conn, err := upgrader.Upgrade(w, r, nil)
    if err != nil {
        log.Println("Upgrade:", err)
        return
    }
    defer conn.Close()
    for {
        messageType, message, err := conn.ReadMessage()
        if err != nil {
            log.Println("Read:", err)
            break
        }
        err = conn.WriteMessage(messageType, message)
        if err != nil {
            log.Println("Write:", err)
            break
        }
    }
}


type MockDBService struct{}

func (m *MockDBService) RecordMatchEnd(match *models.Match) error {
	return nil // Simulate successful database operation
}

func (m *MockDBService) NewPlayer(player *models.Player) error {
	return nil // Assume player creation is always successful
}

func (m *MockDBService) GetPlayer(credentials *models.Credentials) (*models.Player, error) {
	return &models.Player{ID: "1", Username: credentials.Username}, nil
}

func (m *MockDBService) GetPlayerByID(id string) (*models.Player, error) {
	return &models.Player{ID: id, Username: "TestPlayer", Email: "test@example.com"}, nil
}

func (m *MockDBService) HandleRegisterEmailAndPassword(credentials *models.Credentials) (*models.Player, error) {
	return &models.Player{ID: "1", Username: credentials.Username}, nil
}

func muteLogger() func() {
    log.SetOutput(io.Discard) 
    return func() {
        log.SetOutput(os.Stderr)
    }
}

func TestQueuePlayer(t *testing.T) {
	defer muteLogger()()
    dbService := &MockDBService{}
	mockTimer := &MockTimer{C: make(chan bool, 1)}
    matcher := NewMatcher(dbService, mockTimer)

    server := httptest.NewServer(http.HandlerFunc(echoHandler))
    defer server.Close()
    url := "ws" + server.URL[len("http"):]

    conn, _, err := websocket.DefaultDialer.Dial(url, nil)
    if err != nil {
        t.Fatalf("Could not open WebSocket: %v", err)
    }
    defer conn.Close()

    player := models.NewPlayer("1", "TestPlayer", "", "test@example.com")
    matcher.QueuePlayer(player, conn)

    if len(matcher.QueuedPlayers) != 1 {
        t.Errorf("QueuePlayer failed to add player to queue")
    }
    if matcher.PlayerConns[player.ID] != conn {
        t.Errorf("QueuePlayer failed to map connection correctly")
    }
}

func TestDequeuePlayer(t *testing.T) {
	defer muteLogger()()
    dbService := &MockDBService{}
	mockTimer := &MockTimer{C: make(chan bool, 1)}
    matcher := NewMatcher(dbService, mockTimer)

    server := httptest.NewServer(http.HandlerFunc(echoHandler))
    defer server.Close()
    url := "ws" + server.URL[len("http"):]

    conn, _, err := websocket.DefaultDialer.Dial(url, nil)
    if err != nil {
        t.Fatalf("Could not open WebSocket: %v", err)
    }
    defer conn.Close()

    player := models.NewPlayer("1", "TestPlayer", "", "test@example.com")
    matcher.QueuePlayer(player, conn)
    matcher.DequeuePlayer(player, conn)
    
    if len(matcher.QueuedPlayers) != 0 {
        t.Errorf("DequeuePlayer failed to remove player from queue")
    }
    if _, exists := matcher.PlayerConns[player.ID]; exists {
        t.Errorf("DequeuePlayer failed to remove player connection")
    }
}


// could write more tests for the Matcher struct
