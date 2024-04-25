package db

import (
	"errors"

	"github.com/google/uuid"
	"github.com/simonlewi5/csci201-group10/game-server/pkg/models"
	"golang.org/x/crypto/bcrypt"
)

func (s *serviceImpl) NewPlayer(player *models.Player) error {
	_, err := s.db.Exec(`
		INSERT INTO players (id, username, firebase_uid, email)
		VALUES (?, ?, ?, ?)
	`, player.ID, player.Username, player.FirebaseUID, player.Email)
	return err
}

func (s *serviceImpl) GetPlayerByID(id string) (*models.Player, error) {
    player := &models.Player{}
    err := s.db.QueryRow(`
        SELECT id, username, firebase_uid, email, games_played, games_won, games_lost, total_score
        FROM players
        WHERE id = ?
    `, id).Scan(&player.ID, &player.Username, &player.FirebaseUID, &player.Email, &player.GamesPlayed, &player.GamesWon, &player.GamesLost, &player.TotalScore)
    return player, err
}

func (s *serviceImpl) GetPlayer(credentials *models.Credentials) (*models.Player, error) {
    if credentials.Token != "" {
        return s.getPlayerByToken(credentials.Token)
    } else if credentials.Username != "" && credentials.Password != "" {
        // Handle username and password authentication
        return s.getPlayerByUsernameAndPassword(credentials.Username, credentials.Password)
    } else if credentials.Email != "" && credentials.Password != "" {
        // Handle email and password authentication
        return s.getPlayerByEmailAndPassword(credentials.Email, credentials.Password)
    }
    return nil, errors.New("invalid credentials")
}

func (s *serviceImpl) getPlayerByToken(token string) (*models.Player, error) {
    player := &models.Player{}
    err := s.db.QueryRow(`
        SELECT id, username, firebase_uid, email, games_played, games_won, games_lost, total_score
        FROM players
        WHERE firebase_uid = ?
    `, token).Scan(&player.ID, &player.Username, &player.FirebaseUID, &player.Email, &player.GamesPlayed, &player.GamesWon, &player.GamesLost, &player.TotalScore)
    return player, err
}

func (s *serviceImpl) getPlayerByUsernameAndPassword(username, password string) (*models.Player, error) {
    player := &models.Player{}
    var passwordHash string
    err := s.db.QueryRow(`
        SELECT id, username, firebase_uid, email, games_played, games_won, games_lost, total_score, password_hash
        FROM players
        WHERE username = ?
    `, username).Scan(&player.ID, &player.Username, &player.FirebaseUID, &player.Email, &player.GamesPlayed, &player.GamesWon, &player.GamesLost, &player.TotalScore, &passwordHash)
    if err != nil {
        return nil, err
    }
    if CheckPasswordHash(password, passwordHash) {
        return player, nil
    }
    return nil, errors.New("invalid password")
}

func (s *serviceImpl) getPlayerByEmailAndPassword(email, password string) (*models.Player, error) {
    player := &models.Player{}
    var passwordHash string
    err := s.db.QueryRow(`
        SELECT id, username, firebase_uid, email, games_played, games_won, games_lost, total_score, password_hash
        FROM players
        WHERE email = ?
    `, email).Scan(&player.ID, &player.Username, &player.FirebaseUID, &player.Email, &player.GamesPlayed, &player.GamesWon, &player.GamesLost, &player.TotalScore, &passwordHash)
    if err != nil {
        return nil, err
    }
    if CheckPasswordHash(password, passwordHash) {
        return player, nil
    }
    return nil, errors.New("invalid password")
}


func (s *serviceImpl) HandleRegisterEmailAndPassword(credentials *models.Credentials) (*models.Player, error) {
    
    if credentials.Username == "" || credentials.Email == "" || credentials.Password == "" {
        return nil, errors.New("missing required fields")
    }

    // Check if the username or email already exists
    var count int
    err := s.db.QueryRow(`
        SELECT COUNT(*)
        FROM players
        WHERE username = ? OR email = ?
    `, credentials.Username, credentials.Email).Scan(&count)
    if err != nil {
        return nil, errors.New("failed to check if username or email exists")
    }
    if count > 0 {
        return nil, errors.New("username or email already exists")
    }
    hashedPassword, err := HashPassword(credentials.Password)
    if err != nil {
        return nil, errors.New("failed to hash password")
    }

    uuid := generateUUID()
    _, err = s.db.Exec(`
        INSERT INTO players (id, username, email, password_hash)
        VALUES (?, ?, ?, ?)
    `, uuid, credentials.Username, credentials.Email, hashedPassword)
    if err != nil {
        return nil, errors.New("failed to create player")
    }
    player := &models.Player{
        ID:       uuid,
        Username: credentials.Username,
        Email:    credentials.Email,
        GamesPlayed: 0,
        GamesWon:    0,
        GamesLost:   0,
        TotalScore:  0,
    }

    return player, nil
}

func (s *serviceImpl) RecordMatchEnd(match *models.Match) error {
    tx, err := s.db.Begin()
    if err != nil {
        return err
    }

    defer func() {
        if err != nil {
            tx.Rollback()
        }
    }()

    updateStmt, err := tx.Prepare(`
        UPDATE players SET
        games_played = games_played + 1,
        games_won = games_won + ?,
        games_lost = games_lost + ?,
        total_score = total_score + ?
        WHERE id = ?
    `)
    if err != nil {
        return err
    }
    defer updateStmt.Close()

    for _, player := range match.Players {
        won := player.ID == match.Winner.ID
        playerScore := match.PlayerScores[player.ID]
        _, err = updateStmt.Exec(boolToInt(won), boolToInt(!won), playerScore, player.ID)
        if err != nil {
            return err
        }
    }

    _, err = tx.Exec(`
        INSERT INTO match_results (match_id, winner_id, start_time, end_time)
        VALUES (?, ?, ?, ?)
    `, match.ID, match.Winner.ID, match.StartTime, match.EndTime)
    if err != nil {
        return err
    }

    return tx.Commit()
}

func boolToInt(b bool) int {
    if b {
        return 1
    }
    return 0
}

// HashPassword creates a bcrypt hash of the password
func HashPassword(password string) (string, error) {
    bytes, err := bcrypt.GenerateFromPassword([]byte(password), 14)
    return string(bytes), err
}

// CheckPasswordHash compares a bcrypt hashed password with its possible plaintext equivalent
func CheckPasswordHash(password, hash string) bool {
    err := bcrypt.CompareHashAndPassword([]byte(hash), []byte(password))
    return err == nil
}
func generateUUID() string {
    return uuid.New().String()
}
