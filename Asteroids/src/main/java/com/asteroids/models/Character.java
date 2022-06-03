package com.asteroids.models;

import com.asteroids.window.App;
import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

public abstract class Character {
    private Polygon character;
    private Point2D movement;

    public Character(Polygon character, int x, int y) {
        this.character = character;
        this.character.setTranslateX(x);
        this.character.setTranslateY(y);
        this.movement = new Point2D(0, 0);
    }

    public Polygon getCharacter() {
        return this.character;
    }

    public void turnLeft() {
        this.character.setRotate(this.character.getRotate() - 3);
    }

    public void turnRight() {
        this.character.setRotate(this.character.getRotate() + 3);
    }

    public void move() {
        this.character.setTranslateX(this.character.getTranslateX() + this.movement.getX());
        this.character.setTranslateY(this.character.getTranslateY() + this.movement.getY());

        if (this.character.getTranslateX() < 0) {
            this.character.setTranslateX(this.character.getTranslateX() + App.WIDTH);
        }

        if (this.character.getTranslateX() > App.WIDTH) {
            this.character.setTranslateX(this.character.getTranslateX() % App.WIDTH);
        }

        if (this.character.getTranslateY() < 0) {
            this.character.setTranslateY(this.character.getTranslateY() + App.HEIGHT);
        }

        if (this.character.getTranslateY() > App.HEIGHT) {
            this.character.setTranslateY(this.character.getTranslateY() % App.HEIGHT);
        }
    }

    public void accelerate() {
        double changeX = Math.cos(Math.toRadians(this.character.getRotate()));
        double changeY = Math.sin(Math.toRadians(this.character.getRotate()));

        changeX *= 0.05;
        changeY *= 0.05;

        if (changeX >= -0.5 && changeX <= 0.15) {
            this.movement = this.movement.add(changeX, changeY);
        }
    }

    public boolean collide(Character other) {
        Shape collisionArea = Shape.intersect(this.character, other.getCharacter());
        return collisionArea.getBoundsInLocal().getWidth() != -1;
    }

    public void setMovement(Point2D movement) {
        this.movement = movement;
    }

    public Point2D getMovement() {
        return this.movement;
    }
}
