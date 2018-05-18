import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private static Map<Socket, String> seng = new Hashtable<Socket, String>();
    private static Map<Socket, String> sst = new Hashtable<Socket, String>();
    private static Map<Socket, String> shss = new Hashtable<Socket, String>();
    private static Set<String> username = new HashSet<>();


    public static class RunnableServer implements Runnable {
        protected Socket clientSocket = null;

        public RunnableServer(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            try {
                String clientName = null;
                String group = null;
                Map<Socket, String> temp = null;
                while (true) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    String clientMessage;
                    clientMessage = in.readLine();
                    if (clientMessage.equals("server exit")) {
                        if(group != null) {
                            if(group.equals("seng")) {
                                seng.remove(clientSocket);
                            } else if(group.equals("sst")) {
                                sst.remove((clientSocket));
                            } else if(group.equals("shss")) {
                                shss.remove(clientSocket);
                            }
                        }
                        if(clientName != null) {
                            username.remove(clientName);
                        }
                        clientSocket.close();
                        break;
                    }
                    System.out.println(clientMessage);
                    if (clientMessage.startsWith("server hello")) {
                        if (clientName == null) {
                            String[] parts = clientMessage.split(" ");
                            if (parts.length < 3) {
                                out.println("ERROR: You didn't write your name");
                            }
                            if (username.contains(parts[2])) {
                                out.println("ERROR: Already exists user with this name");
                            } else {
                                clientName = parts[2];
                                username.add(clientName);
                                out.println("SUCCESS: Hi " + clientName);
                            }
                        } else {
                            out.println("ERROR: You've already entered your name");
                        }
                    } else {
                        if (clientName == null) {
                            out.println("ERROR: You must enter your name");
                        } else if (clientMessage.startsWith("server grouplist")) {
                            out.print("SUCCESS: seng: ");
                            out.print(seng.values());
                            out.print(" sst:");
                            out.print(sst.values());
                            out.print(" shss:");
                            out.print(shss.values());
                            out.println();
                        } else if (clientMessage.startsWith("server join")) {
                            boolean correct = true;
                            if (group != null) {
                                out.println("ERROR: You are already in a group");
                            } else {
                                String parts[] = clientMessage.split(" ");
                                if (parts.length < 3) {
                                    out.print("ERROR: You didn't write group");
                                } else if (parts[2].equals("seng")) {
                                    seng.put(clientSocket, clientName);
                                } else if (parts[2].equals("sst")) {
                                    sst.put(clientSocket, clientName);
                                } else if (parts[2].equals("shss")) {
                                    shss.put(clientSocket, clientName);
                                } else {
                                    out.println("ERROR: This group does not exist");
                                    correct = false;
                                }

                                if (correct) {
                                    group = parts[2];
                                    out.println("SUCCESS: ");
                                }
                            }
                        } else if (clientMessage.startsWith("server leave")) {
                            String[] parts = clientMessage.split(" ");
                            if (group != null) {
                                if (group.equals("seng") && parts[2].equals("seng")) {
                                    seng.remove(clientSocket);
                                    group = null;
                                    out.println("SUCCESS: ");
                                } else if (group.equals("sst") && parts[2].equals("sst")) {
                                    sst.remove(clientSocket);
                                    group = null;
                                    out.println("SUCCESS: ");
                                } else if (group.equals("shss") && parts[2].equals("shss")) {
                                    shss.remove(clientSocket);
                                    group = null;
                                    out.println("SUCCESS: ");
                                } else {
                                    out.println("ERROR: You are trying to leave group that you are not in");
                                }
                            } else {
                                out.println("ERROR: You are not in any group currently");
                            }
                        } else if (clientMessage.startsWith("server members")) {
                            if (group == null) {
                                out.println("ERROR:You are not in any group now");
                            } else {
                                if (group.equals("seng")) {
                                    temp = new HashMap<>(seng);
                                    System.out.println(group);
                                    out.println(seng.values());
                                }
                                if (group.equals("sst")) {
                                    temp = new HashMap<>(sst);
                                    System.out.println(group);
                                    out.println(sst.values());
                                }
                                if (group.equals("shss")) {
                                    temp = new HashMap<>(shss);
                                    System.out.println(group);
                                    out.println(shss.values());
                                }
                            }
                        } else if (clientMessage.startsWith("toall")) {
                            if(group == null) {
                                out.println("ERROR: You are not in any group");
                            } else {
                                String[] parts = clientMessage.split(" ", 2);
                                if(group.equals("seng")) {
                                    for(Socket s: seng.keySet()) {
                                        if(s.equals(clientSocket)) {

                                        } else {
                                            PrintWriter allout = new PrintWriter(s.getOutputStream(), true);
                                            allout.println(clientName + ": " + parts[1]);
                                        }
                                    }
                                    out.println("SUCCESS: ");
                                } else if(group.equals("sst")) {
                                    for(Socket s: sst.keySet()) {
                                        if(s.equals(clientSocket)) {

                                        } else {
                                            PrintWriter allout = new PrintWriter(s.getOutputStream(), true);
                                            allout.println(clientName + ": " + parts[1]);
                                        }
                                    }
                                    out.println("SUCCESS: ");
                                } else if(group.equals("shss")) {
                                    for(Socket s: shss.keySet()) {
                                        if(s.equals(clientSocket)) {

                                        } else {
                                            PrintWriter allout = new PrintWriter(s.getOutputStream(), true);
                                            allout.println(clientName + ": " + parts[1]);
                                        }
                                    }
                                    out.println("SUCCESS: ");
                                }
                            }
                        } else if(group != null) {
                            String[] parts = clientMessage.split(" ", 2);
                            boolean found = false;
                            boolean lastExist = false;

                            if(temp != null) {
                                for (Socket s : temp.keySet()) {
                                    if (parts[0].equals(temp.get(s).toString())) {
                                        lastExist = true;
                                        break;
                                    }
                                }
                            }

                            if(group.equals("seng")) {
                                for(Socket s: seng.keySet()) {
                                    if(parts[0].equals(seng.get(s).toString())) {
                                        found = true;
                                        PrintWriter oneout = new PrintWriter(s.getOutputStream(), true);
                                        oneout.println(clientName + ": " + parts[1]);
                                    }
                                }
                            } else if(group.equals("sst")) {
                                for(Socket s: sst.keySet()) {
                                    if(parts[0].equals(sst.get(s).toString())) {
                                        found = true;
                                        PrintWriter oneout = new PrintWriter(s.getOutputStream(), true);
                                        oneout.println(clientName + ": " + parts[1]);
                                    }
                                }
                            } else if(group.equals("shss")) {
                                for(Socket s: shss.keySet()) {
                                    if(parts[0].equals(shss.get(s).toString())) {
                                        found = true;
                                        PrintWriter oneout = new PrintWriter(s.getOutputStream(), true);
                                        oneout.println(clientName + ": " + parts[1]);
                                    }
                                }
                            }

                            if(found) {
                                out.println("SUCCESS: ");
                            } else if(!found && lastExist) {
                                out.println("ERROR: Client left until reaching your message");
                            }else {
                                out.println("ERROR: Incorrect command");
                            }
                        } else {
                            out.println("ERROR: Incorrect command");
                        }
                    }
                    out.println();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);

        while(true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(new RunnableServer(clientSocket)).start();
        }
    }
}