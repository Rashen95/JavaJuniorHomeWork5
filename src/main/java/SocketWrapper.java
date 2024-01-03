import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

@Getter
@Setter
public class SocketWrapper implements AutoCloseable {

    private final long id;
    private final Socket socket;
    private boolean isAdmin;
    private final Scanner input;
    private final PrintWriter output;

    SocketWrapper(long id, Socket socket, boolean isAdmin) throws IOException {
        this.id = id;
        this.socket = socket;
        this.isAdmin = isAdmin;
        this.input = new Scanner(socket.getInputStream());
        this.output = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void close() throws Exception {
        socket.close();
    }

    @Override
    public String toString() {
        return String.format("%s", socket.getInetAddress().toString());
    }
}