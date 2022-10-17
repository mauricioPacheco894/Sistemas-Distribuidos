import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.net.ssl.SSLServerSocketFactory;

public class Server {
    
    static class ServerThread extends Thread {
        Socket connection;

        public ServerThread(Socket connection) {
            this.connection = connection;
        }

        @Override
        public void run() {
            try {
                DataInputStream input = new DataInputStream(connection.getInputStream());
                DataOutputStream output = new DataOutputStream(connection.getOutputStream());
                BufferedInputStream bufferedInput = new BufferedInputStream(connection.getInputStream());
                
                String fileName = input.readUTF();
                int fileSize = input.readInt();
                System.out.println("Recibiendo archivo " + fileName + " de " + fileSize + " bytes");

                BufferedOutputStream bufferedOutput = new BufferedOutputStream(new FileOutputStream(fileName));
                byte[] buffer = new byte[1024];
                int bytesRead = 0;
                int totalBytesRead = 0;
                while ((bytesRead = bufferedInput.read(buffer)) != -1) {
                    bufferedOutput.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    if (totalBytesRead == fileSize)
                        break;
                }
                bufferedOutput.close();

                if (totalBytesRead == fileSize) {
                    output.writeUTF("OK");
                    System.out.println("Archivo " + fileName + " recibido correctamente");
                } else {
                    output.writeUTF("ERROR: El servidor no pudo guardar el archivo" + fileName);
                    System.out.println("Error al recibir el archivo " + fileName);
                }
                Thread.sleep(1000);
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("javax.net.ssl.keyStore","serverKeystore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword","12345678");

        SSLServerSocketFactory socketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        ServerSocket server = socketFactory.createServerSocket(9999);
        try {
            for (;;) {
                Socket connection = server.accept();
                ServerThread thread = new ServerThread(connection);
                thread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.close();
        }
    }
}