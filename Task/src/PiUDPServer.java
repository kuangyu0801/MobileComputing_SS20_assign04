


import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.Enumeration;

/**
 * This class is identical to UDPServer except for NetworkInterface Name eth0
 * */

public class PiUDPServer {
    final static int PORT = 5008;
    final static String TAG_RCV = "[RCV]";
    final static String TAG_SEND = "[SEND]";
    private static String localAddress;
    private static String logFile;

    public static void main(String[] args) throws IOException {
        DatagramSocket serverSocket = new DatagramSocket(PORT);
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];

        InetAddress localhost = InetAddress.getLocalHost();
        localAddress = localhost.getHostAddress();
        logFile = localAddress + ".txt";
        System.out.println("Local Host Address: " + localAddress);
        System.out.println("Local Canonical Host Name: " + localhost.getCanonicalHostName());
        System.out.println("Local Host Name: " + localhost.getHostName());
        // TODO: for linux en0 should be replaced with eth0 (ethernet) wlan0 (wireless)
        NetworkInterface en0 = NetworkInterface.getByName("en0");
        displayInterfaceInformation(en0);

        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            // DONE: add logging method
            writeToLog(receivePacket);

            String sentence = new String(receivePacket.getData());
            System.out.println(TAG_RCV + sentence);
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();

            String capitalizedSentence = sentence.toUpperCase();
            sendData = capitalizedSentence.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            serverSocket.send(sendPacket);
            System.out.println(TAG_SEND + capitalizedSentence);
        }
    }

    private static void writeToLog(DatagramPacket datagramPacket) throws IOException {
        FileOutputStream fos = new FileOutputStream(logFile, true);
        fos.write("TAG_RCV".getBytes());
        InetAddress IPAddress = datagramPacket.getAddress();
        fos.write(IPAddress.toString().getBytes());
        fos.write("\r".getBytes());
        fos.close();
    }

    static void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
        System.out.println("Display name:" + netint.getDisplayName());
        System.out.println("Name: " + netint.getName());
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            System.out.println("InetAddress: " + inetAddress.getHostAddress());
        }
        System.out.println();
    }
}
