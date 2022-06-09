package com.company;
import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;
/*Un cliente envía mensajes al servidor, el servidor genera un hilo para comunicarse con el cliente.
Cada comunicación con un cliente se agrega a una lista de matriz, por lo que cualquier mensaje enviado se envía a todos los demás clientes mediante un bucle.*/


public class Client {
    //Un cliente tiene un socket para conectarse al servidor y un lector y escritor para recibir y enviar mensajes respectivamente.
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.username = username;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter= new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }
    //Enviar un mensaje no es un bloqueo y se puede hacer sin generar un hilo, a diferencia de esperar un mensaje.
    public void sendMessage() {
        try {
            //Enviar inicialmente el nombre de usuario del cliente.
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            //Mientras todavía hay una conexión con el servidor, continúe escaneando el terminal y luego envíe el mensaje.
            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                bufferedWriter.write(username + ": " + messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            // Gracefully close everything.
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }
    //Escuchar un mensaje es bloquear, así que necesita un hilo separado para eso.
    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;
                //Si bien todavía hay una conexión con el servidor, continúe escuchando los mensajes en un subproceso separado.
                while (socket.isConnected()) {
                    try {
                        //Obtiente los mensajes enviados por otros usuarios e imprímalos en la consola.
                        msgFromGroupChat = bufferedReader.readLine();
                        System.out.println(msgFromGroupChat);
                    } catch (IOException e) {
                        // Close everything gracefully.
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }


    //Método auxiliar para cerrar todo para que no tengas que repetirte.
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {

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

    // Run the program.
    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your username for the group chat: ");
        String username = scanner.nextLine();
        // Crear un socket para conectarse al servidor.
        Socket socket = new Socket("localhost", 5000);

        // Pass the socket and give the client a username.
        Client client = new Client(socket, username);
        // Infinite loop to read and send messages.
        client.listenForMessage();
        client.sendMessage();
    }
}
