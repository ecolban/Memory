package org.jointheleague.memory.model;

import java.awt.Color;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jointheleague.cards.Card;
import org.jointheleague.cards.Deck;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestGameModel implements Observer {

    private Deck deck;
    private GameModel sut;
    private boolean[] faceUp;

    @Before
    public void setUp() {
        deck = new Deck(Color.BLUE);
    }

    @Test
    public void testSelectCards() {
        deck.getCard(); // Make sure that the deck is not complete.
        int numCards = 104; // All cards from the deck will be used.
        sut = new GameModel(numCards);
        Card[] cards = sut.getCards();
        for (Card card : cards) {
            assertNotNull("A card cannot be null. " +
                    "This could indicate that the deck ran out of cards. " +
                    "Did you remember to shuffle the deck?", card);
        }
        assertEquals(numCards, cards.length);
        Map<Card, Set<Integer>> distinct = new HashMap<>();
        for (Card card : cards) {
            if (!distinct.containsKey(card)) {
                distinct.put(card, new HashSet<>());
            }
        }
        for (int i = 0; i < cards.length; i++) {
            distinct.get(cards[i]).add(i);
        }
        assertEquals(numCards / 2, distinct.size());
        // Randomness test. Checks that the distance between two matching cards
        // varies sufficiently.
        // If this test fails, your code could still be OK. Rerun a couple of
        // times.
        double mean = 0.0;
        double var = 0.0;
        for (Map.Entry<Card, Set<Integer>> entry : distinct.entrySet()) {
            assertEquals(2, entry.getValue().size());
            Integer[] vals = entry.getValue().toArray(new Integer[0]);
            double dist = Math.abs(vals[0] - vals[1]);
            mean += dist;
            var += dist * dist;
        }
        mean = mean / cards.length;
        var = var / cards.length - mean * mean;
        System.out.format("Mean = %.2f, Std. dev. = %.2f", mean, Math.sqrt(var));
        assertTrue("The average distance between matching cards should be greater than 12.", mean > 12.0);
        assertTrue("The standard deviation of the distance between matching cards should be greater than 20.",
                var > 400.0);
    }

    @Test
    public void testSelectFirst() throws InterruptedException {
        sut = new GameModel(4);
        // sut should notify the observer immediately, i.e. within max 100 ms
        CountDownLatch latch = new CountDownLatch(1);
        Observer observer = (o, arg) -> {
            faceUp = sut.getFaceUp();
            latch.countDown();
        };
        sut.addObserver(observer);
        sut.select(0);
        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
        assertArrayEquals(new boolean[]{true, false, false, false}, faceUp);
    }

    @Test
    public void testSelectTwoNonMatching() throws InterruptedException {
        sut = new GameModel(4);
        Card[] cards = sut.getCards();
        final int firstSelection = 0;
        // Find the card that matches cards[firstSelection]
        int j = 1;
        for (; j < 4; j++) {
            if (cards[firstSelection] == cards[j]) break;
        }
        final int firstSelectionMatch = j;
        // Find a card that does not match cards[firstSelection]
        int secondSelection = firstSelectionMatch == 1 ? 2 : 1;
        sut.select(firstSelection);
        sut.select(secondSelection);
        faceUp = sut.getFaceUp();
        // Verify that even if two cards do not match they are face up ...
        assertTrue(faceUp[firstSelection]);
        assertTrue(faceUp[secondSelection]);
        sut.select(firstSelectionMatch);
        faceUp = sut.getFaceUp();
        // ..., but as soon as a third card is opened, the two non-matching cards are face down.
        assertFalse(faceUp[firstSelection]);
        assertFalse(faceUp[secondSelection]);
        assertTrue(faceUp[firstSelectionMatch]);
        // Verify that within 2 seconds max two non-matching cards are face down.
        CountDownLatch latch = new CountDownLatch(1);
        Observer observer = (o, arg) -> {
            faceUp = sut.getFaceUp();
            if (!faceUp[firstSelectionMatch]) latch.countDown();
        };
        sut.addObserver(observer);
        sut.select(secondSelection);
        faceUp = sut.getFaceUp();
        assertTrue(faceUp[firstSelectionMatch]);
        assertTrue(faceUp[secondSelection]);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertFalse(faceUp[firstSelectionMatch]);
        assertFalse(faceUp[secondSelection]);
    }

    @Test
    public void testSelectTwoMatching() throws InterruptedException {
        sut = new GameModel(4);
        Card[] cards = sut.getCards();
        final int firstSelection = 0;
        // Find the card that matches cards[firstSelection]
        int firstSelectionMatch = 1;
        for (; firstSelectionMatch < 4; firstSelectionMatch++) {
            if (cards[firstSelection] == cards[firstSelectionMatch]) break;
        }
        // Find a card that does not match cards[firstSelection]
        int secondSelection = 1 == firstSelectionMatch ? 2 : 1;
        sut.select(firstSelection);
        // Verify that two matching cards remain face up after 2 seconds
        sut.select(firstSelectionMatch);
        Thread.sleep(2000);
        faceUp = sut.getFaceUp();
        assertTrue(faceUp[firstSelection]);
        assertTrue(faceUp[firstSelectionMatch]);
        // Verify that two matching cards remain face up after a third card is selected
        sut.select(secondSelection);
        faceUp = sut.getFaceUp();
        assertTrue(faceUp[firstSelection]);
        assertTrue(faceUp[firstSelectionMatch]);
        assertTrue(faceUp[secondSelection]);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o != sut) return;
        faceUp = sut.getFaceUp();
    }
}
