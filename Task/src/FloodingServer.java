import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.time.LocalTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class FloodingServer {
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
    final static String[] MSG_TYPE = {"RREQ", "RREP", "DATA"};
    private static String myIP;
    private static String logName;
    private DatagramSocket serverSocket;
    private HashSet<String> receivedMessages;
    private HashSet<String> neighbors;
    private HashMap<String, String> dstPathMap;

    public FloodingServer(String inet_name) throws SocketException {
        serverSocket = new DatagramSocket(PORT);
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

    public static String getIP(String name) throws SocketException {
        NetworkInterface en0 = NetworkInterface.getByName(name);
        Enumeration<InetAddress> inetAddresses = en0.getInetAddresses();
        inetAddresses.nextElement();
        InetAddress inetAddress = inetAddresses.nextElement();
        return inetAddress.getHostAddress();
    }

    public static void print(Set<String> set, String tag) throws IOException {
        writeToLog(tag);
        writeToLog("Number of element: " + set.size());
        for (String s : set) {
            writeToLog(s);
        }
    }

    public static String getInfo(String data, int field) {
        // 0: type, 1: src, 2: dst, 3: time, 4: msg, 5: path
        String[] info = data.split(SPLITER);
        return info[field];
    }

    public static String filterOutPath(String data) {
        String[] info = data.split(SPLITER);
        return info[0] + SPLITER + info[1] + SPLITER + info[2] + SPLITER+ info[3];
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

    private void listen() throws IOException {
        writeToLog("Server Started Listening");
        while (true) {
            writeToLog("Listening...");
            byte[] receiveBuffer = new byte[1024];
            byte[] sendBuffer = new byte[1024];

            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            serverSocket.receive(receivePacket);
            LocalTime receivedTime = LocalTime.now();
            String receiveData = new String(receivePacket.getData());
            receiveData = receiveData.trim();
            InetAddress senderIP = receivePacket.getAddress();
            writeToLog(TAG_RCV, senderIP.toString(), receiveData);

            if (!receivedMessages.contains(filterOutPath(receiveData))) {
                receivedMessages.add(filterOutPath(receiveData));
                writeToLog("[Add Message] " + receiveData);
                print(receivedMessages, "[All Messages]");

                String dataType = getInfo(receiveData, 0);
                String dataSrc = getInfo(receiveData, 1);
                String dataDst = getInfo(receiveData, 2);
                String srcTime = getInfo(receiveData, 3);
                String dataMsg = getInfo(receiveData, 4);
                String nbrTime = getInfo(receiveData, 5);
                LocalTime sentTime = LocalTime.parse(nbrTime);
                Long latency = receivedTime.toNanoOfDay() - sentTime.toNanoOfDay();
                writeToLog("Delay time between" + senderIP + "-" + myIP + ":" + latency);
                neighbors.add(senderIP.toString());
                print(neighbors, "[All Neighbors]");
                if (dataDst.equals(myIP)) { // Broadcast reach destination
                    writeToLog("FLOODING TOKEN reached destination");
                } else { // not destination, broadcast with msg updated to current time
                    // msg is sent time with broadcast
                    String broadcastData = buildData(dataType, dataSrc, dataDst, srcTime, dataMsg, LocalTime.now().toString());
                    sendBuffer = broadcastData.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(BROADCAST_IP), PORT);
                    serverSocket.setBroadcast(true);
                    serverSocket.send(sendPacket);
                    writeToLog(TAG_BROADCASET, BROADCAST_IP, broadcastData);
                }
            } else {
                writeToLog("Message Duplicated, will not broadcast");
                print(receivedMessages, "[All Messages]");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        FloodingServer floodingServer = new FloodingServer(INET_NAME[Integer.parseInt(args[0])]);
        floodingServer.listen();
    }
}
