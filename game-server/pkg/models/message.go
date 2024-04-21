package models

type MessageType string

const (
	MessageTypeQueueUpdate MessageType = "QUEUE_UPDATE"
	MessageTypeMatchFound  MessageType = "MATCH_FOUND"
	MessageTypeMatchStart  MessageType = "MATCH_START"
	MessageTypeMatchEnd    MessageType = "MATCH_END"
	MessageTypeGameUpdate  MessageType = "GAME_UPDATE"
	MessageTypePlayerAction MessageType = "PLAYER_ACTION"
	MessageTypeAuthSuccess MessageType = "AUTH_SUCCESS"
	MessageTypeAuthError   MessageType = "AUTH_ERROR"
)

type Message struct {
	Type MessageType `json:"type"`
	Data interface{} `json:"data"`
}
