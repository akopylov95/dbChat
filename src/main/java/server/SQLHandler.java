package server;

import java.sql.*;

public class SQLHandler {
    public static String dbUrl = "jdbc:mysql://localhost/chatUser?useUnicode=true&serverTimezone=UTC&useSSL=false&verifyServerCertificate=false";
    public static String user = "eiolv";
    public static String password = "230115";
    private static PreparedStatement stmt;

    private static Connection c;

    public static void connect(){
        try {
            c = DriverManager.getConnection(dbUrl, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect(){
        try {
            if (c != null && !c.isClosed()){
                c.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getNicByLoginAndPass(String login, String pass){
        String w = null;
        try{
            stmt = c.prepareStatement("SELECT Nickname FROM User WHERE Login = ? AND Password = ?;");
            stmt.setString(1, login);
            stmt.setString(2, pass);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                w = rs.getString("Nickname");
            }
        } catch (SQLException e){
            System.out.println("SQLHandler.getNicByLoginAndPass");
        }
        return w;
    }
}
