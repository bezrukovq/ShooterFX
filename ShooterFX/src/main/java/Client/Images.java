package Client;

import javafx.scene.image.Image;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;

public class Images {
    public static final Image MAP = new Image("/source/map.jpg",700,700,true,true);
    public static final BackgroundImage background = new BackgroundImage(Images.MAP, BackgroundRepeat.ROUND,BackgroundRepeat.ROUND, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
    public static final Image DEAD = new Image("/source/dead.png",64,64,true,true);
    public static final Image ENEMY = new Image("/source/enemy.png", 64, 64, true, true);
    public static final Image FIREBALL = new Image("/source/fireball.png", 64, 30, true, true);
    public static final Image HERO = new Image("/source/Player.png", 64, 64, true, true);
}
