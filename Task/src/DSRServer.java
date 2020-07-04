import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.time.LocalTime;
import java.util.*;


public class DSRServer {
    // TODO: make a enum with integer
/*    enum InfoField {
        TYPE, SRC, DST, SENTTIME, MSG, PATH
    }*/

    final static int PORT = 5008;
    final static String SPLITER = "/";
    final static String PATH_SPLITER = "-";
    final static String TAG_RCV = "[RCV]";
    final static String TAG_SEND = "[SEND]";
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

    public static void main(String[] args) throws IOException {
        DatagramSocket serverSocket = new DatagramSocket(PORT);
        HashSet<String> receivedMessages = new HashSet<>();
        HashSet<String> neighbors = new HashSet<>();
        HashMap<String, String> dstPathMap = new HashMap<>(); // K: dst, V: path
        byte[] receiveBuffer = new byte[1024];
        byte[] sendBuffer = new byte[1024];

        myIP = getIP("en0");
        logName = myIP + ".txt";
        File log = new File(logName);
        System.out.println("Delete previous log: " + log.delete());

        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            serverSocket.receive(receivePacket);
            String receiveTime = LocalTime.now().toString(); // 接收时间
            String receiveData = new String(receivePacket.getData());
            writeToLog(TAG_RCV + receiveTime + receiveData);

            // if the message has not been received before, then broadcast it
            if (!receivedMessages.contains(receiveData)) {
                // receivedMessages.add(receiveData);
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
                        String replyData = buildData(MSG_TYPE[1], myIP, dataSrc, LocalTime.now().toString(), dataMsg, replyPath); // 1: RREP
                        sendBuffer = replyData.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, prevInetAddress, PORT);
                        serverSocket.send(sendPacket);
                        dstPathMap.put(dataSrc, reversePath(replyPath));
                        print(dstPathMap);
                    } else { // not destination, broadcast the message the myIP added to path
                        String addedPath = dataPath + PATH_SPLITER + myIP;
                        String broadcastData = buildData(dataType, dataSrc, dataDst, srcTime, dataMsg, addedPath);
                        sendBuffer = broadcastData.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(BROADCAST_IP), PORT);
                        serverSocket.setBroadcast(true);
                        serverSocket.send(sendPacket);
                        serverSocket.setBroadcast(false); // disable Broadcast after broadcast
                    }
                } else if (dataType.equals(MSG_TYPE[1])) { // RREP
                    if (dataDst.equals(myIP)) {
                        // received RREP
                        writeToLog(TAG_RCV + receiveData);
                        // add path to map
                        dstPathMap.put(dataSrc, dataPath);
                        print(dstPathMap);
                    } else {
                        String forwardIP = findReverseForwardIP(dataPath, myIP);
                        sendBuffer = receivePacket.getData();
                        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(forwardIP), PORT);
                        serverSocket.send(sendPacket);
                    }
                } else { // DATA
                    if (dataDst.equals(myIP)) {
                        writeToLog(TAG_RCV + receiveData);
                    } else {
                        String forwardIP = findForwardIP(dataPath, myIP);
                        sendBuffer = receivePacket.getData();
                        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(forwardIP), PORT);
                        serverSocket.send(sendPacket);
                    }
                }
            } else {
                writeToLog("Message Duplicated, will not broadcast");
            }
        }
    }

    private static void writeToLog(String tag) throws IOException {
        System.out.println(tag);
        FileOutputStream fos = new FileOutputStream(logName, true);
        fos.write(tag.getBytes());
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

    public static void print(Set<String> set) {
        System.out.println("All neighbors:");
        for (String s : set) {
            System.out.println(s);
        }
    }

    public static void print(Map<String, String> map) {
        System.out.println("All forward tables");
        Set<Map.Entry<String, String>> set = map.entrySet();
        Iterator entryIterator = map.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, String> mapElement = (Map.Entry) entryIterator.next();
            System.out.println("Destination: " + mapElement.getKey());
            System.out.println("Path: " + mapElement.getValue());
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

    public static String buildData(String dataType, String dataSrc, String dataDst, String dataMsg, String srcTime, String path) {
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
    public  static String findReverseForwardIP(String path, String my) {
        // DONE: finish reverse Forward IP
        String[] addr = path.split(PATH_SPLITER);
        for (int i = addr.length - 1; i >= 0 ; i -= 1) {
            if (myIP.equals(addr[i])) {
                return addr[i - 1];
            }
        }
        return null;
    }
}

