package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {
    Socket socket;
    Thread thread;
    Server server;
    BufferedReader br = null;
    PrintWriter os = null;
    int userId;
    double x =0;
    double y =0;
    static int uCount = 0;

    public Client(Server server, Socket socket) {
        this.socket = socket;
        this.server = server;
        userId = uCount++;
        try {
            br = new BufferedReader((new InputStreamReader((socket.getInputStream()))));
            os = new PrintWriter(socket.getOutputStream(), true);
            os.println(userId);
            server.pConnected(userId,x,y);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (server.clients.size() != 0)
            for (Client client : server.clients) {
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
                         x = Double.parseDouble(br.readLine());
                         y = Double.parseDouble(br.readLine());
                         String scale = br.readLine();
                         server.pMoved(userId, x, y, scale);
                         break;
                    case 3:
                        String p1 = br.readLine();
                        String p2 = br.readLine();
                        String p3 = br.readLine();
                        String p4 = br.readLine();
                        String p5 = br.readLine();
                        server.pShot(userId, p1, p2, p3, p4, p5);
                        break;
                    case 4:
                        server.pDead(userId,4);
                        break;
                    case 5:
                        server.pDead(userId,5);
                        break;
                }
            } catch (java.net.SocketException e) {
                server.pDead(userId, 5);
                server.clients.remove(this);
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



   /* public static void main(String[] args) {
        final int PORT = 3456;
        String host = "localhost";
        Socket s;
        try {
            s = new Socket(host, PORT);
            String myname = ChatService.askForName(s,true);
            String friendname = ChatService.askForName(s,false);
            ChatService.chat(myname,friendname,s,false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}
