package org.jointheleague.memory.view;

import org.jointheleague.cards.Card;
import org.jointheleague.memory.controller.GameController;
import org.jointheleague.memory.model.GameModel;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

public class MemoryGame extends JPanel implements Runnable, Observer {

    /**
     * Constants
     */
    private static final int NUM_ROWS = 4;
    private static final int NUM_COLUMNS = 6;
    private static final int NUM_CARDS = NUM_ROWS * NUM_COLUMNS;

    private final GameController controller;
    private final GameModel model;
    private final CardButton[] buttons;

    public MemoryGame() {
        this.model = new GameModel(NUM_CARDS);
        this.controller = new GameController(model);
        this.buttons = new CardButton[NUM_CARDS];
    }


    /**
     * Starts the game.
     */
    public static void main(String[] args) {

        SwingUtilities.invokeLater(new MemoryGame());
    }

    /**
     * Initializes the MemoryGame. Generates a list of CardButtons and places
     * them on a grid inside a JFrame.
     */
    @Override
    public void run() {
        JFrame frame = new JFrame("Memory Game");
        setLayout(new GridLayout(NUM_ROWS, NUM_COLUMNS));
        Card[] cards = model.getCards();
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new CardButton(cards[i]);
            add(buttons[i]);
            final int buttonId = i;
            buttons[i].addActionListener(e -> controller.buttonClicked(buttonId));
        }
        frame.add(this);
        frame.pack();
        frame.addWindowListener(controller);
        frame.setVisible(true);
        model.addObserver(this);
    }


    private boolean playAnotherGame() {
        int answer = JOptionPane.showConfirmDialog(this, "Do you want to play again?", "Play again?",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return answer == JOptionPane.YES_OPTION;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o != model) return;
        SwingUtilities.invokeLater(() ->
        {
            if (model.isNewGame()) {
                Card[] cards = model.getCards();
                for (int i = 0; i < cards.length; i++) {
                    buttons[i].setCard(cards[i]);
                }
            } else {
                boolean[] faceUp = model.getFaceUp();
                for (int i = 0; i < faceUp.length; i++) {
                    buttons[i].setFaceUp(faceUp[i]);
                }
            }
            if (model.isGameOver()) {
                controller.onGameOver(playAnotherGame());
            }
        });
    }
}