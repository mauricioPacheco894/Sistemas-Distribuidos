import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ServidorHTTP {

    static int ports[] = { 0, 0, 0, 0};
    static boolean isPrime;

    static class Client extends Thread {
        Socket conection = null;
        int port;
        long n, left, right;
        String result;

        public Client(int port, long n, long left, long right) {
            this.port = port;
            this.n = n;
            this.left = left;
            this.right = right;
        }

        public void run() {
            try {
                for(;;) {
                    try {
                        conection = new Socket("localhost", port);
                        break;
                    } catch (SocketException e) {
                        Thread.sleep(100);
                    }
                }

                DataOutputStream output = new DataOutputStream(conection.getOutputStream());
                DataInputStream input = new DataInputStream(conection.getInputStream());

                output.writeLong(n);
                output.writeLong(left);
                output.writeLong(right);
                result = input.readUTF();

                synchronized (this) {
                    if (result.equals("DIVIDE"))
                        isPrime = false;
                }
                Thread.sleep(1000);
                conection.close();

            } catch (Exception e) {
                System.out.println("Class Client error: " + e.getMessage());
            } finally {
                try {
                    conection.close();
                } catch (Exception e) {
                    System.out.println("Close conection failed: " + e.getMessage());
                }
            }
        }
    }

    static class Worker extends Thread {
        Socket conection;

        Worker(Socket conection) {
            this.conection = conection;
        }

        public void run() {
            try {
                PrintWriter output = new PrintWriter(conection.getOutputStream());
                BufferedReader input = new BufferedReader(new InputStreamReader(conection.getInputStream()));

                String petition = input.readLine();
                System.out.println(petition);

                for (;;) {
                    String line = input.readLine();
                    if (line == null || line.length() == 0) break;
                    System.out.println(line);
                }
                
                if (petition.startsWith("GET /primo?numero=") && petition.substring(18, petition.length() - 9).matches("[0-9]+")) {
                    long number = Long.parseLong(petition.substring(18 ,petition.length() - 9));

                    isPrime = true;
                    if (number < 2) isPrime = false;
                    else {
                    
                        Client c1 = new Client(ports[0], number, 2, number / 8);
                        Client c2 = new Client(ports[1], number, number / 8 + 1, number / 4);
                        Client c3 = new Client(ports[2], number, number / 4 + 1, 3 * number / 8);
                        Client c4 = new Client(ports[3], number, 3 * number / 8 + 1, number / 2);

                        c1.start();
                        c2.start();
                        c3.start();
                        c4.start();

                        c1.join();
                        c2.join();
                        c3.join();
                        c4.join();
                    }

                    System.out.println("--------------------------------------------------");
                    System.out.println("The number " + number + " is prime: " + isPrime);
                    System.out.println("--------------------------------------------------");
                    String answer = "<html><h1> El numero " + number + (isPrime ? " ES PRIMO" : " NO ES PRIMO") + "</h1></html>";

                    output.println("HTTP/1.1 200 OK");
                    output.println("Content-Type: text/html; charset=UTF-8");
                    output.println("Content-Length: " + answer.length());
                    output.println("");
                    output.flush();
                    output.println(answer);
                    output.flush();

                    conection.close();
                } else {
                    output.println("HTTP/1.1 400 Bad Request");
                    output.println("Content-Type: text/html; charset=UTF-8");
                    output.println("");
                    output.println("<html><body>");
                    output.println("<h1> 400 Bad Request </h1>");
                    output.println("</body></html>");
                    output.flush();

                    conection.close();
                }

            } catch (Exception e) {
                System.out.println("Class Worker error: " + e.getMessage());
            } finally {
                try {
                    conection.close();
                } catch (Exception e) {
                    System.out.println("Close connection error: " + e);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(8080);
        System.out.println("Server started, waiting...");
        try {
            for (;;) {
                Socket conection = server.accept();
                System.out.println("Connection received");
                Worker w = new Worker(conection);
                w.start();
                ports[0] = Integer.parseInt(args[0]);
                ports[1] = Integer.parseInt(args[1]);
                ports[2] = Integer.parseInt(args[2]);
                ports[3] = Integer.parseInt(args[3]);
            }
        } catch (NumberFormatException e) {
            System.out.println("NumberFormatException: " + e);
        } catch (IOException e) {
            System.out.println("Main Exception: " + e);
        } finally {
            server.close();
        }
    }
}