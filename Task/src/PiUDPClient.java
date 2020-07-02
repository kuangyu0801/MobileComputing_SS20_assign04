import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * This class is identical to UDPClient except for NetworkInterface Name eth0
 * */

public class PiUDPClient {
    final static int PORT = 5008;
    final static String TAG_RCV = "[RCV]";
    final static String TAG_SEND = "[SEND]";
    final static String BROADCAST_IP = "192.168.210.255";

    public static void main(String[] args) throws IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        clientSocket.setBroadcast(true);
/*        InetAddress IPAddress = InetAddress.getByName("localhost");
        System.out.println(IPAddress);*/
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];

        String sentence = "Hello, World!";
        sendData = sentence.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(BROADCAST_IP), PORT);
        clientSocket.send(sendPacket);
        System.out.println(TAG_SEND + sentence);
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);

        String modifiedSentence = new String(receivePacket.getData());
        System.out.println(TAG_RCV + modifiedSentence);
        clientSocket.close();
    }
}
