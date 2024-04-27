package handlers

import (
	"encoding/json"
	"log"

	"github.com/gorilla/websocket"
	"github.com/simonlewi5/csci201-group10/game-server/pkg/db"
	"github.com/simonlewi5/csci201-group10/game-server/pkg/models"
    "github.com/simonlewi5/csci201-group10/game-server/pkg/matchmaking"
    
)

func HandleConnections(dbService db.DBService, matcher *matchmaking.Matcher) func(*websocket.Conn) {
    return func(conn *websocket.Conn) {
        for {
            _, msg, err := conn.ReadMessage()
            if err != nil {
                go matcher.DequeuePlayerByConn(conn)
                log.Println("Error reading message:", err)
                break
            }
            var data map[string]interface{}
            if err := json.Unmarshal(msg, &data); err != nil {
                log.Println("Error unmarshalling message:", err)
                continue
            }
            switch data["action"] {
                case "login":
                    handleLogin(dbService, conn, data)
                case "register":
                    handleRegistration(dbService, conn, data)
                case "place_card":
                    log.Println("Place card action received")
                case "slap":
                    log.Println("Slap action received")
                case "search_for_match":
                    handleSearchForMatch(dbService, matcher, conn, data)
                default:
                    log.Println("Unknown action:", data["action"])
            }
        }
    }
}

func handleLogin(dbService db.DBService, conn *websocket.Conn, data map[string]interface{}) {
    credentials := models.Credentials{
        Username: data["username"].(string),
        Password: data["password"].(string),
    }

    player, err := dbService.GetPlayer(&credentials)
    if err != nil {
        sendMessage(conn, models.Message{
            Type: models.MessageTypeAuthError,
            Data: "Login failed: " + err.Error(),
        })
        return
    }
    
    sendMessage(conn, models.Message{
        Type: models.MessageTypeAuthSuccess,
        Data: map[string]interface{}{
            "message": "Login successful.",
            "player": player,
        },
    })
}

func handleRegistration(dbService db.DBService, conn *websocket.Conn, data map[string]interface{}) {
    credentials := models.Credentials{
        Username: data["username"].(string),
        Email: data["email"].(string),
        Password: data["password"].(string),
    }
    player, err := dbService.HandleRegisterEmailAndPassword(&credentials)
    if err != nil {
        sendMessage(conn, models.Message{
            Type: models.MessageTypeAuthError,
            Data: "Registration failed: " + err.Error(),
        })
        return
    }
    sendMessage(conn, models.Message{
        Type: models.MessageTypeAuthSuccess,
        Data: map[string]interface{}{
            "message": "Registration successful.",
            "player": player,
        },
    })
}

func handleSearchForMatch(dbService db.DBService, matcher *matchmaking.Matcher, conn *websocket.Conn, data map[string]interface{}) {
    playerID, ok := data["player_id"].(string)
    if !ok || playerID == "" {
        log.Println("player_id is missing or not a string")
        sendMessage(conn, models.Message{
            Type: models.MessageTypeAuthError,
            Data: "player_id is required and must be a string",
        })
        return
    }

    player, err := dbService.GetPlayerByID(playerID)
    if err != nil {
        log.Printf("Error retrieving player with ID %s: %v", playerID, err)
        sendMessage(conn, models.Message{
            Type: models.MessageTypeAuthError,
            Data: "Error searching for match: " + err.Error(),
        })
        return
    }

    matcher.QueuePlayer(player, conn)
}

func sendMessage(conn *websocket.Conn, msg models.Message) {
	msgJSON, err := json.Marshal(msg)
	if err != nil {
		log.Println("Error marshalling message:", err)
		return
	}
	if err := conn.WriteMessage(websocket.TextMessage, msgJSON); err != nil {
		log.Println("Error sending message:", err)
	}
}

func handleSlap( ) {
    // TODO: Implement slap handling
    // 1. Check if the player is allowed to slap
    // 2. Check if the slap is valid
    // 3. Update the match state
    // 4. Send the updated match state to all players
    
}