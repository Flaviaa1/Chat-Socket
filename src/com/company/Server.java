package com.company;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
    private final ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {

        this.serverSocket = serverSocket;
    }
    public void startServer() {
        try {
            System.out.println("Corriendo..");
            //  Escuche las conexiones (clientes para conectar) en el puerto 5000.
            while (!serverSocket.isClosed()) {
                // Se cerrará en el  Client Handler.
                Socket socket = serverSocket.accept();
                System.out.println("UN NUEVO CLIENTE SE HA CONECTADO!");
                ClientHandler clientHandler = new ClientHandler(socket);
                Thread thread = new Thread(clientHandler);

                // El método de inicio inicia la ejecución de un hilo.
                // Cuando llamas a start(), se llama al método de ejecución.
                // El sistema operativo programa los hilos.
                thread.start();
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    // Close the server
    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Run the program.
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5000);
        Server server = new Server(serverSocket);
        server.startServer();
    }

}