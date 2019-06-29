package org.jointheleague.memory.model

import kotlinx.coroutines.*
import org.jointheleague.cards.Card
import org.jointheleague.cards.Deck
import java.awt.Color
import java.util.*
import javax.swing.SwingUtilities


class GameModel(private val numCards: Int) : Observable() {
    private val deck = Deck(Color.RED)
    var cards: Array<Card> = selectCards(numCards)
    var isNewGame: Boolean = false
        private set
    val faceUp: BooleanArray = BooleanArray(numCards)
    private var selection1: Int = 0
    private var selection2: Int = 0
    private var matched = 0
    private var closeUnmatchedCardsJob: Job? = null

    private interface State {
        fun select(i: Int)
        fun closeUnmatchedCards()
    }

    private val zeroCardsUnmatched: State = object : State {

        override fun select(i: Int) {
            if (!faceUp[i]) {
                selection1 = i
                faceUp[i] = true
                currentState = oneCardUnmatched
                isNewGame = false
                setChanged()
                notifyObservers()
            }
        }

        override fun closeUnmatchedCards() {
        }
    }

    private val oneCardUnmatched: State = object : State {

        override fun select(i: Int) {
            if (!faceUp[i]) {
                selection2 = i
                faceUp[i] = true
                if (cards[selection1] == cards[i]) {
                    currentState = zeroCardsUnmatched
                    matched += 2
                } else {
                    currentState = twoCardsUnmatched
                    this@GameModel.closeUnmatchedCardsJob = GlobalScope.launch {
                        delay(1000)
                        SwingUtilities.invokeLater { this@GameModel.closeUnmatchedCards() }
                    }
                }
                setChanged()
                notifyObservers(gameOver)
            }
        }

        override fun closeUnmatchedCards() {
        }
    }

    private val twoCardsUnmatched = object : State {

        override fun select(i: Int) {
            this@GameModel.closeUnmatchedCardsJob?.cancel()
            faceUp[selection1] = false
            faceUp[selection2] = false
            if (!faceUp[i]) {
                selection1 = i
                faceUp[i] = true
                currentState = oneCardUnmatched
            } else {
                currentState = zeroCardsUnmatched
            }
            setChanged()
            notifyObservers()
        }

        override fun closeUnmatchedCards() {
            faceUp[selection1] = false
            faceUp[selection2] = false
            currentState = zeroCardsUnmatched
            setChanged()
            notifyObservers()
        }
    }

    private var currentState: State = zeroCardsUnmatched

    val gameOver: Boolean
        get() = matched == numCards


    fun select(i: Int) {
        currentState.select(i)
    }

    private fun closeUnmatchedCards() {
        currentState.closeUnmatchedCards()
    }

    /**
     * Produces an array containing a random selection of cards where each card
     * occurs exactly two times in the array. Cards occur in random order.
     *
     * @param numCards the number of cards to select (number of pairs = numCards / 2). Must be even.
     * @return the Card array
     */
    private fun selectCards(numCards: Int): Array<Card> {
        val rng = Random()
        if (deck.count < numCards) {
            deck.shuffle()
        }
        var card = deck.card
        val result = Array<Card>(numCards) { card }
        for (i in 2 until result.size) {
            if (i % 2 == 0) {
                card = deck.card
            }
            val j = rng.nextInt(i + 1)
            result[i] = result[j]
            result[j] = card
        }
        isNewGame = true
        return result
    }

    fun reset() {
        currentState = zeroCardsUnmatched
        cards = selectCards(numCards)
        for (i in faceUp.indices) {
            faceUp[i] = false
        }
        matched = 0
        setChanged()
        notifyObservers()
    }
}