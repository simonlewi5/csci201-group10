package models

type Player struct {
	ID          string `json:"id"`
	Username    string `json:"username"`
	FirebaseUID string `json:"firebase_uid"`
	Email       string `json:"email"`
	GamesPlayed int    `json:"games_played"`
	GamesWon    int    `json:"games_won"`
	GamesLost   int    `json:"games_lost"`
}

type Credentials struct {
	Username string `json:"username"`
	Email    string `json:"email"`
	Password string `json:"password"`
	Token    string `json:"token"`
}

func NewPlayer(id, username, firebaseUID, email string) *Player {
	return &Player{
		ID:          id,
		Username:    username,
		FirebaseUID: firebaseUID,
		Email:       email,
		GamesPlayed: 0,
		GamesWon:    0,
		GamesLost:   0,
	}
}

func (p *Player) UpdateStats(won bool, score int) {
	p.GamesPlayed++
	if won {
		p.GamesWon++
	} else {
		p.GamesLost++
	}
}

func (p *Player) GetPlayerStats() map[string]int {
	return map[string]int{
		"games_played": p.GamesPlayed,
		"games_won":    p.GamesWon,
		"games_lost":   p.GamesLost,
	}
}

func (p *Player) GetPlayerProfile() map[string]string {
	return map[string]string{
		"id":           p.ID,
		"username":     p.Username,
		"firebase_uid": p.FirebaseUID,
		"email":        p.Email,
	}
}
