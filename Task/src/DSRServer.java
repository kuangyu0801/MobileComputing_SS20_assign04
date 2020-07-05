import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.time.LocalTime;
import java.util.*;

public class DSRServer {
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

    DSRServer(String inet_name) throws SocketException {
        serverSocket = new DatagramSocket(PORT);
        receivedMessages = new HashSet<>();
        neighbors = new HashSet<>();
        dstPathMap = new HashMap<>(); // K: dst, V: path
        myIP = getIP(inet_name);
        logName = myIP + ".txt";
        File log = new File(logName);
        System.out.println("Delete previous log: " + log.delete());
    }

    public void DSR() throws IOException {
        writeToLog("Enter DSR");
        byte[] receiveBuffer = new byte[1024];
        byte[] sendBuffer = new byte[1024];

        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        serverSocket.receive(receivePacket);
        LocalTime receivedTime = LocalTime.now();
        String receiveData = new String(receivePacket.getData());
        receiveData = receiveData.trim();
        writeToLog(TAG_RCV, receivePacket.getAddress().toString(), receiveData);
        print(receivedMessages);

        // if the message has not been received before, then broadcast it
        if (!receivedMessages.contains(filterOutPath(receiveData))) {
            receivedMessages.add(filterOutPath(receiveData));
            writeToLog("DSR[Add Message] " + receiveData);
            print(receivedMessages);

            String dataType = getInfo(receiveData, 0);
            String dataSrc = getInfo(receiveData, 1);
            String dataDst = getInfo(receiveData, 2);
            String srcTime = getInfo(receiveData, 3);
            String dataMsg = getInfo(receiveData, 4);
            String dataPath = getInfo(receiveData, 5);
            InetAddress prevInetAddress = receivePacket.getAddress();

            if (dataType.equals(MSG_TYPE[0])) {
                // RREQ reach destination and only reply the first received RREQ
                if (dataDst.equals(myIP)) {
                    String replyPath = dataPath + PATH_SPLITER + myIP;
                    // RREP carries the origianl RREQ srcTime for calculating route discovery time
                    String replyData = buildData(MSG_TYPE[1], myIP, dataSrc, srcTime, dataMsg, replyPath); // 1: RREP
                    sendBuffer = replyData.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, prevInetAddress, PORT);
                    serverSocket.send(sendPacket);
                    dstPathMap.put(dataSrc, reversePath(replyPath));
                    print(dstPathMap);
                } else { // not destination, broadcast the message the myIP added to path
                    writeToLog("DSR" + TAG_RCV, prevInetAddress.toString(), receiveData);
                    String addedPath = dataPath + PATH_SPLITER + myIP;
                    String broadcastData = buildData(dataType, dataSrc, dataDst, srcTime, dataMsg, addedPath);
                    sendBuffer = broadcastData.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(BROADCAST_IP), PORT);
                    serverSocket.setBroadcast(true);
                    serverSocket.send(sendPacket);
                    writeToLog("DSR" + TAG_BROADCASET, BROADCAST_IP, broadcastData);
                    serverSocket.setBroadcast(false); // disable Broadcast after broadcast
                }
            } else if (dataType.equals(MSG_TYPE[1])) { // RREP
                if (dataDst.equals(myIP)) {
                    // received RREP
                    writeToLog(TAG_RCV, prevInetAddress.toString(), receiveData);
                    // add path to map
                    dstPathMap.put(dataSrc, dataPath);
                    LocalTime sentTime = LocalTime.parse(srcTime);
                    Long latency = receivedTime.toNanoOfDay() - sentTime.toNanoOfDay();
                    writeToLog("[Discovery Time]" + dataSrc + "-" + myIP + ":" + latency);
                    print(dstPathMap);
                } else {
                    String forwardIP = findReverseForwardIP(dataPath, myIP);
                    sendBuffer = receivePacket.getData();
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(forwardIP), PORT);
                    serverSocket.send(sendPacket);
                    writeToLog(TAG_SEND, forwardIP, receiveData);
                }
            } else { // DATA
                if (dataDst.equals(myIP)) {
                    writeToLog(TAG_RCV, prevInetAddress.toString(), receiveData);
                } else {
                    String forwardIP = findForwardIP(dataPath, myIP);
                    sendBuffer = receivePacket.getData();
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(forwardIP), PORT);
                    serverSocket.send(sendPacket);
                    writeToLog(TAG_SEND, forwardIP, receiveData);
                }
            }
        } else {
            writeToLog("Message Duplicated, will not broadcast");
            print(receivedMessages);
        }
    }

    private void listen() throws IOException {

        while (true) {
            DSR();
        }
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

    public static void print(Set<String> set) throws IOException {
        writeToLog("[All Received Messages] ");
        writeToLog("Number of message: " + set.size());
        for (String s : set) {
            writeToLog(s);
        }
    }

    public static void print(Map<String, String> map) throws IOException {
        System.out.println("All forward tables");
        Set<Map.Entry<String, String>> set = map.entrySet();
        Iterator<Map.Entry<String, String>> entryIterator = map.entrySet().iterator();
        writeToLog("printing Forward Table: ");
        while (entryIterator.hasNext()) {
            Map.Entry<String, String> mapElement = (Map.Entry<String, String>) entryIterator.next();
            System.out.println("Destination: " + mapElement.getKey());
            System.out.println("Path: " + mapElement.getValue());
            writeToLog("Destination: " + mapElement.getKey());
            writeToLog("Path: " + mapElement.getValue());
        }
    }

    public static String getInfo(String data, int field) {
        // 0: type, 1: src, 2: dst, 3: time, 4: msg, 5: path
        String[] info = data.split(SPLITER);
        return info[field];
    }

    public static String reversePath(String path) {
        String[] addr = path.split(PATH_SPLITER);
        // DONE: finish reverse path
        int size = addr.length;
        StringBuilder reversePath = new StringBuilder();
        for (int i = size - 1; i >= 0; i -= 1) {
            // TODO: find a smarter way to put all strings at once
            reversePath.append(addr[i]);
            if (i != 0) {
                reversePath.append(PATH_SPLITER);
            }
        }
        return reversePath.toString();
    }

    public static String buildData(String dataType, String dataSrc, String dataDst, String srcTime, String dataMsg, String path) {
        String replyData = dataType + SPLITER
                + dataSrc + SPLITER
                + dataDst + SPLITER
                + srcTime + SPLITER
                + dataMsg + SPLITER
                + path;
        return replyData;
    }

    // ex. we are F, {S_E_F_J_D} -> J
    public static String findForwardIP(String path, String myIP) {
        // DONE: finish find Forward IP
        String[] addr = path.split(PATH_SPLITER);
        for (int i = 0; i < addr.length; i += 1) {
            if (myIP.equals(addr[i])) {
                return addr[i + 1];
            }
        }
        return null;
    }

    // ex. we are F, {S_E_F_J_D} -> E
    public static String findReverseForwardIP(String path, String myIP) {
        // DONE: finish reverse Forward IP
        String[] addr = path.split(PATH_SPLITER);
        for (int i = addr.length - 1; i >= 0 ; i -= 1) {
            if (myIP.equals(addr[i])) {
                return addr[i - 1];
            }
        }
        return null;
    }

    public static String filterOutPath(String data) {
        String[] info = data.split(SPLITER);
        return info[0] + SPLITER + info[1] + SPLITER + info[2] + SPLITER+ info[3] + SPLITER + info[4];
    }

    public static void main(String[] args) throws IOException {
        // 0 for PC, 1 for RaspberryPi
        DSRServer myServer = new DSRServer(INET_NAME[Integer.parseInt(args[0])]);
        myServer.listen();

    }
}
