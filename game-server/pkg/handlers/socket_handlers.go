package handlers

import (
	"log"

	socketio "github.com/googollee/go-socket.io"
)

func RegisterSocketHandlers(server *socketio.Server) {
	server.OnConnect("/", func(s socketio.Conn) error {
		log.Println("connected:", s.ID())
		return nil
	})

	server.OnEvent("/", "matchmaking", func(s socketio.Conn, msg string) {
		log.Println("matchmaking:", msg)
	})

	server.OnEvent("/", "game", func(s socketio.Conn, msg string) {
		log.Println("game:", msg)
	})

	server.OnError("/", func(s socketio.Conn, e error) {
		log.Println("meet error:", e)
	})

	server.OnDisconnect("/", func(s socketio.Conn, reason string) {
		log.Println("closed", reason)
	})
}
