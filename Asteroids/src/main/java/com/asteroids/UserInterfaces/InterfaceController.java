package com.asteroids.UserInterfaces;

public class InterfaceController {
    private SuperInterface superInterface;

    public void execute() {
        superInterface.start();
    }

    public void setInterface(SuperInterface superInterface) {
        this.superInterface = superInterface;
    }
}
