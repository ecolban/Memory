package org.jointheleague.memory.controller

import org.jointheleague.memory.model.GameModel

import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import kotlin.system.exitProcess

class GameController(private val model: GameModel) : WindowAdapter() {

    fun buttonClicked(i: Int) {
        model.select(i)
    }

    override fun windowClosing(e: WindowEvent?) {
        exitProcess(status = 0)
    }

    fun onGameOver(playAgain: Boolean) {
        if (playAgain) {
            model.reset()
        } else {
            exitProcess(status = 0)
        }
    }
}