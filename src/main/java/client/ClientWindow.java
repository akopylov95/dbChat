package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientWindow extends JFrame{
    private JTextArea jta;
    private JTextField jtf;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private JTextFieldWithHint jtfLogin;
    private JPasswordField jtfPassword;
    private Thread threadRead;
    private boolean isAuthorized = false;
    private static final String AUTH_MESSAGE = "a.u.t.h.o.r.i.z.e.d";
    private JPanel authPanel;
    private JPanel bottom;
    private JTextArea jtaClients;

    public void setAuthorized(boolean authorized) {
        if(authorized){
            authPanel.setVisible(false);
            bottom.setVisible(true);
        } else {
            authPanel.setVisible(true);
            bottom.setVisible(false);
        }
        isAuthorized = authorized;
    }

    public ClientWindow(){
        setTitle("Client");
        setBounds(700,300,400,400);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        jta = new JTextArea();
        jta.setEditable(false);
        jta.setLineWrap(true);
        JScrollPane jsp = new JScrollPane(jta);
        add(jsp,BorderLayout.CENTER);

        JPanel clients = new JPanel();
        jtaClients = new JTextArea(2, 15);
        clients.add(jtaClients);
        jtaClients.setEditable(false);
        add(clients, BorderLayout.EAST);

        bottom = new JPanel(new BorderLayout());
        jtf = new JTextField();
        JButton jSendMsg = new JButton("Send");
        bottom.add(jtf, BorderLayout.CENTER);
        bottom.add(jSendMsg, BorderLayout.EAST);
        add(bottom,BorderLayout.SOUTH);
        bottom.setVisible(false);

        jSendMsg.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMsg();
            }
        });

        jtf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMsg();
            }
        });
        jtf.grabFocus();

        authPanel = new JPanel(new GridLayout());
        jtfLogin = new JTextFieldWithHint("login");
        jtfPassword = new JPasswordField();
        JButton jbAuth = new JButton("Authorize");
        authPanel.add(jtfLogin);
        authPanel.add(jtfPassword);
        authPanel.add(jbAuth);
        add(authPanel,BorderLayout.NORTH);

        jbAuth.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connect();
                try {
                    out.writeUTF("/auth " + jtfLogin.getText() + " " + new String(jtfPassword.getPassword()));
                    out.flush();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        setVisible(true);
    }

    public void updateClientsList(String str){
        str = str.substring(1 , str.length() - 1);
        String[] w = str.split("\\,");
        jtaClients.setText("");
        for(String s : w){
            jtaClients.append(s + "\n");
        }
    }

    public void connect(){
        if(socket == null || (socket != null && socket.isClosed())){
            try {
                socket = new Socket("127.0.0.1", 8189);
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (threadRead == null){
                threadRead = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            while(true){
                                String str = in.readUTF();
                                System.out.println(str);
                                if(str != null && !isAuthorized){
                                    if(str.equals(AUTH_MESSAGE)){
                                        setAuthorized(true);
                                        break;
                                    } else {
                                        jta.append(str + "\n");
                                        jta.setCaretPosition(jta.getDocument().getLength());
                                    }
                                }
                            }
                            while(true){
                                String str = in.readUTF();
                                if(str != null && isAuthorized){
                                    if(str.startsWith("/cllist")){
                                        updateClientsList(str.substring("/cllist ".length()));
                                    } else {
                                        System.out.println("server: " + str);
                                        jta.append(str + "\n");
                                        jta.setCaretPosition(jta.getDocument().getLength());
                                    }
                                }
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (IOException e){
                            jta.append("Connection lost" + "\n");
                            setAuthorized(false);
                            System.out.println("IOException");
                        } finally {
                            threadRead = null;
                            try {
                                System.out.println("socket closing");
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                threadRead.start();
            }
        }
    }

    public void sendMsg(){
        try {
            if(!jtf.getText().trim().isEmpty()){
                out.writeUTF(jtf.getText());
                out.flush();
            }
        } catch (IOException e){
            jta.append("Connection lost" + "\n");
            setAuthorized(false);
            System.out.println("IOException");
            threadRead = null;
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        jtf.setText("");
        jtf.grabFocus();
    }
}
