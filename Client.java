import java.io.*;
import java.net.Socket;

import static java.lang.System.exit;

public class Client {
    public static class GetMessage implements Runnable {
        protected DataOutputStream out;

        public GetMessage(DataOutputStream out) {
            this.out = out;
        }

        public void run() {
            try {
                while(true) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                    String clienMessage = in.readLine();
                    out.writeBytes(clienMessage + '\n');
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        try {
            Socket clientSocket = new Socket("localhost", 12345);
            new Thread(new GetMessage(new DataOutputStream(clientSocket.getOutputStream()))).start();
            while(true) {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String serverMessage = in.readLine();
                if(serverMessage == null) {
                    System.out.println("Disconnected");
                    exit(-1);
                }
                System.out.println(serverMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
