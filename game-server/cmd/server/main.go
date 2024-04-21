package main

import (
	"fmt"
	"log"
	"net/http"

	"github.com/gorilla/websocket"
	"github.com/simonlewi5/csci201-group10/game-server/pkg/db"

	"github.com/simonlewi5/csci201-group10/game-server/pkg/handlers"

)

var (
    upgrader = websocket.Upgrader{
        ReadBufferSize:  1024,
        WriteBufferSize: 1024,
    }
    dbService db.DBService
)

func websocketHandler(dbService db.DBService) http.HandlerFunc {
    return func(w http.ResponseWriter, r *http.Request) {
        conn, err := upgrader.Upgrade(w, r, nil)
        if err != nil {
            log.Println("Failed to upgrade to websocket:", err)
            return
        }
        defer conn.Close()

        handlers.HandleConnections(dbService)(conn)
    }
}

func main() {
    dbService := db.SetupDatabase()
    http.HandleFunc("/ws", websocketHandler(dbService))
    fmt.Println("WebSocket server starting on port 8080...")
    if err := http.ListenAndServe(":8080", nil); err != nil {
        fmt.Printf("Error starting server: %v\n", err)
    }
}
