import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import javax.net.ssl.SSLSocketFactory;

public class Client {

    static class FileThread extends Thread {
        String fileName;

        public FileThread(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public void run() {
            SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            Socket connection = null;
            try {
                for (;;) {
                    try {
                        connection = socketFactory.createSocket("localhost", 9999);
                        break;
                    } catch (Exception e) {
                        Thread.sleep(100);
                    }
                }

                DataInputStream input = new DataInputStream(connection.getInputStream());
                DataOutputStream output = new DataOutputStream(connection.getOutputStream());
                BufferedOutputStream bufferedOutput = new BufferedOutputStream(connection.getOutputStream());

                int fileSize = (int) new File(fileName).length();
                System.out.println("Enviando archivo " + fileName + " de " + fileSize + " bytes");
                output.writeUTF(fileName);
                output.writeInt(fileSize);

                BufferedInputStream bufferedInput = new BufferedInputStream(new FileInputStream(fileName));
                byte[] buffer = new byte[1024];
                int bytesRead = 0;
                while ((bytesRead = bufferedInput.read(buffer)) != -1) {
                    bufferedOutput.write(buffer, 0, bytesRead);
                }
                bufferedOutput.flush();
                bufferedInput.close();
                System.out.println("Archivo " + fileName + " enviado correctamente");

                String response = input.readUTF();
                if (response.equals("OK"))
                    System.out.println("Archivo " + fileName + " recibido correctamente por el servidor");
                else
                    System.out.println(response);

                bufferedOutput.close();
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

    public static void main(String[] args) {
        System.setProperty("javax.net.ssl.trustStore", "clientKeystore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "12345678");

        if (args.length == 0) {
            System.out.println("Uso: java Client <archivo1> <archivo2> ...");
            return;
        }

        FileThread[] threads = new FileThread[args.length];
        for (int i = 0; i < args.length; i++) {
            if (new File(args[i]).exists()) {
                threads[i] = new FileThread(args[i]);
                threads[i].start();
            } else {
                System.out.println("El archivo " + args[i] + " no existe");
            }
        }
    }
}