package org.jointheleague.memory.view

import org.jointheleague.cards.Card
import org.jointheleague.cards.Deck
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.awt.Color
import java.awt.Image
import javax.swing.ImageIcon

class TestCardButton {

    private val deck = Deck(Color.BLUE)
    private lateinit var card: Card
    private lateinit var button: CardButton

    @Before
    fun setUp() {
        card = deck.card
        button = CardButton(card)
        assertNotNull(button)
        assertFalse(button.faceUp)
        assertNotNull(button.icon)
        assertTrue(button.icon is ImageIcon)
        val imageIcon = button.icon as ImageIcon
        assertImageEquals(card.backImage, imageIcon.image)
    }

    @Test
    fun testFlip() {
        assertFalse(button.faceUp)
        val backIcon = button.icon
        assertNotNull(backIcon)
        button.faceUp = true
        assertTrue(button.faceUp)
        val faceIcon = button.icon
        assertNotNull(faceIcon)
        assertNotSame(backIcon, faceIcon)
        assertTrue(faceIcon is ImageIcon)
        val imageIcon = button.icon as ImageIcon
        assertImageEquals(card.faceImage, imageIcon.image)
        button.faceUp = false
        assertFalse(button.faceUp)
        assertSame(backIcon, button.icon)
        button.faceUp = true
        assertTrue(button.faceUp)
        assertSame(faceIcon, button.icon)
    }

    @Test
    fun testGetCard() {
        assertSame(card, button.card)
    }

    /**
     * Method intended to assert that two images are equal.
     *
     * @param expected
     * @param actual
     */
    private fun assertImageEquals(expected: Image, actual: Image) {
        assertEquals(expected.getWidth(null).toLong(), actual.getWidth(null).toLong())
        assertEquals(expected.getHeight(null).toLong(), actual.getHeight(null).toLong())
    }

    @Test
    fun testSetCard() {
        button.faceUp = true
        assertTrue(button.faceUp)
        val card2 = deck.card
        assertSame(card, button.card)
        assertNotSame(card2, button.card)
        button.card = card2
        assertNotSame(card, button.card)
        assertSame(card2, button.card)
        assertFalse(button.faceUp)
    }

}
