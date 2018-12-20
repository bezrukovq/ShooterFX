package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ServerClass {
    final int PORT = 8083;
    HashMap<Integer,Client> clients = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerClass server = new ServerClass();
        server.startServer();
    }

    public void startServer(){
        ServerSocket s = null;
        try {
            s = new ServerSocket(PORT);
            while (true) {
                Socket client = s.accept();
                clients.put(Client.uCount,new Client(this,client));
                recreateRecords();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void pMoved(int id, Double x, Double y,String scale){
        if(clients.size()!=0)
        clients.values().forEach(client -> {
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
            clients.values().forEach(client -> {
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
            clients.values().forEach(client -> {
                if(client.userId!=id) {
                    client.os.println(1);
                    client.os.println(id);
                    client.os.println(x);
                    client.os.println(y);
                }
            });
    }

    public void pHit(int userId,int code) {
        if(clients.size()!=0)
            clients.values().forEach(client -> {
                if(client.userId!=userId) {
                    client.os.println(code);
                    client.os.println(userId);
                }
            });
        if (code==4) {
            recreateRecords();
        }
    }

    private void recreateRecords() {
        ArrayList<Client> topClients = new ArrayList<>(clients.values());
        Collections.sort(topClients);
        int size = topClients.size()>5?5:topClients.size();
        StringBuilder records= new StringBuilder();
        for (int i = 0; i <size; i++)
            records.append(i+1+")"+topClients.get(i).nickName).append(":").append(topClients.get(i).kills).append("   ");
        if(clients.size()!=0)
            clients.values().forEach(client -> {
                    client.os.println(7);
                    client.os.println(records);
            });
    }
}
