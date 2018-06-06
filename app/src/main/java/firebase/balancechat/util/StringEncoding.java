package firebase.balancechat.util;

public class StringEncoding {

    public static String encodeString(String email) {
        return email.replace(".", ",");
    }

    public static String decodeString(String email) {
        return email.replace(",", ".");
    }
}
