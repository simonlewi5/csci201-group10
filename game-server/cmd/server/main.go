package main

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"

	"github.com/gorilla/websocket"
	"github.com/simonlewi5/csci201-group10/game-server/internal/models"
	"github.com/simonlewi5/csci201-group10/game-server/pkg/db"
	"github.com/simonlewi5/csci201-group10/game-server/pkg/matchmaking"
)

var (
    upgrader = websocket.Upgrader{
        ReadBufferSize:  1024,
        WriteBufferSize: 1024,
    }
    matcher *matchmaking.Matcher
    dbService db.DBService
)

func handleConnections(w http.ResponseWriter, r *http.Request) {
    conn, err := upgrader.Upgrade(w, r, nil)
    if err != nil {
        log.Println(err)
        return
    }
    defer conn.Close()

    //authenticate user
    var player *models.Player
    for {
        _, msg, err := conn.ReadMessage()
        if err != nil {
            log.Println("Error reading message during authentication:", err)
            return
        }
    
        // JSON with user credentials or token
        var credentials *models.Credentials
        if err := json.Unmarshal(msg, &credentials); err != nil {
            log.Println("Error unmarshalling credentials:", err)
            continue // try again
        }

        // validate credentials
        if player, err = dbService.GetPlayer(credentials); err != nil {
            log.Println("Error getting player from database:", err)
            return
        }

        if player != nil {
            break // authenticated
        }
    }

    // add player to matchmaking queue
    matcher.AddPlayer(player, conn)


    // listen for messages
    for {
        _, msg, err := conn.ReadMessage()
        if err != nil {
            log.Println(err)
            return
        }
        log.Printf("Received message: %s\n", msg)

        // this is where we handle game messages
    }
}

func main() {
    dbService := db.SetupDatabase()
    matcher = matchmaking.NewMatcher(dbService)

    http.HandleFunc("/ws", handleConnections)
    fmt.Println("WebSocket server starting on port 8080...")
    if err := http.ListenAndServe(":8080", nil); err != nil {
        fmt.Printf("Error starting server: %v\n", err)
    }

}
