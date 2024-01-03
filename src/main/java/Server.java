import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class Server {
    public static final int PORT = 1515;
    private static final String ADMIN_PASSWORD = "555";

    private static long clientIdCounter = 1L;
    private static final Map<Long, SocketWrapper> clients = new HashMap<>();

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Server started at Port " + PORT);
            while (true) {
                final Socket client = server.accept();
                final long clientId = clientIdCounter++;
                SocketWrapper wrapper = new SocketWrapper(clientId, client, false);
                clients.put(clientId, wrapper);
                startClientThread(wrapper, clientId);
            }
        }
    }

    private static void startClientThread(SocketWrapper wrapper, long clientId) {
        new Thread(() -> {
            try (Scanner input = wrapper.getInput(); PrintWriter output = wrapper.getOutput()) {
                output.println("Enter administrator password");
                String inputPassword = input.nextLine();
                if (Objects.equals(inputPassword, Server.ADMIN_PASSWORD)) {
                    wrapper.setAdmin(true);
                    sendMessage("Administrator[" + wrapper + "] connected to server");
                    output.println("Connection successful. Contacts list: " + clients);
                    while (true) {
                        String clientInput = input.nextLine();
                        if (checkForExit(clientInput, clientId)) break;
                        if (clientInput.matches("(?i)Kick \\d+")) {
                            String[] splitInput = clientInput.split(" ");
                            try {
                                long targetId = Long.parseLong(splitInput[1]);
                                SocketWrapper target = clients.get(targetId);
                                target.close();
                                sendMessage("Client [" + target + "] has been kicked by administrator");
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            sendMessage(clientInput);
                        }
                    }
                } else {
                    output.println("Connection successful. Contacts list: " + clients);
                    while (true) {
                        String clientInput = input.nextLine();
                        if (checkForExit(clientInput, clientId)) break;
                        sendMessage(clientInput);
                    }
                }
            }
        }).start();
    }

    private static boolean checkForExit(String input, long id) {
        if (Objects.equals("q", input)) {
            clients.remove(input);
            clients.values().forEach(it -> it.getOutput().println("Client[" + id + "] disconnected"));
            return true;
        }
        return false;
    }

    private static boolean checkForPrivateMessage(String input) {
        String[] splitInput = input.split(" ");
        return splitInput[0].matches("@\\d+");
    }

    private static void sendMessage(String clientInput) {
        if (checkForPrivateMessage(clientInput)) {
            String[] splitInput = clientInput.split(" ");
            long destinationId = Long.parseLong(splitInput[0].replace("@", ""));
            Server.clients.get(destinationId).getOutput().println(clientInput);
        } else {
            Server.clients.values().forEach(it -> it.getOutput().println(clientInput));
        }
    }
}