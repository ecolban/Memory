package org.jointheleague.memory.controller;

import org.jointheleague.memory.model.GameModel;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameController extends WindowAdapter {

    private final GameModel model;

    public GameController(GameModel model) {
        this.model = model;
    }

    public void buttonClicked(int i) {
        model.select(i);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        System.exit(0);
    }

    public void onGameOver(boolean playAgain) {
        if (playAgain) {
            model.reset();
        } else {
            System.exit(0);
        }
    }
}