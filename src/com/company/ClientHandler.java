package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private ArrayList<ClientHandler> clients;

    public ClientHandler(Socket clientSocket, ArrayList<ClientHandler> clients) throws IOException {
        this.client = clientSocket;
        this.clients = clients;
         in = new BufferedReader(new InputStreamReader(client.getInputStream()));
         out = new PrintWriter(client.getOutputStream(), true);

    }
    @Override
    public void run(){
        try {
            while (true) {
                String request = in.readLine();
                if (request.contains("name")) {
                    out.println(Server.getRandomName());
                }else if (request.startsWith("say")) {
                    int firstSpace = request.indexOf(" ");

                    if(firstSpace != -1) {

                        outToAll(request.substring(firstSpace+1));
                    }
                } else {
                        out.println("Escriba  name para mostrar la descripcion");
                    }
                }
        } catch (IOException e) {
            System.err.println("IO exeption in clien handler");
            System.err.println(e.getStackTrace());
        } finally {
            out.close();
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void outToAll(String msg) {
        System.out.println("Esto imprime la cosas que me da null"+msg);
        for (ClientHandler aClient : clients){
            aClient.out.println(msg);

        }
    }
}
