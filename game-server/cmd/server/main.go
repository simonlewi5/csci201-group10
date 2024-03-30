package main

import (
	"log"
	"net/http"
	"github.com/gorilla/websocket"
	"github.com/simonlewi5/csci201-group10/game-server/internal/matchmaking"

)

var upgrader = websocket.Upgrader{
	CheckOrigin: func(r *http.Request) bool {
		return true
	},
	ReadBufferSize: 1024,
	WriteBufferSize: 1024,
}

func main() {
	matcher := matchmaking.NewMatcher()

	http.HandleFunc("/ws", func(w http.ResponseWriter, r *http.Request) {
		serveWs(matcher, w, r)
	})

	log.Println("Server started on port 8080")
	log.Fatal(http.ListenAndServe(":8080", nil))
}

func serveWs(matcher *matchmaking.Matcher, w http.ResponseWriter, r *http.Request) {
	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Println("Error upgrading connection:", err)
		return
	}
	defer conn.Close()

	for {
		_, message, err := conn.ReadMessage()
		if err != nil {
			log.Println("Error reading message:", err)
			break
		}
		log.Printf("Received message: %s from %s\n", message, conn.RemoteAddr())

	}
}
