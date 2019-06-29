package org.jointheleague.memory.view

import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JButton

import org.jointheleague.cards.Card

/**
 * A CardButton is a JButton that is associated with a Card. A CardButton can be
 * in one of two states; face up or face down. In the face up state it displays
 * the face of the associated Card, and in the face down state it displays the
 * back of the associated Card.
 *
 * @see javax.swing.JButton
 */
class CardButton(card: Card) : JButton() {

    var card: Card = card
        set(card) {
            field = card
            this.faceIcon = ImageIcon(card.faceImage)
            this.backIcon = ImageIcon(card.backImage)
            faceUp = false
            icon = backIcon
        }


    private var faceIcon: Icon? = null

    private var backIcon: Icon? = null
    var faceUp: Boolean = false
        set(faceUp) {
            if (field != faceUp) {
                field = faceUp
                icon = if (faceUp) faceIcon else backIcon
                repaint()
            }
        }

    init{
        faceIcon = ImageIcon(card.faceImage)
        backIcon = ImageIcon(card.backImage)
        faceUp = false
        icon = backIcon
    }

}