package models

type MessageType string

const (
	MessageTypeQueueUpdate  MessageType = "QUEUE_UPDATE"
	MessageTypeMatchFound   MessageType = "MATCH_FOUND"
	MessageTypeMatchStart   MessageType = "MATCH_START"
	MessageTypeMatchUpdate  MessageType = "MATCH_UPDATE"
	MessageTypeMatchEnd     MessageType = "MATCH_END"
	MessageTypeGameUpdate   MessageType = "GAME_UPDATE"
	MessageTypePlayerAction MessageType = "PLAYER_ACTION"
	MessageTypeAuthSuccess  MessageType = "AUTH_SUCCESS"
	MessageTypeAuthError    MessageType = "AUTH_ERROR"
	MessageTypeSlapFail 	MessageType = "SLAP_FAILURE"
	MessageTypeSlapSuccess 	MessageType = "SLAP_SUCCESS"
	MessageTypePlayerLost 	MessageType = "PLAYER_LOST"
)

type Message struct {
	Type MessageType `json:"type"`
	Data interface{} `json:"data"`
}
