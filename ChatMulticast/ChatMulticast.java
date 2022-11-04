import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

public class ChatMulticast {
    static void sendMulticastMessage(byte[] buffer, String ip, int port) throws Exception {
        DatagramSocket socket = new DatagramSocket();
        socket.send(new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip), port));
        socket.close();
    }

    static byte[] receiveMulticastMessage(MulticastSocket socket, int messageLength) throws Exception {
        byte[] buffer = new byte[messageLength];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return packet.getData();
    }

    static class Worker extends Thread {
        @Override
        public void run() {
            try {
                MulticastSocket socket = new MulticastSocket(10000);
                InetSocketAddress group = new InetSocketAddress(InetAddress.getByName("239.10.10.10"), 10000);
                NetworkInterface netInter = NetworkInterface.getByName("em1");
                socket.joinGroup(group, netInter);
                while (true) {
                    byte[] buffer = receiveMulticastMessage(socket, 256);
                    System.out.println(new String(buffer).trim());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");
        String name = args[0];
        new Worker().start();
        while (true) {
            System.out.print("Mensaje: ");
            String message = name + ":- " + System.console().readLine();
            sendMulticastMessage(message.getBytes("UTF-8"), "239.10.10.10", 10000);
        }
    }
}