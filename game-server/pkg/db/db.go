package db

import (
	"database/sql"
	"fmt"
	"log"
	"os"

	_ "github.com/go-sql-driver/mysql"
    "github.com/simonlewi5/csci201-group10/game-server/pkg/models"
)

type DBService interface {
    RecordMatchEnd(match *models.Match) error
    NewPlayer(player *models.Player) error
    GetPlayer(credentials *models.Credentials) (*models.Player, error)
    HandleRegisterEmailAndPassword(credentials *models.Credentials) (*models.Player, error)
}

type serviceImpl struct {
    db *sql.DB
}

func NewService(db *sql.DB) DBService {
    return &serviceImpl{db: db}
}

var DB *sql.DB

func SetupDatabase() DBService {
    dbUser := os.Getenv("DB_USER")
    dbPass := os.Getenv("DB_PASS")
    dbName := os.Getenv("DB_NAME")
    dbHost := os.Getenv("DB_HOST")
    dsn := fmt.Sprintf("%s:%s@tcp(%s)/%s?parseTime=true", dbUser, dbPass, dbHost, dbName)

    db, err := sql.Open("mysql", dsn)
    if err != nil {
        log.Fatalf("Could not connect to database: %v", err)
    }
    if err = db.Ping(); err != nil {
        log.Fatalf("Could not ping database: %v", err)
    }

    return NewService(db)
}