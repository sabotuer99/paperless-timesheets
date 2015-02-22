package gov.wyo.paperless;

/**
 * Contains the client IDs and scopes for allowed clients consuming your API.
 */
public class Constants {
  public static final String APPLICATION_NAME = "paperless-timesheets";
  public static final String WEB_CLIENT_ID = "blah blah balh";
  public static final String SIMPLE_API_ACCESS_KEY = "";
  public static final String ANDROID_CLIENT_ID = "replace this with your Android client ID";
  public static final String IOS_CLIENT_ID = "replace this with your iOS client ID";
  public static final String ANDROID_AUDIENCE = WEB_CLIENT_ID;

  public static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
}
