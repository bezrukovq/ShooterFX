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
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class ShooterClient extends Application implements Receive {

    public TextField hostField;
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
    private HashMap<Integer, ImageView> enemies = new HashMap<>();


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
        int x = (int) (Math.random()*100);
        host=hostField.getText();
        gameMap.getChildren().removeAll(hostField);
        try {
            s = new Socket(host, PORT);
            service = new ShooterService(s, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        hero = new ImageView(Images.HERO);
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
                        if (!isShooting) {
                            service.sendShot(hero.getX(), hero.getY(), directionH, direction, vertical);
                            RotateTransition rt = new RotateTransition(Duration.seconds(1.5), hero);
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
                        move =hero.getY() + moveSpeed;
                        hero.setY(move>850?850:move);
                        service.sendMove(hero.getX(), hero.getY(), curScale);
                        break;
                    case S:
                        moveSpeed--;
                        break;
                    case W:
                        moveSpeed++;
                        break;
                    case UP:
                        directionH = -0.8;
                        direction = -1;
                        vertical = true;
                        move=hero.getY() - moveSpeed;
                        hero.setY(move<0?0:move);
                        service.sendMove(hero.getX(), hero.getY(), curScale);
                        break;
                    case LEFT:
                        curScale = -1;
                        move =hero.getX() - moveSpeed;
                        hero.setX(move<0?0:move);
                        direction = -1;
                        hero.setScaleX(curScale);
                        vertical = false;
                        directionH = -0.3;
                        service.sendMove(hero.getX(), hero.getY(), curScale);
                        break;
                    case RIGHT:
                        curScale = 1;
                        move=hero.getX() + moveSpeed;
                        hero.setScaleX(curScale);
                        vertical = false;
                        direction = 1;
                        directionH = 1;
                        hero.setX(move>850?850:move);
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

    @Override
    public void rEnemyXY(int id, double x, double y, int scaleX) {
        enemies.get(id).setX(x);
        enemies.get(id).setY(y);
        enemies.get(id).setScaleX(-scaleX);
    }

    @Override
    public void rEnemyShoot(int id, double Ex, double Ey, double directionH, int direction, boolean vertical) {
        RotateTransition rt = new RotateTransition(Duration.seconds(1.5), enemies.get(id));
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

            if (!destroyed)
                c.boundsInParentProperty().addListener((a, b, d) -> {
                    if (hero.getBoundsInParent().intersects(c.getBoundsInParent())) {
                        if (!dead) {
                            hero.setImage(Images.DEAD);
                            service.sendDead();
                            canMove = false;
                            dead = true;
                            pt.stop();
                            gameMap.getChildren().removeAll(c);
                        } else {
                            pt.stop();
                            gameMap.getChildren().removeAll(c);
                            service.sendDestroyed();
                            destroyed=true;
                        }
                    }
                });
            pt.play();
            pt.setOnFinished(event -> gameMap.getChildren().removeAll(c));
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
    }
}
