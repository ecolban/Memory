package org.jointheleague.memory.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jointheleague.cards.Card;
import org.jointheleague.cards.Deck;
import org.junit.Before;
import org.junit.Test;

public class TestGameModel {

    private Deck deck;

    @Before
    public void setUp() {
        deck = new Deck(Color.BLUE);
    }

    @Test
    public void testSelectCards() {
        deck.getCard(); // Make sure that the deck is not complete.
        int numCards = 104; // All cards from the deck will be used.
        GameModel model = new GameModel(numCards);
        Card[] cards = model.getCards();
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
}
