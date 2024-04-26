package main

import (
	"fmt"
	"log"
	"net/http"
	"time"

	"github.com/gorilla/websocket"
	"github.com/simonlewi5/csci201-group10/game-server/pkg/db"
	"github.com/simonlewi5/csci201-group10/game-server/pkg/handlers"
	"github.com/simonlewi5/csci201-group10/game-server/pkg/matchmaking"
)

var (
    upgrader = websocket.Upgrader{
        ReadBufferSize:  1024,
        WriteBufferSize: 1024,
        HandshakeTimeout: 30 * time.Second,
    }
    dbService db.DBService
    matcher *matchmaking.Matcher
)

func websocketHandler(dbService db.DBService) http.HandlerFunc {
    return func(w http.ResponseWriter, r *http.Request) {
        conn, err := upgrader.Upgrade(w, r, nil)
        // db service check
        if dbService == nil {
            log.Fatal("dbService is nil")
        }
        if err != nil {
            log.Println("Failed to upgrade to websocket:", err)
            return
        }
        defer conn.Close()
        go startPingRoutine(conn)

        handlers.HandleConnections(dbService, matcher)(conn)
    }
}

func startPingRoutine(conn *websocket.Conn) {
    ticker := time.NewTicker(30 * time.Second)
    defer ticker.Stop()
    for range ticker.C {
        if err := conn.WriteMessage(websocket.PingMessage, nil); err != nil {
            log.Printf("ping failed: %s", err)
            return  // maybe reconnect here
        }
    }
}


func main() {
    dbService = db.SetupDatabase()
    matcher = matchmaking.NewMatcher(dbService)
    go matcher.StartMatching()

    http.HandleFunc("/ws", websocketHandler(dbService))
    fmt.Println("WebSocket server starting on port 8080...")
    if err := http.ListenAndServe(":8080", nil); err != nil {
        fmt.Printf("Error starting server: %v\n", err)
    }
}
