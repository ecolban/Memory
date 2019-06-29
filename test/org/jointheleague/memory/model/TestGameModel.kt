package org.jointheleague.memory.model

import org.jointheleague.cards.Card
import org.junit.Assert.*
import org.junit.Test
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class TestGameModel {

    private lateinit var sut: GameModel

    @Test
    fun testSelectCards() {
        val numCards: Int = 104 // All cards from the deck will be used.
        sut = GameModel(numCards)
        val cards = sut.cards
        for (card in cards) {
            assertNotNull("A card cannot be null. " +
                    "This could indicate that the deck ran out of cards. " +
                    "Did you remember to shuffle the deck?", card)
        }
        assertEquals(numCards, cards.size)
        val cardToPositionsMap = HashMap<Card, MutableSet<Int>>(numCards / 2)
        for (card in cards) {
            cardToPositionsMap[card] = HashSet()
        }
        for ((i, c) in cards.withIndex()) {
            cardToPositionsMap[c]?.add(i)
        }
        assertEquals((numCards / 2), cardToPositionsMap.size)
        // Randomness test. Checks that the distance between two matching cards
        // varies sufficiently.
        // If this test fails, your code could still be OK. Rerun a couple of
        // times.
        var mean = 0.0
        var variance = 0.0
        for (positions in cardToPositionsMap.values) {
            assertEquals(2, positions.size)
            val pos = positions.toIntArray()
            val dist = Math.abs(pos[0] - pos[1]).toDouble()
            mean += dist
            variance += dist * dist
        }
        mean /= cards.size
        variance = variance / cards.size - mean * mean
        println("Mean = %.2f, Std. dev. = %.2f".format(mean, Math.sqrt(variance)))
        assertTrue("The average distance between matching cards should be greater than 12.", mean > 12.0)
        assertTrue("The standard deviation of the distance between matching cards should be greater than 20.",
                variance > 400.0)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSelectFirst() {
        sut = GameModel(4)
        // sut should notify the observer immediately, i.e. within max 100 ms
        val latch = CountDownLatch(1)
        val observer = Observer { _, _ ->
            latch.countDown()
        }
        sut.addObserver(observer)
        sut.select(0)
        assertTrue(latch.await(100, TimeUnit.MILLISECONDS))
        assertArrayEquals(booleanArrayOf(true, false, false, false), sut.faceUp)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSelectTwoNonMatching() {
        val numCards = 4
        sut = GameModel(numCards)
        val cards = sut.cards
        val firstSelection = Random().nextInt(numCards)
        val card0 = cards[firstSelection]
        // Find the card that matches card0
        val matchingSelection = (0 until cards.size).first { i ->
            i != firstSelection && cards[i] == card0
        }
        // Find a card that does not match card0
        val nonMatchingSelection = (0 until cards.size).first { i ->
            cards[i] != card0
        }
        sut.select(firstSelection)
        sut.select(nonMatchingSelection)
        // Verify that even if two cards do not match they are face up ...
        assertTrue(sut.faceUp[firstSelection])
        assertTrue(sut.faceUp[nonMatchingSelection])
        sut.select(matchingSelection)
        // ..., but as soon as a third card is selected, the two non-matching cards are face down.
        assertFalse(sut.faceUp[firstSelection])
        assertFalse(sut.faceUp[nonMatchingSelection])
        assertTrue(sut.faceUp[matchingSelection])
        // Verify that within 2 seconds max two non-matching cards are face down.
        sut.select(nonMatchingSelection)
        assertTrue(sut.faceUp[nonMatchingSelection])
        val latch = CountDownLatch(1)
        val observer = Observer { _, _ ->
            if (!sut.faceUp[matchingSelection]) latch.countDown()
        }
        sut.addObserver(observer)
        assertTrue(latch.await(2, TimeUnit.SECONDS))
        assertFalse(sut.faceUp[matchingSelection])
        assertFalse(sut.faceUp[nonMatchingSelection])
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSelectTwoMatching() {
        val numCards = 4
        sut = GameModel(numCards)
        val cards = sut.cards
        val firstSelection = Random().nextInt(numCards)
        val card0 = cards[firstSelection]
        // Find the card that matches card0
        val matchingSelection = (0 until cards.size).first { i ->
            i != firstSelection && cards[i] == card0
        }
        // Find a card that does not match card0
        val nonMatchingSelection = (0 until cards.size).first { i ->
            cards[i] != card0
        }
        sut.select(firstSelection)
        // Verify that two matching cards remain face up after 2 seconds
        sut.select(matchingSelection)
        Thread.sleep(2000)
        assertTrue(sut.faceUp[firstSelection])
        assertTrue(sut.faceUp[matchingSelection])
        // Verify that two matching cards remain face up after a third card is selected
        sut.select(nonMatchingSelection)
        assertTrue(sut.faceUp[firstSelection])
        assertTrue(sut.faceUp[matchingSelection])
        assertTrue(sut.faceUp[nonMatchingSelection])
    }
}
