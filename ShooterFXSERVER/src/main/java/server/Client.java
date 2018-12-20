package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable,Comparable<Client> {
    Socket socket;
    Thread thread;
    ServerClass server;
    BufferedReader br = null;
    PrintWriter os = null;
    int kills = 0;
    int userId;
    boolean dead = false;
    double x =0;
    double y =0;
    static int uCount = 0;
    String nickName;

    public Client(ServerClass server, Socket socket) {
        this.socket = socket;
        this.server = server;
        userId = uCount++;
        try {
            br = new BufferedReader((new InputStreamReader((socket.getInputStream()))));
            os = new PrintWriter(socket.getOutputStream(), true);
            os.println(userId);
            nickName= br.readLine();
            x = Integer.parseInt(br.readLine());
            y= Integer.parseInt(br.readLine());
            //send new user to others
            server.pConnected(userId,x,y);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //send all enmies to new user
        if (server.clients.size() != 0)
            for (Client client : server.clients.values()) {
                if(!client.dead) {
                    os.println(1);
                    os.println(client.userId);
                    os.println(client.x);
                    os.println(client.y);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        thread = new Thread(this);
        thread.start();
    }


    @Override
    public void run() {
        while (true) {
            try {
                int code = Integer.parseInt(br.readLine());
                switch (code) {
                    case 2:
                        //move
                         x = Double.parseDouble(br.readLine());
                         y = Double.parseDouble(br.readLine());
                         String scale = br.readLine();
                         server.pMoved(userId, x, y, scale);
                         break;
                    case 3:
                        //shot
                        String p1 = br.readLine();
                        String p2 = br.readLine();
                        String p3 = br.readLine();
                        String p4 = br.readLine();
                        String p5 = br.readLine();
                        server.pShot(userId, p1, p2, p3, p4, p5);
                        break;
                    case 4:
                        //dead
                        server.clients.get(Integer.parseInt(br.readLine())).kills++;
                        server.pHit(userId,4);
                        break;
                    case 5:
                        //destroyed
                        dead=true;
                        server.pHit(userId,5);
                        break;
                    case 6:
                        //hit
                        server.pHit(userId,6);
                        break;
                }
            } catch (java.net.SocketException e) {
                server.pHit(userId, 5);
                server.clients.remove(userId);
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int compareTo(Client o) {
        return o.kills-kills;
    }
}
