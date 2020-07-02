import java.io.IOException;
import java.net.*;
import java.time.LocalTime;
import java.util.Enumeration;

public class UDPClient {
    final static int PORT = 5008;
    final static String SPLITER = "_";
    final static String TAG_RCV = "[RCV]";
    final static String TAG_SEND = "[SEND]";
    final static String BROADCAST_IP = "192.168.210.255";
    final static String[] ALL_ADDRS = {
            "192.168.210.174", "192.168.210.180", "192.168.210.185",
            "192.168.210.196", "192.168.210.197"};

    public static void main(String[] args) throws IOException {

        DatagramSocket clientSocket = new DatagramSocket(PORT);
        // enable broadcast
        clientSocket.setBroadcast(true);
        System.out.println(args[0]);
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];

        String msg = "Hello, World!";
        String src = getIP("en0");
        String dst = ALL_ADDRS[Integer.parseInt(args[0])]; // use the input argument as destination
        System.out.println("dst:" + dst);
        String time = LocalTime.now().toString();
        String data =  src + SPLITER + dst + SPLITER + time + SPLITER + msg;
        sendData = data.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(BROADCAST_IP), PORT);
        clientSocket.send(sendPacket);

        System.out.println(TAG_SEND + data);
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);

        String modifiedSentence = new String(receivePacket.getData());
        System.out.println(TAG_RCV + modifiedSentence);
        clientSocket.close();
    }

    public static String getIP(String name) throws SocketException {
        NetworkInterface en0 = NetworkInterface.getByName(name);
        Enumeration<InetAddress> inetAddresses = en0.getInetAddresses();
        inetAddresses.nextElement();
        InetAddress inetAddress = inetAddresses.nextElement();
        return inetAddress.getHostAddress();
    }

}
