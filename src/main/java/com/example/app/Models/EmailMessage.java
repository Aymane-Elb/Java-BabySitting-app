package com.example.app.Models;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class EmailMessage {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty senderId = new SimpleIntegerProperty();
    private final IntegerProperty receiverId = new SimpleIntegerProperty();
    private final StringProperty subject = new SimpleStringProperty();
    private final StringProperty body = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> sendDate = new SimpleObjectProperty<>();
    private final BooleanProperty unread = new SimpleBooleanProperty(true); // DB: is_read (false means unread)
    private final BooleanProperty favorite = new SimpleBooleanProperty(false); // DB: is_favorite

    public EmailMessage(int id, int senderId, int receiverId, String subject,
                        String body, LocalDateTime sendDate, boolean isRead, boolean isFavorite) {
        this.id.set(id);
        this.senderId.set(senderId);
        this.receiverId.set(receiverId);
        this.subject.set(subject);
        this.body.set(body);
        this.sendDate.set(sendDate);
        this.unread.set(!isRead); // Invert
        this.favorite.set(isFavorite);
    }

    // Getters
    public int getId() { return id.get(); }
    public int getSenderId() { return senderId.get(); }
    public int getReceiverId() { return receiverId.get(); }
    public String getSubject() { return subject.get(); }
    public String getBody() { return body.get(); }
    public LocalDateTime getSendDate() { return sendDate.get(); }
    public boolean isUnread() { return unread.get(); }
    public boolean isFavorite() { return favorite.get(); }

    // Setters
    public void setId(int value) { id.set(value); }
    public void setSenderId(int value) { senderId.set(value); }
    public void setReceiverId(int value) { receiverId.set(value); }
    public void setSubject(String value) { subject.set(value); }
    public void setBody(String value) { body.set(value); }
    public void setSendDate(LocalDateTime value) { sendDate.set(value); }
    public void setUnread(boolean value) { unread.set(value); }
    public void setFavorite(boolean value) { favorite.set(value); }

    // Property accessors
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty senderIdProperty() { return senderId; }
    public IntegerProperty receiverIdProperty() { return receiverId; }
    public StringProperty subjectProperty() { return subject; }
    public StringProperty bodyProperty() { return body; }
    public ObjectProperty<LocalDateTime> sendDateProperty() { return sendDate; }
    public BooleanProperty unreadProperty() { return unread; }
    public BooleanProperty favoriteProperty() { return favorite; }

    public void toggleFavorite() {
        setFavorite(!isFavorite());
    }

    public String getPreview() {
        String fullBody = getBody();
        return fullBody.length() <= 100 ? fullBody : fullBody.substring(0, 97) + "...";
    }

    public boolean matchesSearch(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) return true;
        String term = searchTerm.toLowerCase();
        return getSubject().toLowerCase().contains(term) || getBody().toLowerCase().contains(term);
    }
}
