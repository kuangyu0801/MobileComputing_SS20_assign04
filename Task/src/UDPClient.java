import java.io.IOException;
import java.net.*;

public class UDPClient {
    final static int PORT = 5008;
    final static String TAG_RCV = "[RCV]";
    final static String TAG_SEND = "[SEND]";

    public static void main(String[] args) throws IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName("localhost");

        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];

        String sentence = "Hello, World!";
        sendData = sentence.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, PORT);
        clientSocket.send(sendPacket);
        System.out.println(TAG_SEND + sentence);
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);

        String modifiedSentence = new String(receivePacket.getData());
        System.out.println(TAG_RCV + modifiedSentence);
        clientSocket.close();
    }
}
