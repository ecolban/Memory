package org.jointheleague.memory.model;

import org.jointheleague.cards.Card;
import org.jointheleague.cards.Deck;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Random;


public class GameModel extends Observable {

    private int numCards;
    private final Deck deck = new Deck(Color.RED);
    private Card[] cards;
    private boolean isNewGame;
    private boolean[] faceUp;
    private int selection1;
    private int selection2;
    private int matched = 0;
    private Timer autoFlipTimer = new Timer(1000, e -> closeUnmatched());

    public GameModel(int numCards) {
        this.numCards = numCards;
        cards = selectCards(numCards);
        faceUp = new boolean[numCards];
        autoFlipTimer.setRepeats(false);
    }

    private interface State {

        void select(int i);

        void closeUnmatched();
    }

    private final State zeroCardsUnmatched = new State() {

        @Override
        public void select(int i) {
            if (!faceUp[i]) {
                selection1 = i;
                faceUp[i] = true;
                currentState = oneCardUnmatched;
                isNewGame = false;
                setChanged();
                notifyObservers();
            }
        }

        @Override
        public void closeUnmatched() {

        }
    };

    private final State oneCardUnmatched = new State() {

        @Override
        public void select(int i) {
            if (!faceUp[i]) {
                selection2 = i;
                faceUp[i] = true;
                if (cards[selection1] == cards[i]) {
                    currentState = zeroCardsUnmatched;
                    matched += 2;
                } else {
                    currentState = twoCardsUnmatched;
                    autoFlipTimer.start();
                }
                setChanged();
                notifyObservers();
            }
        }

        @Override
        public void closeUnmatched() {

        }
    };

    private final State twoCardsUnmatched = new State() {

        @Override
        public void select(int i) {
            if (autoFlipTimer.isRunning()) autoFlipTimer.stop();
            faceUp[selection1] = false;
            faceUp[selection2] = false;
            if (!faceUp[i]) {
                selection1 = i;
                faceUp[i] = true;
                currentState = oneCardUnmatched;
            } else {
                currentState = zeroCardsUnmatched;
            }
            setChanged();
            notifyObservers();
        }

        @Override
        public void closeUnmatched() {
            faceUp[selection1] = false;
            faceUp[selection2] = false;
            currentState = zeroCardsUnmatched;
            setChanged();
            notifyObservers();
        }
    };

    private State currentState = zeroCardsUnmatched;

    public void select(int i) {
        currentState.select(i);
    }

    private void closeUnmatched() {
        currentState.closeUnmatched();
    }

    /**
     * Produces an array containing a random selection of cards where each card
     * occurs exactly two times in the array. Cards occur in random order.
     * <p>
     *
     * @param numCards the number of cards to select (number of pairs = numCards / 2). Must be even.
     * @return the Card array
     */
    private Card[] selectCards(int numCards) {
        Card[] result = new Card[numCards];
        Random rng = new Random();
        if (deck.getCount() < numCards) {
            deck.shuffle();
        }
        Card card = deck.getCard();
        result[0] = card;
        for (int i = 1; i < result.length; i++) {
            if (i % 2 == 0) {
                card = deck.getCard();
            }
            int j = rng.nextInt(i + 1);
            result[i] = result[j];
            result[j] = card;
        }
        isNewGame = true;
        return result;
    }

    public void reset() {
        currentState = zeroCardsUnmatched;
        cards = selectCards(numCards);
        for (int i = 0; i < faceUp.length; i++) {
            faceUp[i] = false;
        }
        matched = 0;
        setChanged();
        notifyObservers();
    }

    public Card[] getCards() {
        return cards.clone();
    }

    public boolean[] getFaceUp() {
        return faceUp.clone();
    }

    public boolean isGameOver() {
        return matched == numCards;
    }

    public boolean isNewGame() {
        return isNewGame;
    }

}