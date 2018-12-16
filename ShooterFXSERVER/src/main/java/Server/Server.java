package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    final int PORT = 8083;
    ArrayList<Client> clients = new ArrayList<>();
    public static void main(String[] args) throws IOException {
        Server server = new Server();
    }
    public Server(){
        ServerSocket s = null;
        try {
            s = new ServerSocket(PORT);
            while (true) {
                Socket client = s.accept();
                clients.add(new Client(this,client));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void pMoved(int id, Double x, Double y,String scale){
        if(clients.size()!=0)
        clients.forEach(client -> {
            if(client.userId!=id) {
                client.os.println(2);
                client.os.println(id);
                client.os.println(x);
                client.os.println(y);
                client.os.println(scale);
            }
        });
    }
    public void pShot(int id, String Ex,String Ey,String directionH,String direction,String vertical){
        if(clients.size()!=0)
            clients.forEach(client -> {
                if(client.userId!=id) {
                    client.os.println(3);
                    client.os.println(id);
                    client.os.println(Ex);
                    client.os.println(Ey);
                    client.os.println(directionH);
                    client.os.println(direction);
                    client.os.println(vertical);
                }

            });
    }
    public void pConnected(int id,double x,double y){
        if(clients.size()!=0)
            clients.forEach(client -> {
                if(client.userId!=id) {
                    client.os.println(1);
                    client.os.println(id);
                    client.os.println(x);
                    client.os.println(y);
                }
            });
    }

    public void pDead(int userId,int code) {
        if(clients.size()!=0)
            clients.forEach(client -> {
                if(client.userId!=userId) {
                    client.os.println(code);
                    client.os.println(userId);
                }
            });
    }
}
