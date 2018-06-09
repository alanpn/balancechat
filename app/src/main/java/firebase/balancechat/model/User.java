package firebase.balancechat.model;

public class User {

    private String username;
    private String email;
    private String uid;
    private String phoneNumber;
    private String providerId;
    private String profilePicLocation;


    public User(){

    }

    public User(String username, String email, String uid, String phoneNumber, String providerId){
        this.username = username;
        this.email = email;
        this.uid = uid;
        this.phoneNumber = phoneNumber;
        this.providerId = providerId;

    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getUid() {
        return uid;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getProfilePicLocation() {
        return profilePicLocation;
    }

}