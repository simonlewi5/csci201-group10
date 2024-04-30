package handlers

import (
	"encoding/json"
	"io"
	"log"
	"net/http"
	"net/http/httptest"
	"os"
	"testing"

	"github.com/gorilla/websocket"
	"github.com/simonlewi5/csci201-group10/game-server/pkg/matchmaking"
	"github.com/simonlewi5/csci201-group10/game-server/pkg/models"
)

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

var upgrader = websocket.Upgrader{
	ReadBufferSize:   1024,
	WriteBufferSize:  1024,
}
func setupTestWebSocketServer() (*httptest.Server, string) {
    server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        conn, err := upgrader.Upgrade(w, r, nil)
        if err != nil {
            log.Println("Upgrade:", err)
            return
        }
        handler := HandleConnections(&MockDBService{}, &matchmaking.Matcher{})
        handler(conn)
    }))
    url := "ws" + server.URL[len("http"):]
    return server, url
}

func TestHandleConnections(t *testing.T) {
	defer muteLogger()()
    server, url := setupTestWebSocketServer()
    defer server.Close()

    conn, _, err := websocket.DefaultDialer.Dial(url, nil)
    if err != nil {
        t.Fatalf("Could not open WebSocket: %v", err)
    }
    defer conn.Close()

    msg := map[string]interface{}{"action": "login", "username": "testuser", "password": "testpass"}
    msgBytes, err := json.Marshal(msg)
    if err != nil {
        t.Fatal("Failed to marshal message:", err)
    }

    if err := conn.WriteMessage(websocket.TextMessage, msgBytes); err != nil {
        t.Fatal("WriteMessage failed:", err)
    }

    messageType, p, err := conn.ReadMessage()
    if err != nil {
        t.Fatal("ReadMessage failed:", err)
    }
    if messageType != websocket.TextMessage {
        t.Errorf("Expected text message, got %v", messageType)
    }

    var response models.Message
    if err := json.Unmarshal(p, &response); err != nil {
        t.Fatal("Error unmarshalling response:", err)
    }

    if response.Type != models.MessageTypeAuthSuccess {
        t.Errorf("Expected auth success, got %v", response.Type)
    }
}

func TestHandleLogin(t *testing.T) {
	defer muteLogger()()
    server, url := setupTestWebSocketServer()
    defer server.Close()

    conn, _, err := websocket.DefaultDialer.Dial(url, nil)
    if err != nil {
        t.Fatalf("Could not open WebSocket: %v", err)
    }
    defer conn.Close()

    // Send a login message
    msg := map[string]interface{}{"action": "login", "username": "testuser", "password": "testpass"}
    err = conn.WriteJSON(msg)
    if err != nil {
        t.Fatal("Failed to send message:", err)
    }

    // Read response
    _, p, err := conn.ReadMessage()
    if err != nil {
        if !websocket.IsCloseError(err, websocket.CloseNormalClosure, websocket.CloseGoingAway) {
            t.Fatal("ReadMessage failed:", err)
        }
    }

    var response models.Message
    json.Unmarshal(p, &response)
    if response.Type != models.MessageTypeAuthSuccess {
        t.Errorf("Expected MessageTypeAuthSuccess, got %s", response.Type)
    }
}

func TestHandleRegistration(t *testing.T) {
	defer muteLogger()()
    server, url := setupTestWebSocketServer()
    defer server.Close()

    conn, _, err := websocket.DefaultDialer.Dial(url, nil)
    if err != nil {
        t.Fatalf("Could not connect to WebSocket: %v", err)
    }
    defer conn.Close()

    registration := map[string]interface{}{
        "action": "register",
        "username": "newuser",
        "email": "newuser@example.com",
        "password": "newpass",
    }
    err = conn.WriteJSON(registration)
    if err != nil {
        t.Fatalf("Failed to send registration data: %v", err)
    }

    _, p, err := conn.ReadMessage()
    if err != nil {
        t.Fatal("ReadMessage failed:", err)
    }

    var response models.Message
    json.Unmarshal(p, &response)
    if response.Type != models.MessageTypeAuthSuccess {
        t.Errorf("Expected MessageTypeAuthSuccess, got %s", response.Type)
    }
}
