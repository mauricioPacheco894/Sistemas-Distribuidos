import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.SocketException;
//import java.nio.ByteBuffer;

public class cliente {

    static void read(DataInputStream in, byte[] data, int position, int remaining) throws Exception {
        while(remaining > 0) {
            int n = in.read(data, position, remaining);
            position += n;
            remaining -= n;
        }
    }

    public static void main(String[] args) throws Exception {
        Socket conection = null;

        for(;;) {
            try {
                conection = new Socket("localhost", 50000);
                break;
            } catch (SocketException e) {
                Thread.sleep(100);
            }
        }

        DataOutputStream output = new DataOutputStream(conection.getOutputStream());
        //DataInputStream input = new DataInputStream(conection.getInputStream());

        long t1 = System.currentTimeMillis();

        // output.writeInt(123);
        // output.writeDouble(1234567890.1234567890);
        // output.write("hello".getBytes());

        // byte[] buffer = new byte[5];
        // read(input, buffer, 0, buffer.length);
        // System.out.println(new String(buffer, "UTF-8"));

        // ByteBuffer doublesBuffer = ByteBuffer.allocate(5*8);

        // doublesBuffer.putDouble(1.1);
        // doublesBuffer.putDouble(1.2);
        // doublesBuffer.putDouble(1.3);
        // doublesBuffer.putDouble(1.4);
        // doublesBuffer.putDouble(1.5);

        // byte array[] = doublesBuffer.array();
        // output.write(array);
        
        for(int i = 1; i <= 10000; i++) {
            output.writeDouble((double) i);
        }

        // ByteBuffer doublesBuffer = ByteBuffer.allocate(10000*8);

        // for(int i = 1; i <= 10000; i++) {
        //     doublesBuffer.putDouble((double) i);
        // }
        // byte array[] = doublesBuffer.array();
        // output.write(array);

        long t2 = System.currentTimeMillis();

        System.out.println(t2 - t1 + "ms");
        
        Thread.sleep(1000);
        conection.close();
    }
}