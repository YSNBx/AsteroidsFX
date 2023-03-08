package com.asteroids.UserInterfaces;

import com.asteroids.window.App;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class GameOverMenu implements SuperInterface {
  private AnimationTimer animationTimer;

  private BorderPane pane;

  private MediaPlayer mediaPlayer;

  private Map<String, AudioClip> audioFiles;

  private HBox hboxTop;

  private VBox vbox;

  private Stage stage;

  public GameOverMenu(AnimationTimer timer, BorderPane pane, MediaPlayer mediaPlayer, Map<String, AudioClip> audioFiles, HBox hboxTop, VBox vbox, Stage stage) {
    this.animationTimer = timer;
    this.pane = pane;
    this.mediaPlayer = mediaPlayer;
    this.audioFiles = audioFiles;
    this.hboxTop = hboxTop;
    this.vbox = vbox;
    this.stage = stage;
  }

  @Override
  public void start() {
    pane.getChildren().clear();

    mediaPlayer.stop();
    audioFiles.get("deathSound").play();

    Button button = new Button("Play Again");
    button.setStyle("-fx-background-radius: 8px;" + "-fx-focus-color: transparent;" + "-fx-faint-focus-color: transparent;"

    );

    pane.setTop(hboxTop);
    pane.setCenter(vbox);

    button.setFont(Font.font("Monoid", 15));
    button.setPrefSize(200, 50);

    Label pointCounter = new Label("Points: " + App.GLOBAL_POINTS_COUNTER);
    pointCounter.setFont(Font.font("Monoid", 25));
    pointCounter.setPadding(new Insets(0, 0, 20, 0));

    Label highscoreCounter = new Label();
    highscoreCounter.setFont(Font.font("Monoid", 35));
    highscoreCounter.setPadding(new Insets(20, 0, 0, 0));

    if (App.GLOBAL_POINTS_COUNTER > App.HIGHSCORE) {
      App.HIGHSCORE = App.GLOBAL_POINTS_COUNTER;
    }

    highscoreCounter.setText("Highscore: " + App.HIGHSCORE);

    saveHighscoreToFile();

    hboxTop.getChildren().add(highscoreCounter);
    hboxTop.setAlignment(Pos.CENTER);
    hboxTop.setPadding(new Insets(20, 0, 0, 0));

    vbox.getChildren().add(pointCounter);
    vbox.getChildren().add(button);
    vbox.setAlignment(Pos.CENTER);
    vbox.setPadding(new Insets(0, 0, 20, 0));

    button.setOnAction(e -> {
      App.GLOBAL_POINTS_COUNTER = 0;
      App.CHARGES_COUNTER = 5;
      App app = new App();
      app.start(stage);
    });
  }

  public void saveHighscoreToFile() {
    try (FileWriter write = new FileWriter("highscores")) {
      if (App.HIGHSCORE >= App.GLOBAL_POINTS_COUNTER) {
        write.write("Highscore: " + App.HIGHSCORE);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
