package game


import "github.com/simonlewi5/csci201-group10/game-server/pkg/models"

type PlayerContext struct {
    Player *models.Player
    CurrentMatch *models.Match
}

func NewPlayerContext(player *models.Player) *PlayerContext {
    return &PlayerContext{
        Player: player,
    }
}
