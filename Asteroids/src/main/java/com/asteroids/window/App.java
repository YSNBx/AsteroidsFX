package com.asteroids.window;

import com.asteroids.models.*;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class App extends Application {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static int GLOBAL_POINTS_COUNTER = 0;
    public static int CHARGES_COUNTER = 5;
    public static int HIGHSCORE = 0;


    @Override
    public void start(Stage stage) {
        
        //Init Audio Files
        Map<String, AudioClip> audioFiles = createAudioFilesAndSetVolume();
        MediaPlayer backgroundMusic = new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("/audio/gamestart.mp3")).toExternalForm()));

        backgroundMusic.setVolume(0.25);
        backgroundMusic.setAutoPlay(true);

        BorderPane pane = new BorderPane();
        pane.setPrefSize(WIDTH, HEIGHT);
        Text pointCounter = new Text(15, 30, "Points: 0");
        pointCounter.setFont(Font.font("Monoid", 15));

        Text projectileCounter = new Text(550, 30, "Laser Charges Left: " + CHARGES_COUNTER + "/5");
        projectileCounter.setFont(Font.font("Monoid", 15));

        pane.getChildren().add(pointCounter);
        pane.getChildren().add(projectileCounter);
    
        
        HBox hboxTop = new HBox();
        VBox vbox = new VBox();

        AtomicInteger points = new AtomicInteger();

        Ship ship = new Ship(WIDTH / 2, HEIGHT / 2);
        ship.getCharacter().setFill(Color.BLUEVIOLET);

        List<Projectile> projectiles = new ArrayList<>();
        List<Asteroid> asteroids = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Random rnd = new Random();
            Asteroid asteroid = new Asteroid(rnd.nextInt(WIDTH / 3), rnd.nextInt(HEIGHT));
            asteroid.getCharacter().setFill(Color.DARKGREY);
            asteroid.getCharacter().setStroke(Color.BLACK);
            asteroids.add(asteroid);
        }

        pane.getChildren().add(ship.getCharacter());
        asteroids.forEach(asteroid -> pane.getChildren().add(asteroid.getCharacter()));

        Scene scene = new Scene(pane);

        Map<KeyCode, Boolean> pressedKeys = new EnumMap<>(KeyCode.class);

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE && projectiles.size() < 5) {
                audioFiles.get("laserCannon").play();

                projectileCounter.setText("Laser Charges Left: " + --CHARGES_COUNTER + "/5");
                Projectile projectile = new Projectile(
                    (int) ship.getCharacter().getTranslateX(), (int) ship.getCharacter().getTranslateY()
                );
                projectile.getCharacter().setFill(Color.RED);
                projectile.getCharacter().setRotate(ship.getCharacter().getRotate());
                projectiles.add(projectile);
                projectile.accelerate();
                projectile.setMovement(projectile.getMovement().normalize().multiply(3));
                pane.getChildren().add(projectile.getCharacter());
            } else if (event.getCode() == KeyCode.SPACE) {
                audioFiles.get("emptyClip").play();
            } else {
                pressedKeys.put(event.getCode(), Boolean.TRUE);
            }
        });

        scene.setOnKeyReleased(event -> {
            pressedKeys.put(event.getCode(), Boolean.FALSE);
        });

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                backgroundMusic.play();
                backgroundMusic.setOnEndOfMedia(new Runnable() {
                    @Override
                    public void run() {
                        backgroundMusic.seek(Duration.ZERO);
                        backgroundMusic.play();
                    }
                });
                if (Math.random() < 0.005) {
                    Asteroid asteroid = new Asteroid(WIDTH, HEIGHT);
                    asteroid.getCharacter().setFill(Color.DARKGREY);
                    asteroid.getCharacter().setStroke(Color.BLACK);

                    if (!asteroid.collide(ship)) {
                        asteroids.add(asteroid);
                        pane.getChildren().add(asteroid.getCharacter());
                    }
                }

                if (Boolean.TRUE.equals(pressedKeys.getOrDefault(KeyCode.LEFT, false))) {
                    ship.turnLeft();
                }

                if (Boolean.TRUE.equals(pressedKeys.getOrDefault(KeyCode.RIGHT, false))) {
                    ship.turnRight();
                }

                if (Boolean.TRUE.equals(pressedKeys.getOrDefault(KeyCode.UP, false))) {
                    ship.accelerate();
                }

                ship.move();
                
                asteroids.forEach(Asteroid::move);
                projectiles.forEach(Projectile::move);

                projectiles.forEach(projectile -> {
                    asteroids.forEach(asteroid -> {
                        if (projectile.collide(asteroid)) {
                            audioFiles.get("asteroidHitSound").play();
                            projectile.setAlive(false);
                            asteroid.setAlive(false);
                            GLOBAL_POINTS_COUNTER++;
                        }
                    });

                    if (!projectile.isAlive()) {
                        pointCounter.setText("Points: " + points.incrementAndGet());
                        projectileCounter.setText("Laser Charges Left: " + ++CHARGES_COUNTER + "/5");
                    }
                });

                projectiles.stream()
                        .filter(projectile -> !projectile.isAlive())
                        .forEach(projectile -> pane.getChildren().remove(projectile.getCharacter()));
                projectiles.removeAll(projectiles.stream()
                        .filter(projectile -> !projectile.isAlive()).toList());

                asteroids.stream()
                        .filter(asteroid -> !asteroid.isAlive())
                        .forEach(asteroid -> pane.getChildren().remove(asteroid.getCharacter()));
                asteroids.removeAll(asteroids.stream()
                        .filter(asteroid -> !asteroid.isAlive()).toList());

                asteroids.forEach(asteroid -> {
                    if (ship.collide(asteroid)) {
                        stop();
                        pane.getChildren().clear();
                        
                        backgroundMusic.stop();
                        audioFiles.get("deathSound").play();

                        Button button = new Button("Play Again");
                        button.setStyle(
                                "-fx-background-radius: 8px;" +
                                "-fx-focus-color: transparent;" +
                                "-fx-faint-focus-color: transparent;"

                        );

                        button.setFont(Font.font("Monoid", 15));
                        button.setPrefSize(200, 50);

                        pane.setTop(hboxTop);
                        pane.setCenter(vbox);

                        Label pointCounter = new Label("Points: " + GLOBAL_POINTS_COUNTER);
                        pointCounter.setFont(Font.font("Monoid", 25));
                        pointCounter.setPadding(new Insets(0, 0, 20, 0));

                        Label highscoreCounter = new Label();
                        highscoreCounter.setFont(Font.font("Monoid", 35));
                        highscoreCounter.setPadding(new Insets(20, 0, 0, 0));
                        
                        if (GLOBAL_POINTS_COUNTER > HIGHSCORE) {
                            HIGHSCORE = GLOBAL_POINTS_COUNTER;
                        }
                        
                        highscoreCounter.setText("Highscore: " + HIGHSCORE);
                        
                        saveHighscoreToFile();

                        hboxTop.getChildren().add(highscoreCounter);
                        hboxTop.setAlignment(Pos.CENTER);
                        hboxTop.setPadding(new Insets(20, 0, 0, 0));

                        
                        vbox.getChildren().add(pointCounter);
                        vbox.getChildren().add(button);
                        vbox.setAlignment(Pos.CENTER);
                        vbox.setPadding(new Insets(0, 0, 20, 0));

                        button.setOnAction(e -> {
                            GLOBAL_POINTS_COUNTER = 0;
                            CHARGES_COUNTER = 5;
                            App app = new App();
                            app.start(stage);
                        });
                    }
                });
            }

        }.start();


        stage.setTitle("Asteroids!");
        stage.setScene(scene);
        stage.show();
    }

    public Map<String, AudioClip> createAudioFilesAndSetVolume() {
        Map<String, AudioClip> tmp = new HashMap<>();

        tmp.put("laserCannon", new AudioClip(Objects.requireNonNull(getClass().getResource("/audio/pew_pew.wav")).toExternalForm()));
        tmp.put("emptyClip", new AudioClip(Objects.requireNonNull(getClass().getResource("/audio/empty.wav")).toExternalForm()));
        tmp.put("deathSound", new AudioClip(Objects.requireNonNull(getClass().getResource("/audio/death.wav")).toExternalForm()));
        tmp.put("asteroidHitSound", new AudioClip(Objects.requireNonNull(getClass().getResource("/audio/break.wav")).toExternalForm()));
        
        setVolume(tmp);

        return (HashMap<String, AudioClip>) tmp;
    }

    public void setVolume(Map<String, AudioClip> tmp) {
        for (var entry : tmp.entrySet()) {
            entry.getValue().setVolume(0.15);
        }
    }

    public void saveHighscoreToFile() {
        try (FileWriter write = new FileWriter("highscores")) {
            if (HIGHSCORE >= GLOBAL_POINTS_COUNTER) {
                write.write("Highscore: " + HIGHSCORE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(App.class);
    }
}
