package Client;
public interface Receive {
    void newEnemy(int id, double x, double y);
    void rEnemyXY(int id, double x, double y, int scaleX);
    void rEnemyShoot(int id, double Ex, double Ey, double directionH, int direction, boolean vertical);
    void  rEnemyDead(int id);
    void rDestroyed(int id);

    void rHit(int id);

    void updateRecords(String records);
}
