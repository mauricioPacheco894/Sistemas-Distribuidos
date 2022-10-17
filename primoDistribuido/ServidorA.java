import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorA {

    static class Worker extends Thread {
        private Socket conection;
        long n, left, right;
        String result = "NO DIVIDE";

        public Worker(Socket conection) {
            this.conection = conection;
        }

        public void run() {
            try {
                DataOutputStream output = new DataOutputStream(conection.getOutputStream());
                DataInputStream input = new DataInputStream(conection.getInputStream());

                n = input.readLong();
                left = input.readLong();
                right = input.readLong();

                if (left > 1 )
                    for (long i = left; i <= right; i++)
                        if (n % i == 0) {
                            result = "DIVIDE";
                            break;
                        } else {
                            result = "NO DIVIDE";
                        }

                System.out.println("n: " + n + " left: " + left + " right: " + right + " result: " + result);
                output.writeUTF(result);
                System.out.println("Finished process");
                Thread.sleep(1000);
                conection.close();

            } catch (Exception e) {
                System.out.println("I/O error: " + e.getMessage());
            } finally {
                try {
                    conection.close();
                } catch (Exception e) {
                    System.out.println("Close conection failed: " + e.getMessage());
                }
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(Integer.parseInt(args[0]));
        System.out.println("Server started, waiting for connections...");
        try {
            for (;;) {
                Socket conection = server.accept();
                System.out.println("Connection received");
                Worker w = new Worker(conection);
                w.start();
            }
        } catch (Exception e) {
            System.out.println("Connection error: " + e.getMessage());

        } finally {
            server.close();
        }
    }  
}
