package handlers

import (
	"encoding/json"
	"log"
	"time"

	"github.com/gorilla/websocket"
	"github.com/simonlewi5/csci201-group10/game-server/pkg/db"
	"github.com/simonlewi5/csci201-group10/game-server/pkg/matchmaking"
	"github.com/simonlewi5/csci201-group10/game-server/pkg/models"
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
                case "play_card":
                    log.Println("Place card action received")
                    handlePlayCard(conn, matcher, data)
                case "slap":
                    log.Println("Slap action received")
                    handleSlap(conn, matcher, data)
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

func handlePlayCard(conn *websocket.Conn, matcher *matchmaking.Matcher, data map[string]interface{}) {
    playerID, ok := data["player_id"].(string)
    if !ok || playerID == "" {
        log.Println("player_id is missing or not a string")
        sendMessage(conn, models.Message{
            Type: models.MessageTypeAuthError,
            Data: "player_id is required and must be a string",
        })
        return
    }

    match := matcher.GetMatchByPlayerID(playerID)
    if match == nil {
        log.Println("Player is not in a match")
        sendMessage(conn, models.Message{
            Type: models.MessageTypeAuthError,
            Data: "Player is not in a match",
        })
        return
    }

    match.GameLock.Lock()
    defer match.GameLock.Unlock()
    
    err := match.PlayCard(playerID)
    if err != nil {
        log.Println("Error playing card:", err)
        sendMessage(conn, models.Message{
            Type: models.MessageTypeAuthError,
            Data: "Error playing card: " + err.Error(),
        })
        return
    }

    for _, player := range match.Players {
        if conn, ok := matcher.GetPlayerConns()[player.ID]; ok {
            sendMessage(conn, models.Message{
                Type: models.MessageTypeMatchUpdate,
                Data: match,
            })
        }
    }

    // Check for face card/Ace challenge
    challengeIssued, challengeMet := match.CenterPile.CheckFaceCardChallenge()
    if challengeIssued && !challengeMet {
        // pause for a second so everyone can see the challenge
        time.Sleep(1 * time.Second)

        // award the center pile to the player who met the challenge
        match.AddPileToHand(playerID)

        // notify all players of the updated match state
        for _, player := range match.Players {
            if conn, ok := matcher.GetPlayerConns()[player.ID]; ok {
                sendMessage(conn, models.Message{
                    Type: models.MessageTypeMatchUpdate,
                    Data: match,
                })
            }
        }
    }

    // check if the match is over
}

func handleSlap(conn *websocket.Conn, matcher *matchmaking.Matcher, data map[string]interface{}) {
    // TODO: Implement slap handling
    // 1. Check if the player is allowed to slap
    // -- for this, need to track how many slap tries someone has left if they're dead
    // 2. Check if the slap is valid
    // -- probably need to record timestamps of when the server received each slap for this, and/or
    //    make it so that if the center pile has been marked as slapped or is in the middle of being
    //    added to the hand of the first slapper, other incoming slaps are ignored/marked as failures
    // 3. Update the match state
    // 4. Send the updated match state to all players


    // player validation checks
    playerID, ok := data["player_id"].(string)
    if !ok || playerID == "" {
        log.Println("player_id is missing or not a string")
        sendMessage(conn, models.Message{
            Type: models.MessageTypeAuthError,
            Data: "player_id is required and must be a string",
        })
        return
    }

    match := matcher.GetMatchByPlayerID(playerID)
    if match == nil {
        log.Println("Player is not in a match")
        sendMessage(conn, models.Message{
            Type: models.MessageTypeAuthError,
            Data: "Player is not in a match",
        })
        return
    }

    playerUsername := match.GetPlayerByID(playerID).Username

    // lock the match before doing anything
    match.GameLock.Lock()
    defer match.GameLock.Unlock()

    slappable := match.CenterPile.CheckSlappable()

    deadMansSlaps := match.DeadMansSlaps[playerUsername]
    if deadMansSlaps == 0 || (deadMansSlaps == 1 && !slappable) {
        for _, player := range match.Players {
            if conn, ok := matcher.GetPlayerConns()[player.ID]; ok {
                sendMessage(conn, models.Message{
                    Type: models.MessageTypePlayerLost,
                    Data: playerUsername,
                })
            }
        }
        return
    }

    // check if player is allowed to slap
    if slappable {
        log.Println("Pile is slappable")
        for _, player := range match.Players {
            if conn, ok := matcher.GetPlayerConns()[player.ID]; ok {
                sendMessage(conn, models.Message{
                    Type: models.MessageTypeSlapSuccess,
                    Data: playerUsername,
                })
            }
        }
        match.AddPileToHand(playerID)
    } else {
        log.Println("Pile is not slappable")
        for _, player := range match.Players {
            if conn, ok := matcher.GetPlayerConns()[player.ID]; ok {
                sendMessage(conn, models.Message{
                    Type: models.MessageTypeSlapFail,
                    Data: playerUsername,
                })
            }
        }
        match.PunishBadSlap(playerID)
    }

    // update match state before sending if slap succeeded

    for _, player := range match.Players {
        if conn, ok := matcher.GetPlayerConns()[player.ID]; ok {
            sendMessage(conn, models.Message{
                Type: models.MessageTypeMatchUpdate,
                Data: match,
            })
        }
    }
}