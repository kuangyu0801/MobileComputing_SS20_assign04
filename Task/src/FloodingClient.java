import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.time.LocalTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;

public class FloodingClient {
    final static String[] INET_NAME = {"en0", "wlan0"};
    final static int PORT = 5008;
    final static String SPLITER = "/";
    final static String PATH_SPLITER = "-";
    final static String TAG_RCV = "[RCV]";
    final static String TAG_SEND = "[SND]";
    final static String TAG_BROADCASET = "[BRD]";
    final static String TAG_TIME = "[TIME]";
    final static String BROADCAST_IP = "192.168.210.255";
    final static String[] ALL_ADDRS = {
            "192.168.210.174", "192.168.210.180", "192.168.210.185",
            "192.168.210.196", "192.168.210.197"};
    // 0: type, 1: src, 2: dst, 3: time, 4: msg, 5: path
    final static int[] INFO_FIELD = {0, 1, 2, 3, 4, 5};
    final static String[] MSG_TYPE = {"RREQ", "RREP", "DATA", "FLOD"};
    private static String myIP;
    private static String logName;
    private DatagramSocket clientSocket;
    private HashSet<String> receivedMessages;
    private HashSet<String> neighbors;
    private HashMap<String, String> dstPathMap;

    public static String getIP(String name) throws SocketException {
        NetworkInterface en0 = NetworkInterface.getByName(name);
        Enumeration<InetAddress> inetAddresses = en0.getInetAddresses();
        inetAddresses.nextElement();
        InetAddress inetAddress = inetAddresses.nextElement();
        return inetAddress.getHostAddress();
    }

    public FloodingClient(String inet_name) throws SocketException {
        clientSocket = new DatagramSocket(PORT);
        receivedMessages = new HashSet<>();
        neighbors = new HashSet<>();
        dstPathMap = new HashMap<>(); // K: dst, V: path
        myIP = getIP(inet_name);
        logName = myIP + "flood_.txt";
        File log = new File(logName);
        System.out.println("Delete previous log: " + log.delete());
    }

    private static void writeToLog(String tag) throws IOException {
        System.out.println(tag);
        FileOutputStream fos = new FileOutputStream(logName, true);
        fos.write(tag.getBytes());
        fos.write("\r".getBytes());
        fos.close();
    }

    private static void writeToLog(String tag, String addrres, String content) throws IOException {
        String text = LocalTime.now() + " | " + tag + " | " + addrres + " | " + content;
        System.out.println(text);
        FileOutputStream fos = new FileOutputStream(logName, true);
        fos.write(text.getBytes());
        fos.write("\r".getBytes());
        fos.close();
    }

    public static String buildData(String dataType, String dataSrc, String dataDst, String srcTime, String dataMsg, String nbrTime) {
        String replyData = dataType + SPLITER
                + dataSrc + SPLITER
                + dataDst + SPLITER
                + srcTime + SPLITER
                + dataMsg + SPLITER
                + nbrTime;
        return replyData;
    }

    private void startFlooding(String dst, String msg) throws IOException {
        writeToLog("Server Started Sending");
        clientSocket.setBroadcast(true);
        byte[] sendBuffer;
        String srcTime = LocalTime.now().toString();
        String broadcastData = buildData(MSG_TYPE[3], myIP, dst, srcTime, msg, srcTime);
        sendBuffer = broadcastData.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(BROADCAST_IP), PORT);
        clientSocket.setBroadcast(true);
        clientSocket.send(sendPacket);
        writeToLog(TAG_BROADCASET, BROADCAST_IP, broadcastData);
        clientSocket.close();
    }

    public static void main(String[] args) throws IOException {
        // 0 for PC, 1 for RaspberryPi
        FloodingClient floodingClient = new FloodingClient(INET_NAME[Integer.parseInt(args[0])]);
        // 0~5 for different address
        floodingClient.startFlooding(ALL_ADDRS[Integer.parseInt(args[1])], "Hello World!");
    }
}
