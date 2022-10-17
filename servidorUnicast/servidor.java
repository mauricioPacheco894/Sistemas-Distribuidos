import java.io.DataInputStream;
// import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
// import java.nio.ByteBuffer;

public class servidor {

    static void read(DataInputStream in, byte[] data, int position, int remaining) throws Exception {
        while(remaining > 0) {
            int n = in.read(data, position, remaining);
            position += n;
            remaining -= n;
        }
    }

    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(50000);
        Socket conection = server.accept();

        // DataOutputStream output = new DataOutputStream(conection.getOutputStream());
        DataInputStream input = new DataInputStream(conection.getInputStream());

        long t1 = System.currentTimeMillis();

        // int n = input.readInt();
        // System.out.println(n);

        // double x = input.readDouble();
        // System.out.println(x);

        // byte[] buffer = new byte[5];
        // read(input, buffer, 0, buffer.length);
        // System.out.println(new String(buffer, "UTF-8"));

        // output.write("HELLO".getBytes());
        
        // byte[] array = new byte[5*8];
        // read(input, array, 0, array.length);

        // ByteBuffer b = ByteBuffer.wrap(array);

        // for(int i = 0; i < 5; i++) 
        //     System.out.println(b.getDouble());

        for(int i = 1; i <= 10000; i++) {
            input.readDouble();
        }

        // byte[] array = new byte[10000*8];
        // read(input, array, 0, array.length);
        
        long t2 = System.currentTimeMillis();

        System.out.println(t2 - t1 + "ms");

        conection.close();
        server.close();
    }
}
