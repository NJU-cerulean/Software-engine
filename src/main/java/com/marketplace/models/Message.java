package com.marketplace.models;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Message {
    private String messageId;
    private String senderId;
    private String receiverId;
    private List<String> content;
    private Date timestamp;

    public Message(String senderId, String receiverId, List<String> content) {
        this.messageId = "msg-" + UUID.randomUUID();
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = new Date();
    }

    public String getMessageId() { return messageId; }
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public List<String> getContent() { return content; }
    public Date getTimestamp() { return timestamp; }

    public void sendMessage() { /* persist via DAO */ }
    public void getConversation() { /* compose conversation */ }
}
