package com.example.app.Controllers.Email;

public class Inbox {
    private final String sender;
    private final String subject;
    private final String snippet;
    private final String date;

    public Inbox(String sender, String subject, String snippet, String date) {
        this.sender = sender;
        this.subject = subject;
        this.snippet = snippet;
        this.date = date;
    }

    public String getSender() { return sender; }
    public String getSubject() { return subject; }
    public String getSnippet() { return snippet; }
    public String getDate() { return date; }
}
