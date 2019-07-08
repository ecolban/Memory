package org.jointheleague.memory.model

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.jointheleague.cards.Card
import org.jointheleague.cards.Deck
import java.awt.Color
import java.util.*


const val GAME_OVER = "GAME_OVER"
const val NEW_GAME = "NEW_GAME"

class GameModel(private val numCards: Int) : Observable() {

    private val deck = Deck(Color.RED)
    private var matched = 0
    private val eventQueue = Channel<Int>()

    var cards: Array<Card> = selectCards(numCards)
    val faceUp: BooleanArray = BooleanArray(numCards)

    fun playGame() {
        GlobalScope.launch(Dispatchers.Default) {
            log("playGame launched.")
            var closeUnmatchedCardsJob: Job? = null
            var first = 0
            var second = 0
            while (matched != numCards) {
                val tmp = nextSelected()
                if (closeUnmatchedCardsJob?.isActive == true) {
                    closeUnmatchedCardsJob.cancel()
                    closeUnmatchedCards(first, second)
                }
                first = if (!faceUp[tmp]) tmp else nextFaceDownSelected()
                openCard(first)
                second = nextFaceDownSelected()
                openCard(second)
                if (cards[first] == cards[second]) {
                    matched += 2
                } else {
                    closeUnmatchedCardsJob = launchCloseUnmatchedCards(first, second)
                }
            }
            setChanged()
            notifyObservers(GAME_OVER)
        }
    }

    private fun CoroutineScope.launchCloseUnmatchedCards(first: Int, second: Int): Job {
        return launch {
            log("Launching launchCloseUnmatchedCards...")
            delay(1000)
            closeUnmatchedCards(first, second)
            log("...done!")
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
        setChanged()
        notifyObservers(NEW_GAME)
    }

    /**
     * Logs a message with thread and coroutine info.
     * Run with VM option -Dkotlinx.coroutines.debug
     */
    private fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")
}