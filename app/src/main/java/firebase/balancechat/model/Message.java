package firebase.balancechat.model;

import java.util.Date;

public class Message {

    private String sender;
    private String message;
    private Boolean multimedia = false;
    private String contentType = "";
    private String contentLocation = "";
    private long timestamp;

    public Message() {

    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    //Constructor for plain text message
    public Message(String sender, String message, String time) {
        this.sender = sender;
        this.message = message;
        timestamp = new Date().getTime();
        this.multimedia = false;
    }

    //Constructor for Multimedia message
    public Message(String sender, String message, String contentType, String contentLocation, String time) {
        this.sender = sender;
        this.message = message;
        this.multimedia = true;
        this.contentType = contentType;
        timestamp = new Date().getTime();
        this.contentLocation = contentLocation;
    }

    public String getSender() {
        return sender;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getContentLocation() {
        return contentLocation;
    }

    public Boolean getMultimedia() {
        return multimedia;
    }

    public String getContentType() {
        return contentType;
    }
}
