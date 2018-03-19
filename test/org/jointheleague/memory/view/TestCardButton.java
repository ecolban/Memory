package org.jointheleague.memory.view;

import java.awt.Color;
import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.jointheleague.cards.Card;
import org.jointheleague.cards.Deck;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestCardButton {

    private final Deck deck = new Deck(Color.BLUE);
    private Card card;
    private CardButton button;

    @Before
    public void setUp() {
        card = deck.getCard();
        button = new CardButton(card);
        assertNotNull(button);
        assertTrue(button instanceof JButton);
        assertFalse(button.isFaceUp());
        assertNotNull(button.getIcon());
        assertTrue(button.getIcon() instanceof ImageIcon);
        ImageIcon imageIcon = (ImageIcon) button.getIcon();
        assertImageEquals(card.getBackImage(), imageIcon.getImage());
    }

    @Test
    public void testFlip() {
        assertFalse(button.isFaceUp());
        Icon backIcon = button.getIcon();
        assertNotNull(backIcon);
        button.setFaceUp(true);
        assertTrue(button.isFaceUp());
        Icon faceIcon = button.getIcon();
        assertNotNull(faceIcon);
        assertNotSame(backIcon, faceIcon);
        assertTrue(faceIcon instanceof ImageIcon);
        ImageIcon imageIcon = (ImageIcon) button.getIcon();
        assertImageEquals(card.getFaceImage(), imageIcon.getImage());
        button.setFaceUp(false);
        assertFalse(button.isFaceUp());
        assertSame(backIcon, button.getIcon());
        button.setFaceUp(true);
        assertTrue(button.isFaceUp());
        assertSame(faceIcon, button.getIcon());
    }

    @Test
    public void testGetCard() {
        assertSame(card, button.getCard());
    }

    /**
     * Method intended to assert that two images are equal.
     *
     * @param expected
     * @param actual
     */
    private void assertImageEquals(Image expected, Image actual) {
        assertEquals(expected.getWidth(null), actual.getWidth(null));
        assertEquals(expected.getHeight(null), actual.getHeight(null));
    }

    @Test
    public void testSetCard() {
        button.setFaceUp(true);
        assertTrue(button.isFaceUp());
        Card card2 = deck.getCard();
        assertSame(card, button.getCard());
        assertNotSame(card2, button.getCard());
        button.setCard(card2);
        assertNotSame(card, button.getCard());
        assertSame(card2, button.getCard());
        assertFalse(button.isFaceUp());
    }

    @Test
    public void testSetCardWithNullArgument() {
        try {
            button.setCard(null);
            fail("Argument cannot be null");
        } catch (IllegalArgumentException ignored) {
        }
    }
}
