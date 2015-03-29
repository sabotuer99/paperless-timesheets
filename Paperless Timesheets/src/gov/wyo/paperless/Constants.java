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
  public static final String SERVICE_ACCOUNT_ID = "391255808597-ncoveqtosl0ishta72bt26tvpa0it0lh@developer.gserviceaccount.com"; 
  //public static final String SERVICE_ACCOUNT_ID = "391255808597-ncoveqtosl0ishta72bt26tvpa0it0lh.apps.googleusercontent.com";

  public static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
  public static final String SPREADSHEET_FEED = "https://spreadsheets.google.com/feeds/spreadsheets/private/full";
  public static final String TIMECARD_GROUP = "paperless-timesheet-test@googlegroups.com";

  public static final String OAUTH_PROVIDER = "google";
}
