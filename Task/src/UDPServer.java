
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class UDPServer {
    final static int PORT = 5008;
    final static String SPLITER = "_";
    final static String TAG_RCV = "[RCV]";
    final static String TAG_SEND = "[SEND]";
    final static String BROADCAST_IP = "192.168.210.255";
    final static String[] ALL_ADDRS = {
            "192.168.210.174", "192.168.210.180", "192.168.210.185",
            "192.168.210.196", "192.168.210.197"};
    private static String localAddress;
    private static String logFile;

    public static void main(String[] args) throws IOException {
        DatagramSocket serverSocket = new DatagramSocket(PORT);
        HashSet<String> receivedMessages = new HashSet<>();
        HashSet<String> neighbors = new HashSet<>();
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];

        localAddress = getIP("en0");
        logFile = localAddress + ".txt";

        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            String data = new String(receivePacket.getData());
            writeToLog(TAG_RCV + data);
            System.out.println(TAG_RCV + data);

            // if the message has not been received before, then broadcast it
            if (!receivedMessages.contains(data)) {
                receivedMessages.add(data);
                // 0: src, 1: dst, 2: time, 3: msg
                String[] infos = data.split(SPLITER);
                String msg = infos[3];
                String dst = infos[1];
                InetAddress IPAddress = receivePacket.getAddress();
                neighbors.add(IPAddress.getHostName());
                print(neighbors);

                // continue broadcast if dst is not local address
                if (!localAddress.equals(dst)) {
                    int port = receivePacket.getPort();
                    System.out.println("received port:" + port);
                    sendData = data.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(BROADCAST_IP), PORT);
                    serverSocket.setBroadcast(true);
                    serverSocket.send(sendPacket);
                    System.out.println(TAG_SEND + data);
                    writeToLog(TAG_SEND + data);
                    System.out.println("Not Destination, continue to broadcast ");
                } else{
                    System.out.println("Reached destination, will not broadcast ");
                }
            } else {
                System.out.println("Message Duplicated, will not broadcast");
            }
        }
    }

    private static void writeToLog(String tag) throws IOException {
        FileOutputStream fos = new FileOutputStream(logFile, true);
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
}
