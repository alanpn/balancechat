package firebase.balancechat.util;

public final class Constants {

    /*DB*/
    public static final String USER_CHILD = "users";
    public static final String FRIEND_CHILD = "friends";
    public static final String CHAT_CHILD = "chats";
    public static final String MESSAGE_CHILD = "messages";
    public static final String PROFILE_PICTURE_LOCATION_CHILD = "profilePicLocation";
//    public static final String PROFILE_PICTURE_LOCATION_CHILD = "photoUrl";
    public static final String PROFILE_PICTURE_PATH = "gs://balancechat-6f37f.appspot.com/Photos/profile_picture/";

    /*??*/
    public static final String MESSAGE_ID = "MID";
    public static final String CHAT_NAME = "CNAME";

    /*message*/
    public static final int MAX_MSG_LENGTH = 150;
    public static final int SIGN_IN_REQUEST_CODE = 123;
    public static final int REQUEST_INVITE = 1;
    public static final String MESSAGE_TIME_FORMAT = "h:mm a";
    public static final int GALLERY_INTENT = 2;

}
