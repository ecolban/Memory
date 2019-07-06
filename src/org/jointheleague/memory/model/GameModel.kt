package org.jointheleague.memory.model

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.jointheleague.cards.Card
import org.jointheleague.cards.Deck
import java.awt.Color
import java.util.*


class GameModel(private val numCards: Int) : Observable() {
    private val deck = Deck(Color.RED)
    var cards: Array<Card> = selectCards(numCards)
    var isNewGame: Boolean = false
        private set
    val faceUp: BooleanArray = BooleanArray(numCards)
    private var matched = 0
    private val eventQueue = Channel<Int>()

    val gameOver: Boolean
        get() = matched == numCards

    /**
     * Logs a message with thread and coroutine info.
     * Run with VM option -Dkotlinx.coroutines.debug
     */
    private fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

    fun playGame() {
        GlobalScope.launch {
            log("playGame launched.")
            isNewGame = false
            var closeUnmatchedCardsJob: Job? = null
            var first = 0
            var second = 0
            while (!gameOver) {
                val tmp = nextSelected()
                if (closeUnmatchedCardsJob?.isActive == true) {
                    closeUnmatchedCardsJob.cancel()
                    closeUnmatchedCards(first, second)
                }
                first = if (!faceUp[tmp]) tmp else nextFaceDownSelected()
                openCard(first)
                second = nextFaceDownSelected()
                if (cards[first] == cards[second]) {
                    matched += 2
                } else {
                    closeUnmatchedCardsJob = launchCloseUnmatchedCards(first, second)
                }
                openCard(second)
            }
        }
    }

    private fun CoroutineScope.launchCloseUnmatchedCards(first: Int, second: Int): Job {
        return launch {
            log("Launching launchCloseUnmatchedCards...")
            delay(1000)
            closeUnmatchedCards(first, second)
        }
    }

    private suspend fun nextSelected(): Int {
        log("nextSelected")
        return eventQueue.receive()
    }

    private suspend fun nextFaceDownSelected(): Int {
        log("nextFaceDownSelected")
        var selection = eventQueue.receive()
        while (faceUp[selection]) {
            selection = eventQueue.receive()
        }
        return selection
    }


    fun select(i: Int) = runBlocking {
        log("select($i)")
        eventQueue.send(i)
    }

    private fun openCard(i: Int) {
        faceUp[i] = true
        setChanged()
        notifyObservers()
    }

    private fun closeUnmatchedCards(selection1: Int, selection2: Int) {
        if (cards[selection1] != cards[selection2]) {
            faceUp[selection1] = false
            faceUp[selection2] = false
            setChanged()
            notifyObservers()
        }
    }

    /**
     * Produces an array containing a random selection of cards where each card
     * occurs exactly two times in the array. Cards occur in random order.
     *
     * @param numCards an even number of cards to select (number of pairs = numCards / 2).
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
        return result
    }

    fun reset() {
        cards = selectCards(numCards)
        for (i in faceUp.indices) {
            faceUp[i] = false
        }
        matched = 0
        isNewGame = true
        setChanged()
        notifyObservers()
    }
}