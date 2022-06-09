package com.company;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
/**
 * Cuando un cliente se conecta, el servidor genera un hilo para manejar el cliente.
 * De esta manera el servidor puede manejar múltiples clientes al mismo tiempo.
 *
 * Esta palabra clave debe usarse en setters, pasando el objeto como un argumento,
 * y para llamar constructores alternativos (un constructor con un conjunto diferente de
 * argumentos.
 */

public class ClientHandler implements Runnable {


    //Lista de matrices de todos los hilos que manejan clientes para que cada mensaje se pueda enviar al cliente que maneja el hilo.
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    // Id que se incrementará con cada nuevo cliente

    //Socket para una conexión, lector de búfer y escritor para recibir y enviar datos respectivamente
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter= new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            //Cuando un cliente se conecta se envía su nombre de usuario.
            this.clientUsername = bufferedReader.readLine();

            //agregua el nuevo controlador de cliente a la matriz para que puedan recibir mensajes de otros usuarios.
            clientHandlers.add(this);
            broadcastMessage("SERVER: " + clientUsername + "ha entrado en el chat!");
        } catch (IOException e) {
            // Close everything more gracefully.
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }
    // Todo en este método se ejecuta en un thread separado. Queremos escuchar los mensajes
    // en un thread independiente porque la escucha (bufferedReader.readLine()) es una operación de bloqueo
    // Una operación de bloqueo significa que la persona que llama espera a que la persona que llama termine su operación.
    @Override
    public void run() {
        String messageFromClient;
        // Continúe escuchando los mensajes mientras aún se establece una conexión con el cliente.
        while (socket.isConnected()) {
            try {

                //Lea lo que el cliente envió y luego envíelo a todos los demás clientes.
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                // Close everything gracefully.
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }
    // Envíe un mensaje a través de cada thread de controlador de cliente para que todos reciban el mensaje.
    // Básicamente, cada controlador de cliente es una conexión a un cliente. Así que para cualquier mensaje que
    // se recibe, recorre cada conexión y envíala hacia abajo.
    public void broadcastMessage(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                //No desea transmitir el mensaje al usuario que lo envió.
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                // Gracefully close everything.
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }
    //Si el cliente se desconecta por cualquier motivo, elimínelos de la lista para que no se envíe un mensaje por una conexión rota.

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " ha dejado el chat!");
    }

    // Método auxiliar para cerrar todo para que no tengas que repetirte.
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
