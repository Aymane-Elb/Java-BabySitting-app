package server;

import java.io.*;
import java.net.*;
import java.util.Set;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private Set<ClientHandler> clients;

    public ClientHandler(Socket socket, Set<ClientHandler> clients) throws IOException {
        this.socket = socket;
        this.clients = clients;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received: " + message); // Optionally print the message for debugging
                synchronized (clients) { // Synchronize block to ensure thread-safe access
                    for (ClientHandler client : clients) {
                        if (client != this) {
                            client.out.write(message + "\n");
                            client.out.flush();
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected");
        } finally {
            try {
                clients.remove(this);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
