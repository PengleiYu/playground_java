package network;

/**
 * Created by yupenglei on 17/6/8.
 */
public class Constants {
    public static final String SEPARATOR = " ";
    public static final String ACTION_END = "action_end";
    public static final String ACTION_DOWNLOAD_LARGE = "ACTION_DOWNLOAD_LARGE";
    public static final String ACTION_DOWNLOAD_LITTLE = "ACTION_DOWNLOAD_LITTLE";
    public static final String ACTION_UPLOAD = "action_upload";

    public static final String ACTION_CHAT = "action_chat";
    public static final String ACTION_CHAT_NO_RESPOND = "action_chat_no_respond";
    public static final String ACTION_CHAT_NEED_RESPOND = "action_chat_need_respond";


    public static void printWrite(String msg) {
        threadPrint("printWrite ==> " + msg);
    }

    public static void printRead(String msg) {
        threadPrint("printRead <== " + msg);
    }

    private static void threadPrint(String msg) {
        long id = Thread.currentThread().getId();
        System.out.println(String.format("Thread %s: %s", id, msg));
    }
}
