package main

import (
    "fmt"
    "net/http"
    "github.com/gorilla/websocket"
)

var upgrader = websocket.Upgrader{
    ReadBufferSize:  1024,
    WriteBufferSize: 1024,
}

func echo(w http.ResponseWriter, r *http.Request) {
    conn, err := upgrader.Upgrade(w, r, nil)
    if err != nil {
        fmt.Println("Error upgrading WebSocket:", err)
        return
    }
    defer conn.Close()

    for {
        mt, message, err := conn.ReadMessage()
        if err != nil {
            fmt.Println("Error reading message:", err)
            break
        }
        fmt.Printf("Received message: %s\n", message)

        if err := conn.WriteMessage(mt, message); err != nil {
            fmt.Println("Error writing message:", err)
            break
        }
    }
}

func main() {
    http.HandleFunc("/echo", echo)
    fmt.Println("WebSocket server starting on port 8080...")
    if err := http.ListenAndServe(":8080", nil); err != nil {
        fmt.Printf("Error starting server: %v\n", err)
    }
}

// package main

// import (
// 	"database/sql"
// 	"fmt"
// 	// "log"
// 	"os"

// 	_ "github.com/go-sql-driver/mysql"
// )

// func main() {
//     fmt.Println("Starting server...")

//     dbUser := os.Getenv("DB_USER")
//     dbPass := os.Getenv("DB_PASS")
//     // dbName := os.Getenv("DB_NAME")
//     dbHost := os.Getenv("DB_HOST")

//     // dsn := fmt.Sprintf("%s:%s@tcp(%s)/%s", dbUser, dbPass, dbHost, dbName)
//     dsn := fmt.Sprintf("%s:%s@tcp(%s)/mysql", dbUser, dbPass, dbHost)
//     fmt.Printf("Connecting to database with DSN: %s\n", dsn)
//     db, err := sql.Open("mysql", dsn)
//     if err != nil {
//         fmt.Printf("Error opening database: %v\n", err)
//         return
//     }
//     defer db.Close()

//     fmt.Println("Database connection opened.")

//     if err := db.Ping(); err != nil {
//         fmt.Printf("Error pinging database: %v\n", err)
//         return
//     }

//     fmt.Println("Connected to database.")
// }

// func main() {
// 	dbUser := os.Getenv("DB_USER")
// 	dbPass := os.Getenv("DB_PASS")
// 	dbName := os.Getenv("DB_NAME")
// 	dbHost := os.Getenv("DB_HOST")

// 	dsn := fmt.Sprintf("%s:%s@tcp(%s)/%s", dbUser, dbPass, dbHost, dbName)
// 	db, err := sql.Open("mysql", dsn)
// 	if err != nil {
// 		log.Fatalf("error opening database: %v", err)
// 	}
// 	defer db.Close()

// 	if err := db.Ping(); err != nil {
// 		log.Fatalf("error pinging database: %v", err)
// 	}

// 	log.Println("connected to database")

// 	// create table
// 	_, err = db.Exec(`CREATE TABLE IF NOT EXISTS players (
// 		id INT AUTO_INCREMENT PRIMARY KEY,
// 		username VARCHAR(255) NOT NULL,
// 		firebase_uid VARCHAR(255) NOT NULL,
// 		email VARCHAR(255) NOT NULL,
// 		games_played INT NOT NULL,
// 		games_won INT NOT NULL,
// 		games_lost INT NOT NULL,
// 		total_score INT NOT NULL
// 	)`)

// 	if err != nil {
// 		log.Fatalf("error creating players table: %v", err)
// 	}

// 	log.Println("created players table")

// 	rows, err := db.Query("SELECT id, username, firebase_uid, email, games_played, games_won, games_lost, total_score FROM players")
// 	if err != nil {
// 		log.Fatalf("error querying players table: %v", err)
// 	}
// 	defer rows.Close()

// 	for rows.Next() {
// 		var id, gamesPlayed, gamesWon, gamesLost, totalScore int
// 		var username, firebaseUID, email string

// 		err := rows.Scan(&id, &username, &firebaseUID, &email, &gamesPlayed, &gamesWon, &gamesLost, &totalScore)
// 		if err != nil {
// 			log.Fatal("error reading row: ", err)
// 		}

// 		log.Printf("Read player: ID=%d, Username=%s, FirebaseUID=%s, Email=%s, GamesPlayed=%d, GamesWon=%d, GamesLost=%d, TotalScore=%d\n",
// 			id, username, firebaseUID, email, gamesPlayed, gamesWon, gamesLost, totalScore)
// 	}

// 	if err = rows.Err(); err != nil {
// 		log.Fatal("error iterating rows: ", err)
// 	}

// 	log.Println("read operation completed")

// 	logFile, err := os.OpenFile("/var/log/game-server/game-server.log", os.O_CREATE|os.O_WRONLY|os.O_APPEND, 0666)
// 	if err != nil {
// 		log.Fatalf("error opening log file: %v", err)
// 	}
// 	defer logFile.Close()

// 	log.SetOutput(logFile)

// 	log.Println("server started")
// }


// import (
// 	"log"
// 	"net/http"
// 	// "os"

// 	socketio "github.com/googollee/go-socket.io"
// 	"github.com/simonlewi5/csci201-group10/game-server/internal/models"
// 	// "github.com/simonlewi5/csci201-group10/game-server/pkg/handlers"
// 	"github.com/simonlewi5/csci201-group10/game-server/pkg/matchmaking"
// )

// func main() {
// 	server := socketio.NewServer(nil)
	
// 	matcher := matchmaking.NewMatcher()

// 	server.OnConnect("/", func(s socketio.Conn) error {
// 		log.Println("connected:", s.ID())
// 		return nil
// 	})

// 	server.OnEvent("/", "joinMatchmaking", func(s socketio.Conn, userID string) {
		
// 		// dummy player creation
// 		player := models.NewPlayer(userID, "username", "firebaseUID", "email")
// 		matcher.AddPlayer(&player, s)
// 		log.Printf("Player %s joined matchmaking", player.ID)

// 	})

// 	server.OnDisconnect("/", func(s socketio.Conn, reason string) {
// 		log.Println("Disconnected: ", reason)
// 	})

// 	go func() {
// 		if err := server.Serve(); err != nil && err != http.ErrServerClosed {
// 			log.Fatalf("serve error: %v\n", err)
// 		}
// 	}()
// 	defer server.Close()

// 	http.Handle("/socket.io/", server)
// 	log.Println("Socket.IO server running at localhost:8000")
// 	if err := http.ListenAndServe(":8000", nil); err != nil {
// 		log.Fatalf("listen error: %v\n", err)
// 	}
// }
