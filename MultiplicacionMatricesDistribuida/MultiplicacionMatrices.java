import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketPermission;
import java.nio.ByteBuffer;

public class MultiplicacionMatrices {
    static int N;
    static float[][] A;
    static float[][] B;
    static float[][] C;
    static float checksum;

    static void read(DataInputStream in, byte[] data, int position, int remaining) throws Exception {
        while (remaining > 0) {
            int n = in.read(data, position, remaining);
            position += n;
            remaining -= n;
        }
    }

    static class MultThread extends Thread {
        String host;
        int port;
        int begin;
        int end;

        public MultThread(String host, int port, int begin, int end) {
            this.host = host;
            this.port = port;
            this.begin = begin;
            this.end = end;
        }

        @Override
        public void run() {
            try {
                Socket connection = null;
                for (;;) {
                    try {
                        connection = new Socket(host, port);
                        break;
                    } catch (SocketException e) {
                        Thread.sleep(1000);
                    }
                }
                DataInputStream input = new DataInputStream(connection.getInputStream());
                DataOutputStream output = new DataOutputStream(connection.getOutputStream());

                ByteBuffer floatBuffer = ByteBuffer.allocate(N * N / 2 * 4);

                for (int i = begin; i < end; i++) {
                    for (int j = 0; j < N; j++) {
                        floatBuffer.putFloat(A[i][j]);
                    }
                }

                output.writeInt(N);
                output.write(floatBuffer.array());
                floatBuffer.clear();
                floatBuffer = ByteBuffer.allocate(N * N * 4);

                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        floatBuffer.putFloat(B[i][j]);
                    }
                }

                output.write(floatBuffer.array());

                byte[] data = new byte[N * N / 2 * 4];
                read(input, data, 0, data.length);
                floatBuffer = ByteBuffer.wrap(data);

                for (int i = begin; i < end; i++) {
                    for (int j = 0; j < N; j++) {
                        C[i][j] = floatBuffer.getFloat();
                    }
                }

                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        switch (args[0]) {
            case "0":
                try {
                    N = Integer.parseInt(args[5]);
                    A = new float[N][N];
                    B = new float[N][N];
                    C = new float[N][N];

                    for (int i = 0; i < N; i++) {
                        for (int j = 0; j < N; j++) {
                            A[i][j] = i + 3 * j;
                            B[i][j] = 2 * i - j;
                            C[i][j] = 0;
                        }
                    }

                    // Trastransponer la matriz B
                    for (int i = 0; i < N; i++) {
                        for (int j = i; j < N; j++) {
                            float temp = B[i][j];
                            B[i][j] = B[j][i];
                            B[j][i] = temp;
                        }
                    }

                    for (int i = 0; i < N && N == 12; i++) {
                        for (int j = 0; j < N; j++) {
                            System.out.print(A[i][j] + " ");
                        }
                        System.out.println();
                    }
                    System.out.println();

                    for (int i = 0; i < N && N == 12; i++) {
                        for (int j = 0; j < N; j++) {
                            System.out.print(B[i][j] + " ");
                        }
                        System.out.println();
                    }
                    System.out.println();

                    MultThread m1 = new MultThread(args[1], Integer.parseInt(args[2]), 0, N / 2);
                    MultThread m2 = new MultThread(args[3], Integer.parseInt(args[4]), N / 2, N);
                    m1.start();
                    m2.start();
                    m1.join();
                    m2.join();

                    for (int i = 0; i < N && N == 12; i++) {
                        for (int j = 0; j < N; j++) {
                            System.out.print(C[i][j] + " ");
                        }
                        System.out.println();
                    }
                    System.out.println();

                    for (int i = 0; i < N; i++) {
                        for (int j = 0; j < N; j++) {
                            checksum += C[i][j];
                        }
                    }
                    System.out.println("checksum: " + checksum);
                } catch (Exception e) {
                    System.out.println("Uso: java MultiplicacionMatrices 0 <host1> <puerto1> <host2> <puerto2> <N>");
                }
                break;
            case "1":
            case "2":
                try {
                    ServerSocket server = new ServerSocket(Integer.parseInt(args[1]));
                    Socket serverConection = server.accept();
                    DataOutputStream output1 = new DataOutputStream(serverConection.getOutputStream());
                    DataInputStream input1 = new DataInputStream(serverConection.getInputStream());

                    int N1 = input1.readInt();
                    float[][] A1 = new float[N1 / 2][N1];
                    float[][] B1 = new float[N1][N1];
                    float[][] C1 = new float[N1 / 2][N1];

                    byte[] data1 = new byte[(N1 * N1 / 2) * 4];
                    read(input1, data1, 0, (N1 * N1 / 2) * 4);
                    ByteBuffer floatBuffer1 = ByteBuffer.wrap(data1);
                    for (int i = 0; i < N1 / 2; i++) {
                        for (int j = 0; j < N1; j++) {
                            A1[i][j] = floatBuffer1.getFloat();
                        }
                    }

                    data1 = new byte[(N1 * N1) * 4];
                    read(input1, data1, 0, (N1 * N1) * 4);
                    floatBuffer1 = ByteBuffer.wrap(data1);
                    for (int i = 0; i < N1; i++) {
                        for (int j = 0; j < N1; j++) {
                            B1[i][j] = floatBuffer1.getFloat();
                        }
                    }

                    for (int i = 0; i < N1 / 2; i++) {
                        for (int j = 0; j < N1; j++) {
                            for (int k = 0; k < N1; k++) {
                                C1[i][j] += A1[i][k] * B1[j][k];
                            }
                        }
                    }

                    floatBuffer1 = ByteBuffer.allocate((N1 * N1 / 2) * 4);
                    for (int i = 0; i < N1 / 2; i++) {
                        for (int j = 0; j < N1; j++) {
                            floatBuffer1.putFloat(C1[i][j]);
                        }
                    }

                    output1.write(floatBuffer1.array());
                    serverConection.close();
                    server.close();
                } catch (Exception e) {
                    System.out.println("Uso: java MultiplicacionMatrices <nodo> <puerto>");
                }
                break;
            default:
                System.out.println("Opcion no valida");
                break;
        }
    }
}