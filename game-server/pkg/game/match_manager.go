package game

import (
    "sync"

    "github.com/simonlewi5/csci201-group10/game-server/pkg/models"
)

type MatchManager struct {
    Matches map[string]*models.Match
    PlayerMatchMap map[string]string
    lock sync.RWMutex
}

func NewMatchManager() *MatchManager {
    return &MatchManager{
        Matches: make(map[string]*models.Match),
        PlayerMatchMap: make(map[string]string),
    }
}

func (m *MatchManager) CreateMatch(players []*models.Player) *models.Match {
    match := models.NewMatch(players)
    m.lock.Lock()
    defer m.lock.Unlock()
    m.Matches[match.ID] = match
    for _, player := range players {
        m.PlayerMatchMap[player.ID] = match.ID
    }
    return match
}

func (m *MatchManager) GetMatchForPlayer(playerID string) *models.Match {
    m.lock.RLock()
    defer m.lock.RUnlock()
    matchID := m.PlayerMatchMap[playerID]
    return m.Matches[matchID]
}