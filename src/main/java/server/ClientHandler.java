package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable{
    private Socket s;
    private DataInputStream in;
    private DataOutputStream out;
    private String name;
    private MyServer owner;
    private static final String AUTH_MESSAGE = "a.u.t.h.o.r.i.z.e.d";

    public ClientHandler(Socket s, MyServer ms){
        this.s = s;
        this.owner = ms;
        try {
            in = new DataInputStream(s.getInputStream()); //создали поток и получаем InputStream из сокета
            out = new DataOutputStream(s.getOutputStream()); //создали поток и получаем InputStream из сокета
        } catch (IOException e) {
            e.printStackTrace();
        }
        name = "";
    }

    public String getName() {
        return name;
    }

    @Override
    public void run() {
        try{
            while(true){                            //цикл для проверки авторизации
                String str = in.readUTF();

                if (str != null) {
                    if (name.isEmpty()) {
                        String[] x = str.split(" ");
                        if (x.length == 3 && x[0].equals("/auth")) {
                            String login = x[1];
                            String pass = x[2];
                            String nick = SQLHandler.getNicByLoginAndPass(login, pass);
                            if (nick != null) {
                                if(!owner.isNickBusy(nick)){
                                    name = nick;
                                    sendMsg(AUTH_MESSAGE);
                                    owner.addClient(this);
                                    break;
                                } else {
                                    sendMsg("This login already is use");
                                }
                            } else {
                                sendMsg("Wrong login/password");
                            }
                        }
                    }
                }
            }

            while (true) {                          //цикл для отправки сообщений
                String str = in.readUTF();
                if (str != null) {
                    if(str.startsWith("/")){
                        String[] w = str.split(" ");
                        switch (w[0]){
                            case "/msg" :
                                String nick = w[1];
                                String msg = str.substring(w[0].length()+w[1].length()+2);
                                owner.personalMsg(nick, " from " + getName() + ": " +msg);
                                owner.personalMsg(getName(), " to " + nick + ": " +msg);
                                break;
                            case "/list" :
                                sendMsg(owner.allClients());
                                break;
                        }
                    } else {
                        System.out.print("Client " +name + " says: " + str + "\n");
                        owner.broadcastMsg(name + ": " + str);
                    }
                }
                try {
                    Thread.currentThread().sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }catch (IOException e){
            owner.removeClient(this);
            try {
                s.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            System.out.println("IO_ERROR");
        }
    }

    public void sendMsg(String msg){
        try {
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
