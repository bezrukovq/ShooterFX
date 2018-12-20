package Client;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ShooterService {
    private Receive r;
    private BufferedReader br;
    private PrintWriter os;
    private int myId;
    private int id;


    public ShooterService(Socket client, Receive r,String nickName,int x, int y) {
        this.r = r;
        try {
            os = new PrintWriter(client.getOutputStream(), true);
            br = new BufferedReader((new InputStreamReader((client.getInputStream()))));
            myId = Integer.parseInt(br.readLine());
            os.println(nickName);
            os.println(x);
            os.println(y);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread t = new Thread(this::receive);
        t.setDaemon(true);
        t.start();
    }

    public void sendMove(double x, double y, int scaleX) {
        os.println(2);
        os.println(x);
        os.println(y);
        os.println(scaleX);
    }

    public void sendShot(double Ex, double Ey, double directionH, int direction, boolean vertical) {
        os.println(3);
        os.println(Ex);
        os.println(Ey);
        os.println(directionH);
        os.println(direction);
        os.println(vertical);
    }

    public void receive() {
        try {

            while (true) {
                int code = Integer.parseInt(br.readLine());
                switch (code) {
                    case 1:
                        //NEW
                        id = Integer.parseInt(br.readLine());
                        double x = Double.parseDouble(br.readLine());
                        double y = Double.parseDouble(br.readLine());
                        Platform.runLater(
                                () -> r.newEnemy(id, x, y)
                        );
                        break;
                    case 2:
                        //MOVE
                        id = Integer.parseInt(br.readLine());
                        x = Double.parseDouble(br.readLine());
                        y = Double.parseDouble(br.readLine());
                        int scaleX = Integer.parseInt(br.readLine());
                        Platform.runLater(() -> r.rEnemyXY(id, x, y, scaleX));
                        break;
                    case 3:
                        //SHOT
                        int id = Integer.parseInt(br.readLine());
                        double Ex = Double.parseDouble(br.readLine());
                        double Ey = Double.parseDouble(br.readLine());
                        double directionH = Double.parseDouble(br.readLine());
                        int direction = Integer.parseInt(br.readLine());
                        boolean vertical = !br.readLine().equals("false");
                        Platform.runLater(
                                () -> r.rEnemyShoot(id, Ex, Ey, directionH, direction, vertical)
                        );
                        break;
                    case 4:
                        //dead
                        id = Integer.parseInt(br.readLine());
                        Platform.runLater(() -> r.rEnemyDead(id));
                        break;
                    case 5:
                        //destroyed
                        id = Integer.parseInt(br.readLine());
                        Platform.runLater(() -> r.rDestroyed(id));
                        break;
                    case 6:
                        //hit
                        id = Integer.parseInt(br.readLine());
                        Platform.runLater(()-> r.rHit(id));
                        break;
                    case 7:
                        //renew records
                        String records = br.readLine();
                        Platform.runLater(()-> r.updateRecords(records));
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendDead(int id) {
        os.println(4);
        os.println(id);
    }

    public void sendDestroyed() {
        os.println(5);
    }

    public void sendHit() {
        os.println(6);
    }
}
