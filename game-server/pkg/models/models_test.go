package models

import (
    "testing"
)

func TestNewDeck(t *testing.T) {
    deck := NewDeck()
    if len(deck.Cards) != 52 {
        t.Errorf("Expected deck length of 52, but got %v", len(deck.Cards))
    }
}

func TestShuffle(t *testing.T) {
    deck := NewDeck()
    firstCard := deck.Cards[0]
    deck.Shuffle()
    if deck.Cards[0] == firstCard {
        t.Errorf("Expected first card to change after shuffle")
    }
}

func TestDrawCard(t *testing.T) {
    deck := NewDeck()
    firstCard := deck.Cards[0]
    drawnCard := deck.DrawCard()
    if drawnCard != firstCard {
        t.Errorf("Expected first card to be drawn")
    }
    if len(deck.Cards) != 51 {
        t.Errorf("Expected deck length of 51 after draw, but got %v", len(deck.Cards))
    }
}

func TestDrawCards(t *testing.T) {
    deck := NewDeck()
    drawnCards := deck.DrawCards(5)
    if len(drawnCards) != 5 {
        t.Errorf("Expected 5 cards to be drawn, but got %v", len(drawnCards))
    }
    if len(deck.Cards) != 47 {
        t.Errorf("Expected deck length of 47 after drawing 5 cards, but got %v", len(deck.Cards))
    }
}

func TestCheckSlappable(t *testing.T) {
    centerPile := CenterPile{Cards: []Card{{Value: 1, Suit: Spades}, {Value: 2, Suit: Hearts}}}
    if centerPile.CheckSlappable() {
        t.Errorf("Expected slappable check to fail with distinct card values")
    }
    centerPile.Cards = []Card{{Value: 1, Suit: Spades}, {Value: 1, Suit: Hearts}}
    if !centerPile.CheckSlappable() {
        t.Errorf("Expected slappable check to pass with same card values")
    }
}

func TestCheckFaceCardChallenge(t *testing.T) {
    centerPile := CenterPile{Cards: []Card{{Value: 11, Suit: Spades}, {Value: 2, Suit: Hearts}}}
    challenge, met := centerPile.CheckFaceCardChallenge()
    if !challenge || met {
        t.Errorf("Expected challenge to be issued and not met")
    }
    centerPile.Cards = []Card{{Value: 11, Suit: Spades}, {Value: 12, Suit: Hearts}}
    challenge, met = centerPile.CheckFaceCardChallenge()
    if !challenge || !met {
        t.Errorf("Expected challenge to be issued and met")
    }
}

func TestNewPlayer(t *testing.T) {
	player := NewPlayer("1", "testuser", "uid123", "test@example.com")
	if player.ID != "1" || player.Username != "testuser" || player.FirebaseUID != "uid123" || player.Email != "test@example.com" ||
		player.GamesPlayed != 0 || player.GamesWon != 0 || player.GamesLost != 0 {
		t.Errorf("NewPlayer did not initialize correctly")
	}
}

func TestUpdateStats(t *testing.T) {
	player := NewPlayer("1", "testuser", "uid123", "test@example.com")
	player.UpdateStats(true, 100) // Assume won
	if player.GamesPlayed != 1 || player.GamesWon != 1 || player.GamesLost != 0 {
		t.Errorf("UpdateStats did not update correctly on win")
	}
	player.UpdateStats(false, 50) // Assume lost
	if player.GamesPlayed != 2 || player.GamesWon != 1 || player.GamesLost != 1 {
		t.Errorf("UpdateStats did not update correctly on loss")
	}
}

func TestGetPlayerStats(t *testing.T) {
	player := NewPlayer("1", "testuser", "uid123", "test@example.com")
	stats := player.GetPlayerStats()
	if stats["games_played"] != 0 || stats["games_won"] != 0 || stats["games_lost"] != 0 {
		t.Errorf("GetPlayerStats did not return correct initial stats")
	}
	player.UpdateStats(true, 100)
	stats = player.GetPlayerStats()
	if stats["games_played"] != 1 || stats["games_won"] != 1 || stats["games_lost"] != 0 {
		t.Errorf("GetPlayerStats did not return correct updated stats")
	}
}

func TestGetPlayerProfile(t *testing.T) {
	player := NewPlayer("1", "testuser", "uid123", "test@example.com")
	profile := player.GetPlayerProfile()
	if profile["id"] != "1" || profile["username"] != "testuser" || profile["firebase_uid"] != "uid123" || profile["email"] != "test@example.com" {
		t.Errorf("GetPlayerProfile did not return correct information")
	}
}

func TestNewMatch(t *testing.T) {
    players := []*Player{NewPlayer("1", "Alice", "", "alice@example.com"), NewPlayer("2", "Bob", "", "bob@example.com")}
    match := NewMatch(players)
    if len(match.Players) != 2 {
        t.Errorf("Expected 2 players, got %d", len(match.Players))
    }
    if match.MatchState != MatchStateInProgress {
        t.Errorf("Expected MatchState to be 'IN_PROGRESS', got %s", match.MatchState)
    }
}

func TestPlayCard(t *testing.T) {
    players := []*Player{NewPlayer("1", "Alice", "", "alice@example.com"), NewPlayer("2", "Bob", "", "bob@example.com")}
    match := NewMatch(players)
    match.TurnIndex = 0
    err := match.PlayCard("1")
    if err != nil {
        t.Errorf("Failed to play card: %v", err)
    }

    hand := match.Hands["Alice"]
    if len(hand.Cards) != 25 {
        t.Errorf("Expected 25 cards in Alice's hand, got %d", len(hand.Cards))
    }
}

func TestAddPileToHand(t *testing.T) {
    players := []*Player{NewPlayer("1", "Alice", "", "alice@example.com"), NewPlayer("2", "Bob", "", "bob@example.com")}
    match := NewMatch(players)
    match.CenterPile.Cards = []Card{{Value: 10, Suit: Hearts}}

    match.AddPileToHand("1")
    hand := match.Hands["Alice"]
    if len(hand.Cards) != 27 {
        t.Errorf("Expected 27 cards in Alice's hand, got %d", len(hand.Cards))
    }
}

func TestPunishBadSlap(t *testing.T) {
    players := []*Player{NewPlayer("1", "Alice", "", "alice@example.com"), NewPlayer("2", "Bob", "", "bob@example.com")}
    match := NewMatch(players)

    match.PlayCard("1")
    match.PunishBadSlap("1")

    hand := match.Hands["Alice"]
    if len(hand.Cards) != 24 {
        t.Errorf("Expected 24 cards in Alice's hand after bad slap, got %d", len(hand.Cards))
    }
}

func TestCheckEndGame(t *testing.T) {
    players := []*Player{NewPlayer("1", "Alice", "", "alice@example.com"), NewPlayer("2", "Bob", "", "bob@example.com")}
    match := NewMatch(players)
    
    aliceHand := match.Hands["Alice"]
    aliceHand.Cards = []Card{}
    match.Hands["Alice"] = aliceHand
    
    match.DeadMansSlaps["Alice"] = 0
    
    if !match.CheckEndGame() {
        t.Errorf("Expected the game to end")
    }
}
