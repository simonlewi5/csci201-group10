package db

import (
	"errors"

	"github.com/simonlewi5/csci201-group10/game-server/internal/models"
)

func (s *serviceImpl) NewPlayer(player *models.Player) error {
	_, err := DB.Exec(`
		INSERT INTO players (id, username, firebase_uid, email)
		VALUES (?, ?, ?, ?)
	`, player.ID, player.Username, player.FirebaseUID, player.Email)
	return err
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
    err := DB.QueryRow(`
        SELECT id, username, firebase_uid, email, games_played, games_won, games_lost, total_score
        FROM players
        WHERE firebase_uid = ?
    `, token).Scan(&player.ID, &player.Username, &player.FirebaseUID, &player.Email, &player.GamesPlayed, &player.GamesWon, &player.GamesLost, &player.TotalScore)
    return player, err
}

func (s *serviceImpl) getPlayerByUsernameAndPassword(username, password string) (*models.Player, error) {
    player := &models.Player{}
    err := s.db.QueryRow(`
        SELECT id, username, firebase_uid, email, games_played, games_won, games_lost, total_score
        FROM players
        WHERE username = ? AND password_hash = SHA2(?, 256)
    `, username, password).Scan(&player.ID, &player.Username, &player.FirebaseUID, &player.Email, &player.GamesPlayed, &player.GamesWon, &player.GamesLost, &player.TotalScore)
    return player, err
}

func (s *serviceImpl) getPlayerByEmailAndPassword(email, password string) (*models.Player, error) {
    player := &models.Player{}
    err := s.db.QueryRow(`
        SELECT id, username, firebase_uid, email, games_played, games_won, games_lost, total_score
        FROM players
        WHERE email = ? AND password_hash = SHA2(?, 256)
    `, email, password).Scan(&player.ID, &player.Username, &player.FirebaseUID, &player.Email, &player.GamesPlayed, &player.GamesWon, &player.GamesLost, &player.TotalScore)
    return player, err
}

func (s *serviceImpl) RecordMatchEnd(match *models.Match) error {
    tx, err := DB.Begin()
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
        _, err = updateStmt.Exec(boolToInt(won), boolToInt(!won), player.CurrScore, player.ID)
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