package main

import (
	"log"
	"net/http"
	// "os"

	socketio "github.com/googollee/go-socket.io"
	"github.com/simonlewi5/csci201-group10/game-server/internal/models"
	// "github.com/simonlewi5/csci201-group10/game-server/pkg/handlers"
	"github.com/simonlewi5/csci201-group10/game-server/pkg/matchmaking"
)

func main() {
	server := socketio.NewServer(nil)
	
	matcher := matchmaking.NewMatcher()

	server.OnConnect("/", func(s socketio.Conn) error {
		log.Println("connected:", s.ID())
		return nil
	})

	server.OnEvent("/", "joinMatchmaking", func(s socketio.Conn, userID string) {
		
		// dummy player creation
		player := models.NewPlayer(userID, "username", "firebaseUID", "email")
		matcher.AddPlayer(&player, s)
		log.Printf("Player %s joined matchmaking", player.ID)

	})

	server.OnDisconnect("/", func(s socketio.Conn, reason string) {
		log.Println("Disconnected: ", reason)
	})

	go func() {
		if err := server.Serve(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("serve error: %v\n", err)
		}
	}()
	defer server.Close()

	http.Handle("/socket.io/", server)
	log.Println("Socket.IO server running at localhost:8000")
	if err := http.ListenAndServe(":8000", nil); err != nil {
		log.Fatalf("listen error: %v\n", err)
	}
}
