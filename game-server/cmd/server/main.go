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
    log.Printf("Request for signed URL of file: %s", objectName) // Log the requested file name

    creds, err := google.CredentialsFromJSON(ctx, []byte(os.Getenv("GOOGLE_APPLICATION_CREDENTIALS")), storage.ScopeReadOnly)
    if err != nil {
        log.Printf("Error loading credentials: %v", err) // Log this error
        http.Error(w, "Cannot load credentials from environment", http.StatusInternalServerError)
        return
    }

    var credsData credentials
    err = json.Unmarshal(creds.JSON, &credsData)
    if err != nil {
        log.Printf("Error parsing credentials JSON: %v", err) // Log parsing error
        http.Error(w, "Failed to parse credentials", http.StatusInternalServerError)
        return
    }

    client, err := storage.NewClient(ctx, option.WithCredentials(creds))
    if err != nil {
        log.Printf("Error creating storage client: %v", err) // Log client creation error
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
        log.Printf("Error creating signed URL: %v", err) // Log URL creation error
        http.Error(w, "Cannot create signed URL", http.StatusInternalServerError)
        return
    }

    http.Redirect(w, r, url, http.StatusFound)
}


func main() {
	dbService = db.SetupDatabase()
	matcher = matchmaking.NewMatcher(dbService)
	go matcher.StartMatching()

	// Start WebSocket server
	http.HandleFunc("/ws", websocketHandler(dbService))
	go func() {
		fmt.Println("WebSocket server starting on port 8080...")
		if err := http.ListenAndServe(":8080", nil); err != nil {
			log.Fatalf("Failed to start WebSocket server: %v", err)
		}
	}()

	// Start HTTP server
	http.HandleFunc("/get_signed_url", getSignedURLHandler)
	go func() {
		fmt.Println("HTTP server starting on port 8081...")
		if err := http.ListenAndServe(":8081", nil); err != nil {
			log.Fatalf("Failed to start HTTP server: %v", err)
		}
	}()

    select {}
}
