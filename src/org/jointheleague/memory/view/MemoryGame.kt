package org.jointheleague.memory.view

import org.jointheleague.memory.controller.GameController
import org.jointheleague.memory.model.GameModel
import java.awt.GridLayout
import java.util.*
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.SwingUtilities

/*
 * Constants
 */
private const val NUM_ROWS = 4
private const val NUM_COLUMNS = 6
private const val NUM_CARDS = NUM_ROWS * NUM_COLUMNS

class MemoryGame : JPanel(), Runnable, Observer {

    private val model: GameModel = GameModel(NUM_CARDS)
    private val controller: GameController = GameController(model)
    private val buttons: Array<CardButton> = Array(NUM_CARDS) { i ->
        CardButton(model.cards[i])
    }

    /**
     * Initializes the MemoryGame. Generates a list of CardButtons and places
     * them on a grid inside a JFrame.
     */
    override fun run() {
        val frame = JFrame("Memory Game")
        layout = GridLayout(NUM_ROWS, NUM_COLUMNS)
        for ((i, button) in buttons.withIndex()) {
            add(button)
            button.addActionListener { controller.buttonClicked(i) }
        }
        frame.add(this)
        frame.pack()
        frame.addWindowListener(controller)
        frame.isVisible = true
        model.addObserver(this)
    }


    private fun playAnotherGame(): Boolean {
        val answer = JOptionPane.showConfirmDialog(this, "Do you want to play again?", "Play again?",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
        return answer == JOptionPane.YES_OPTION
    }

    override fun update(observable: Observable, arg: Any?) {
        if (observable !== model) return
        if (model.isNewGame) {
            val cards = model.cards
            for ((i, c) in cards.withIndex()) {
                buttons[i].card = c
            }
        } else {
            for ((i, v) in model.faceUp.withIndex()) {
                buttons[i].faceUp = v
            }
        }
        if (model.gameOver) {
            controller.onGameOver(playAnotherGame())
        }
    }
}

fun main() {
    SwingUtilities.invokeLater(MemoryGame())
}