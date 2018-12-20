import Client.ShooterClient;
import org.junit.Assert;
import org.junit.Test;

public class GameTests {

    @Test
    public void testAddNewEnemy() {
        ShooterClient shooterClient = new ShooterClient();
        new Thread(new Runnable() {
            @Override
            public void run() {
                shooterClient.main(new String[0]);
            }
        }).start();
        try {
            Thread.sleep(1000);
            shooterClient.newEnemy(1, 0, 0);
        } catch (NullPointerException | InterruptedException e) {
        }
        Assert.assertEquals(1, shooterClient.enemies.size());
    }

    @Test
    public void testEnemyMove() {
        ShooterClient shooterClient = new ShooterClient();
        new Thread(new Runnable() {
            @Override
            public void run() {
                shooterClient.main(new String[0]);
            }
        }).start();
        try {
            Thread.sleep(1000);
            shooterClient.newEnemy(1, 0, 0);
            shooterClient.rEnemyXY(1, 50, 50, 1);
        } catch (NullPointerException | InterruptedException e) {
        }
        Assert.assertEquals(50, shooterClient.enemies.get(1).getX(), 50);
        Assert.assertEquals(50, shooterClient.enemies.get(1).getY(), 50);
    }

    @Test
    public void testEnemyDestroyed() {
        ShooterClient shooterClient = new ShooterClient();
        new Thread(new Runnable() {
            @Override
            public void run() {
                shooterClient.main(new String[0]);
            }
        }).start();
        try {
            Thread.sleep(1000);
            shooterClient.newEnemy(1, 0, 0);
            shooterClient.rDestroyed(1);
        } catch (NullPointerException | InterruptedException e) {
        }
        Assert.assertEquals(0, shooterClient.enemies.size());
    }

    @Test
    public void testUpdateRec() {
        ShooterClient shooterClient = new ShooterClient();
        new Thread(new Runnable() {
            @Override
            public void run() {
                shooterClient.main(new String[0]);
            }
        }).start();
        try {
            Thread.sleep(1000);
            shooterClient.updateRecords("nRec");
        } catch (NullPointerException | InterruptedException e) {
        }
        Assert.assertTrue(shooterClient.top.getText().equals("nRec"));
    }
}
