package org.jointheleague.memory.view;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.jointheleague.cards.Card;

/**
 * A CardButton is a JButton that is associated with a Card. A CardButton can be
 * in one of two states; face up or face down. In the face up state it displays
 * the face of the associated Card, and in the face down state it displays the
 * back of the associated Card.
 *
 * @see javax.swing.JButton
 */
@SuppressWarnings("serial")
public class CardButton extends JButton {

    private boolean faceUp = false;
    private Card card;
    private Icon faceIcon;
    private Icon backIcon;

    /**
     * Constructor
     *
     * @param card the Card of this CardButton
     * @throws IllegalArgumentException if card is null
     */
    public CardButton(Card card) throws IllegalArgumentException {
        super();
        setCard(card);
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        if (card == null) throw new IllegalArgumentException("card must be non-null");
        this.card = card;
        this.faceIcon = new ImageIcon(card.getFaceImage());
        this.backIcon = new ImageIcon(card.getBackImage());
        faceUp = false;
        setIcon(backIcon);
    }

    public void setFaceUp(boolean faceUp) {
        if (this.faceUp != faceUp) {
            this.faceUp = faceUp;
            setIcon(faceUp ? faceIcon : backIcon);
            repaint();
        }
    }
}