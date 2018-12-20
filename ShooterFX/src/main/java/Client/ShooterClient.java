package Client;

import javafx.animation.PathTransition;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class ShooterClient extends Application implements Receive {

    public TextField hostField;

    public Label top;
    public TextField nickName;
    public Label hp;
    @FXML
    private Button btn_connect;
    @FXML
    private AnchorPane gameMap;

    private final int PORT = 8083;
    private String host = "localhost";
    private Socket s;
    private ShooterService service;
    private ImageView hero;
    private int moveSpeed = 5;
    private byte direction = 1;
    private double directionH = 1;
    private boolean vertical = false;
    private int curScale = 1;
    private boolean isShooting = false;
    private boolean canMove = true;
    private boolean dead = false;
    private boolean destroyed = false;
    private int health = 100;
    public HashMap<Integer, ImageView> enemies = new HashMap<>();


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader
                .load(getClass()
                        .getResource("/fxml/sample.fxml"));
        primaryStage.setTitle("Shooter");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    public void connect(ActionEvent actionEvent) {
        int x = (int) (Math.random() * 650);
        int y = (int) (Math.random() * 650);
        host = hostField.getText();
        try {
            s = new Socket(host, PORT);
            service = new ShooterService(s, this, nickName.getText(), x, y);
        } catch (IOException e) {
            e.printStackTrace();
        }
        hp.setText("100");
        gameMap.setBackground(new Background(Images.background));
        gameMap.getChildren().removeAll(hostField, nickName);
        hero = new ImageView(Images.HERO);
        hero.setX(x);
        hero.setY(y);
        gameMap.getChildren().addAll(hero);
        btn_connect.setVisible(false);
        btn_connect.setDisable(true);
        setUpControls();
    }

    public void setUpControls() {
        gameMap.getScene().setOnKeyReleased(event -> {
            if (canMove)
                switch (event.getCode()) {
                    case SPACE:
                        //shoot
                        if (!isShooting) {
                            service.sendShot(hero.getX(), hero.getY(), directionH, direction, vertical);
                            RotateTransition rt = new RotateTransition(Duration.seconds(1), hero);
                            rt.setByAngle(-360);
                            rt.play();
                            isShooting = true;
                            canMove = false;
                            rt.setOnFinished(event1 -> {
                                ImageView c = new ImageView(Images.FIREBALL);
                                if (vertical)
                                    c.setRotate(90 * direction);
                                else
                                    c.setScaleX(direction);
                                PathElement dir = vertical ? new VLineTo(hero.getY() + 600 * direction) : new HLineTo(hero.getX() + 600 * direction);
                                double x = vertical ? hero.getX() + 32 : hero.getX() + 100 * directionH;
                                double y = vertical ? hero.getY() + 40 * directionH : (hero.getY() + 32);
                                Path path = new Path();
                                path.getElements().addAll(new MoveTo(x, y), dir);
                                path.setFill(null);
                                gameMap.getChildren().addAll(c);
                                c.setY(y);
                                c.setX(x);
                                PathTransition pt = new PathTransition(Duration.millis(2000), path, c);
                                pt.play();
                                isShooting = false;
                                canMove = true;
                                c.boundsInParentProperty().addListener((a, b, d) -> {
                                    for (ImageView hero : enemies.values()) {
                                        if (hero.getBoundsInParent().intersects(c.getBoundsInParent())) {
                                            pt.stop();
                                            gameMap.getChildren().removeAll(c);
                                        }
                                    }
                                });
                                pt.setOnFinished(event2 -> gameMap.getChildren().removeAll(c));
                            });
                        }
                        break;
                }
        });
        gameMap.getScene().setOnKeyPressed(event -> Platform.runLater(() -> {
            double move;
            if (canMove && !dead)
                switch (event.getCode()) {
                    case DOWN:
                        direction = 1;
                        vertical = true;
                        directionH = 2.5;
                        move = hero.getY() + moveSpeed;
                        hero.setY(move > 650 ? 650 : move);
                        service.sendMove(hero.getX(), hero.getY(), curScale);
                        break;
                    case L:
                        moveSpeed--;
                        break;
                    case P:
                        //secret cheat for speed(for test and fun)
                        moveSpeed++;
                        break;
                    case UP:
                        directionH = -0.8;
                        direction = -1;
                        vertical = true;
                        move = hero.getY() - moveSpeed;
                        hero.setY(move < 0 ? 0 : move);
                        service.sendMove(hero.getX(), hero.getY(), curScale);
                        break;
                    case LEFT:
                        curScale = -1;
                        move = hero.getX() - moveSpeed;
                        hero.setX(move < 0 ? 0 : move);
                        direction = -1;
                        hero.setScaleX(curScale);
                        vertical = false;
                        directionH = -0.3;
                        service.sendMove(hero.getX(), hero.getY(), curScale);
                        break;
                    case RIGHT:
                        curScale = 1;
                        move = hero.getX() + moveSpeed;
                        hero.setScaleX(curScale);
                        vertical = false;
                        direction = 1;
                        directionH = 1;
                        hero.setX(move > 650 ? 650 : move);
                        service.sendMove(hero.getX(), hero.getY(), curScale);
                        break;

                }
        }));
    }

    @Override
    public void newEnemy(int id, double x, double y) {
        ImageView enemy = new ImageView(Images.ENEMY);
        enemies.put(id, enemy);
        enemy.setX(x);
        enemy.setY(y);
        System.out.println(enemies + "    " + id);
        gameMap.getChildren().addAll(enemy);
    }

    //Enemy move
    @Override
    public void rEnemyXY(int id, double x, double y, int scaleX) {
        enemies.get(id).setX(x);
        enemies.get(id).setY(y);
        enemies.get(id).setScaleX(-scaleX);
    }

    @Override
    public void rEnemyShoot(int id, double Ex, double Ey, double directionH, int direction, boolean vertical) {
        RotateTransition rt = new RotateTransition(Duration.seconds(1), enemies.get(id));
        rt.setByAngle(-360);
        rt.play();
        rt.setOnFinished(event1 -> {
            ImageView c = new ImageView(Images.FIREBALL);
            if (vertical)
                c.setRotate(90 * direction);
            else
                c.setScaleX(direction);
            PathElement dir = vertical ? new VLineTo(Ey + 600 * direction) : new HLineTo(Ex + 600 * direction);
            double x = vertical ? Ex + 32 : Ex + 100 * directionH;
            double y = vertical ? Ey + 40 * directionH : (Ey + 32);
            Path path = new Path();
            path.getElements().addAll(new MoveTo(x, y), dir);
            path.setFill(null);
            gameMap.getChildren().addAll(c);
            c.setY(y);
            c.setX(x);
            PathTransition pt = new PathTransition(Duration.millis(2000), path, c);

            c.boundsInParentProperty().addListener((a, b, d) -> {
                for (ImageView hero : enemies.values()) {
                    if (hero.getBoundsInParent().intersects(c.getBoundsInParent())) {
                        pt.stop();
                        gameMap.getChildren().removeAll(c);
                    }
                }
                if (!destroyed)
                    if (hero.getBoundsInParent().intersects(c.getBoundsInParent())) {
                        if (!dead) {
                            health -= 34;
                            hp.setText(String.valueOf(health));
                            service.sendHit();
                            animateHit(hero);
                            if (health < 0) {
                                hp.setText("DEAD");
                                hero.setImage(Images.DEAD);
                                service.sendDead(id);
                                canMove = false;
                                dead = true;
                            }
                            gameMap.getChildren().removeAll(c);
                            pt.stop();
                        } else {
                            pt.stop();
                            gameMap.getChildren().removeAll(c);
                            service.sendDestroyed();
                            destroyed = true;
                        }
                    }
            });
            pt.play();
            pt.setOnFinished(event -> gameMap.getChildren().removeAll(c));
        });
    }

    private void animateHit(ImageView hero) {
        RotateTransition rT = new RotateTransition(Duration.seconds(0.1), hero);
        rT.setByAngle(-30);
        rT.play();
        rT.setOnFinished(event -> {
            RotateTransition rT2 = new RotateTransition(Duration.seconds(0.1), hero);
            rT2.setByAngle(60);
            rT2.play();
            rT2.setOnFinished(event2 ->
            {
                RotateTransition rT3 = new RotateTransition(Duration.seconds(0.1), hero);
                rT3.setByAngle(-30);
                rT3.play();
            });
        });
    }

    @Override
    public void rEnemyDead(int id) {
        enemies.get(id).setImage(Images.DEAD);
    }

    @Override
    public void rDestroyed(int id) {
        gameMap.getChildren().removeAll(enemies.get(id));
        enemies.remove(id);
        System.out.println(enemies.size());
    }

    @Override
    public void rHit(int id) {
        animateHit(enemies.get(id));
    }

    @Override
    public void updateRecords(String records) {
        top.setText(records);
    }
}
