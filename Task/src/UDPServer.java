import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPServer {
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
        System.out.println("System IP Address : " + localAddress);

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
}
