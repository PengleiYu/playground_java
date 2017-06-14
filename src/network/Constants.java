package network;

/**
 * Created by yupenglei on 17/6/8.
 */
public class Constants {
    public static final String SEPARATOR = " ";
    public static final String ACTION_END = "ACTION_END";
    public static final String ACTION_DOWNLOAD_LARGE = "ACTION_DOWNLOAD_LARGE";
    public static final String ACTION_DOWNLOAD_LITTLE = "ACTION_DOWNLOAD_LITTLE";
    public static final String ACTION_UPLOAD = "ACTION_UPLOAD";

    public static final String ACTION_CHAT = "ACTION_CHAT";
    public static final String MSG_CHAT_NO_RESPOND = "MSG_CHAT_NO_RESPOND";
    public static final String MSG_CHAT_NEED_RESPOND = "MSG_CHAT_NEED_RESPOND";


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
