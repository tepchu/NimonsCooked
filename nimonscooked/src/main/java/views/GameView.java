package views;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.geometry.*;

import controllers.*;
import models.map.*;
import models.chef.*;
import models.station.*;
import models.item.*;
import models.order.Order;
import models.enums.*;
import java.util.*;

public class GameView extends Application {
    private static final int TILE_SIZE = 60;

    private GameController gameController;
    private Canvas canvas;
    private GraphicsContext gc;

    // HUD Components
    private VBox orderNotificationPanel;
    private Label scoreLabel;
    private Label timeLabel;
    private Label chefInfoLabel;

    @Override
    public void start(Stage primaryStage){

    }

    private HBox createTopLayout(int width){

    }

    private VBox createScorePanel(int width){

    }

    private VBox createTimerPanel(int width){

    }

    private HBox createBottomLayout(int width){

    }

    private void render(){

    }

    private void drawTile(Position pos, TileType type){

    }

    private void drawStation(Position pos, Station station){

    }
}
