package server;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class MyServer {
    private ArrayList<ClientHandler> clients = new ArrayList<>();
    public MyServer(){
        ServerSocket server = null; //создали сервер
        Socket s = null;
        SQLHandler.connect();

        try {
            server = new ServerSocket(8189); //дали серверу порт 8189
            while(true){
                System.out.println("Waiting for clients");
                s = server.accept(); //ожидаем подключение клиентов (дальнейший код не выполняется)
                System.out.println("Client connected");
                ClientHandler ch = new ClientHandler(s, this);
                Thread t = new Thread(ch);
                t.start();
            }
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            try {
                server.close();
                SQLHandler.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void broadcastMsg(String msg){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String s = sdf.format(cal.getTime());
        for (ClientHandler ch :
                clients) {
            ch.sendMsg(s + " " + msg);
        }
    }

    public synchronized void broadcastSystemMsg(String msg){
        for (ClientHandler ch :
                clients) {
            ch.sendMsg(msg);
        }
    }

    public synchronized void removeClient(ClientHandler ch){
        if(clients.contains(ch)){
            clients.remove(ch);
            broadcastSystemMsg("/cllist " + allClients());
            broadcastMsg(ch.getName() + " disconnected from chatroom");
        }
    }

    public synchronized void addClient(ClientHandler ch){
        if(!clients.contains(ch)){
            clients.add(ch);
            broadcastSystemMsg("/cllist " + allClients());
            broadcastMsg(ch.getName() + " connected to the chatroom");
        }
    }

    public synchronized boolean isNickBusy(String nick){
        for (ClientHandler ch :
                clients) {
            if(ch.getName().equals(nick)){
                return true;
            }
        }
        return false;
    }

    public synchronized void personalMsg(String nick, String msg){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String s = sdf.format(cal.getTime());
        for (ClientHandler ch :
                clients) {
            if(ch.getName().equals(nick)){
                ch.sendMsg(s + " " + msg);
            }
        }
    }

    public synchronized String allClients(){
        return Arrays.toString(clients.toArray());
    }
}
