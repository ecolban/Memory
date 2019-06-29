package org.jointheleague.memory.controller

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jointheleague.memory.model.GameModel

import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

class GameController(private val model: GameModel) : WindowAdapter() {

    fun buttonClicked(i: Int) {
        GlobalScope.launch {
            model.select(i)
        }
    }

    override fun windowClosing(e: WindowEvent?) {
        System.exit(0)
    }

    fun onGameOver(playAgain: Boolean) {
        if (playAgain) {
            model.reset()
        } else {
            System.exit(0)
        }
    }
}