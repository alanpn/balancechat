package firebase.balancechat.model;

import java.util.Date;

import firebase.balancechat.util.Constants;
import tgio.rncryptor.RNCryptorNative;

public class Message {

    private RNCryptorNative RNCryptor = new RNCryptorNative();
    private String sender;
    private String message;
    private Boolean multimedia = false;
    private String contentType = null;
    private String contentLocation = null;
    private long timestamp;

    public Message() {

    }

    public Message(String sender, String message) {
        this.sender = sender;
        this.message = new String(RNCryptor.encrypt(message, Constants.ENCRYPTION_KEY));
        timestamp = new Date().getTime();
    }

    /* constructor for text */
    public Message(String sender, String message, String contentType) {
        this.sender = sender;
        this.message = new String(RNCryptor.encrypt(message, Constants.ENCRYPTION_KEY));
        this.contentType = contentType;
        timestamp = new Date().getTime();
    }

    /* constructor for multimedia */
    public Message(String sender, String message, String contentType, String contentLocation) {
        this.sender = sender;
        this.message = new String(RNCryptor.encrypt(message, Constants.ENCRYPTION_KEY));
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
