package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
	"time"

	"cloud.google.com/go/storage"
	"github.com/gorilla/websocket"
	"github.com/simonlewi5/csci201-group10/game-server/pkg/db"
	"github.com/simonlewi5/csci201-group10/game-server/pkg/handlers"
	"github.com/simonlewi5/csci201-group10/game-server/pkg/matchmaking"
	"golang.org/x/oauth2/google"
	"google.golang.org/api/option"
)

var (
	upgrader = websocket.Upgrader{
		ReadBufferSize:   1024,
		WriteBufferSize:  1024,
		HandshakeTimeout: 30 * time.Second,
	}
	dbService db.DBService
	matcher   *matchmaking.Matcher
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
			return // maybe reconnect here
		}
	}
}

type credentials struct {
    ClientEmail  string `json:"client_email"`
    PrivateKey   string `json:"private_key"`
}

func getSignedURLHandler(w http.ResponseWriter, r *http.Request) {
    ctx := context.Background()
    bucketName := "game-assets-bucket-egyptian-ratscrew"
    objectName := r.URL.Query().Get("file")

    creds, err := google.CredentialsFromJSON(ctx, []byte(os.Getenv("GOOGLE_APPLICATION_CREDENTIALS")), storage.ScopeReadOnly)
    if err != nil {
        http.Error(w, "Cannot load credentials from environment", http.StatusInternalServerError)
        return
    }

    var credsData credentials
    err = json.Unmarshal(creds.JSON, &credsData)
    if err != nil {
        http.Error(w, "Failed to parse credentials", http.StatusInternalServerError)
        return
    }

    client, err := storage.NewClient(ctx, option.WithCredentials(creds))
    if err != nil {
        http.Error(w, "Cannot create storage client", http.StatusInternalServerError)
        return
    }
    defer client.Close()

    url, err := client.Bucket(bucketName).SignedURL(objectName, &storage.SignedURLOptions{
        GoogleAccessID: credsData.ClientEmail,
        PrivateKey:     []byte(credsData.PrivateKey),
        Method:         "GET",
        Expires:        time.Now().Add(15 * time.Minute),
    })
    if err != nil {
        http.Error(w, "Cannot create signed URL", http.StatusInternalServerError)
        return
    }

    http.Redirect(w, r, url, http.StatusFound)
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

    http.HandleFunc("/get_signed_url", getSignedURLHandler)
    fmt.Println("HTTP server starting on port 8081...")
    if err := http.ListenAndServe(":8081", nil); err != nil {
        fmt.Printf("Error starting server: %v\n", err)
    }
}
