#Instructions

##Introduction

In this project, we will customize an existing class using inheritance. In this case, we will make a `CardButton` class by extending `JButton`. Then, we will use the `CardButton` class to create a Memory game. In the Memory game, the user tries to find matching cards among a set of cards laid down in a grid. We will use a CardButton for each card. Initially, all the cards are facing down. The user flips two cards, and if they match, they remain facing up, otherwise the are flipped back so they are facing down again. The game ends when the user has found all the matching cards.

We will have to make sure that the user cannot cheat. To do this we must ensure that if the user flips two cards that do not match, those two cards are flipped back before the user can continue. This will require a "smart" `ActionListener` that will have to take into account the *state* of the game when handling button clicks. We will use a small _finite state machine_ to helps us keep track of what the action listener must do.

This project will also teach you a useful _shuffling_ algorithm. 

###Part 1 - The CardButton class

This project's test folder contains a `TestCardButton` class. Write the `CardButton` class in the `src` folder in a package named `org.jointheleague.memory` (the same package name as the `TestCardButton` class). Make sure that all the tests in `TestCardButton` pass. The tests should provide you with sufficient information on how to implement the `CardButton` class. 

Next you can test your `CardButton` in a simple GUI application. Create a class `SingleCard` that has a main method, and when run, shows a frame with a single `CardButton` in it. Add an `ActionListener` to the `CardButton` such that, when the user clicks on the card, it flips. You should by now have had some practice in creating GUI applications, so this part shouldn't be too difficult. However, make sure that you ***do not build the GUI on the main thread***. Java's GUI framework, Swing, is a little sensitive on this issue, and if you build the GUI on the main thread, every now and then, your application crashes. Here is a setup you can use:

    public class SingleCard implements Runnable, ActionListener {

        public static void main(String[] args) {
            SwingUtilities.invokeLater(new SingleCard());
        }

        @Override
        public void run() {
        // build your GUI here
        // ...
        }
        
        // ...
    }

###Part 2 - The MemoryGame class

####Step 1
You are now going to build a GUI with a grid of `CardButton`s. You can use `GridLayout` to layout the cards. Here is a setup you may use:

    public class MemoryGame extends JPanel implements Runnable, ActionListener {
        
        public void run() {
			JFrame frame = new JFrame("Memory");
    		setLayout(new GridLayout(NUM_ROWS, NUM_COLUMNS));
    		
    		// ...
    	}
    }
    
Add `NUM_ROWS * NUM_COLUMNS` `CardButton` instances to the grid and add `this` as the `ActionListener` to each of the buttons. When the user clicks on a card, the card should flip. To find out which `CardButton` to flip, you can use `getSource()`, as shown here:

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof CardButton) {
            CardButton b = (CardButton) e.getSource();
            // flip b
    }
        
Add a main method to the `MemoryGame` class and run the `MemoryGame`. The application should show a grid of cards and each time the user clicks on a card, it flips. 

#### Step 2

It is not yet possible to play the game, because we haven't made sure that the cards come in pairs. When we call `deck.getCard()` a _different_ card is returned each time. So the next thing we'll do is to get half as many `Card` instances from `deck` and use each card in two `CardButton` instances.

Question: Is it possible to have a grid where both `NUM_ROWS` and `NUM_COLUMNS` are odd? 

Create a method: 

    public static Cards[] selectCards(Deck deck, int numCards) {
       // Fill in your code here
    }

that returns a shuffled array of `Card`s where each card occurs exactly twice. Make sure that the `testSelectCards()` test passes. Here is some code that shuffles an `int` array `a` of length `n`. You'll have to adapt the code for your `selectCards()` method.

	Random rng = new Random();
    for (int i = a.length - 1; i > 0; i--) {
    	// randomly choose an index between 0 and i inclusive:
    	int j = rng.nextInt(i + 1);
    	// swap a[j] and a[i]
    	// your code here
    }

Update the application to use the cards returned by `selectCards()` when building the `CardButton`s and rerun the program. The user should now be able to play the game. However, there is nothing to prevent the user from cheating. The user could just flip every card regardless of whether there is a match until all cards are face up. A user who does not want to cheat will have to flip non-matching cards so they face down again before continuing, which is kind of tedious. 

####Step 3
 
In this step, we will modify the `actionPerformed()` method to handle clicks depending on how many unmatched cards are facing up. At any time, there can be at most two unmatched cards facing up, so there are three cases (which we call states) to consider. We will use an instance variable to hold the current state: 

    private int state 
, and two instance variables:
    
    private CardButton firstCardButton 
and 

    private CardButton secondCardButton

In state:

- __State 0:__ Zero unmatched cards facing up
    - If the user clicks on a card facing up, ignore the click. 
    - If the user clicks on a card facing down, assign that button to `firstCardButton` and flip it (so it faces up). Go to state 1 (i.e., assign 1 to the variable `state`).

- __State 1:__ One unmatched card facing up
    - If the user clicks on a card facing up, ignore the click. 
    - If the user clicks on a card facing down, assign that button to `secondCardButton` and flip it (so it faces up). Check if the two buttons match, i.e., have equal cards. If they do, go to state 0. If they don't go to state 2.

- __State 2:__ Two unmatched cards facing up
    - If the user clicks on a card facing up, flip `firstCardButton` and `secondCardButton` (so they both face down), and go to state 0. 
    - If the user clicks on a button facing down, first flip `firstCardButton` and `secondCardButton` (so they both face down), then assign the button the user just clicked on to `firstCardButton` and flip it (so it faces up), and go to state 1.

The logic can be captured in a finite state machine diagram:

           +-+               +-+
       c.u.| |           c.u.| |
	       | v               | v
	     +-----+           +-----+
	     |  0  |   c.d.    |  1  |
	     |     |---------->|     |<---+
	     +-----+           +-----+    |
	       ^ ^                |       |
	       | |                |c.d.   |
	       | |           Y    |       |
	   c.u.| +-------------[match?]   |c.d.
	   t.e.|                  | N     |
	       |    +-----+       |       |           
	       +----|  2  |<------+       |
	            |     |---------------+
	            +-----+
	            
In the above diagram, 'c.u.' stands for __c__lick on a card facing __u__p, whereas 'c.d.' stands for __c__lick on a card facing __d__own. Note that the diagram shows the states, the events and the state transitions, but not the action to be taken associated with each state and event. For the latter, refer to the bullet points above.

After updating the `actionPerformed()` method, rerun the program and verify that the user can no longer cheat.

####Step 4

When the user clicks on two cards that do not match, we don't want to let the user stare at those two cards forever. Instead, we'll give the user limited time to visualize the two cards by automatically flipping them back after one second.  Add a `private Timer autoFlipTimer` (from package `javax.swing`). Add `this` as `ActionListener` and set the timeout to 1000 ms. Prevent the timer from restarting every 1000 ms by calling 

    autoFlipTimer.setRepeats(false);

Start the timer upon _entering_ state 2, and stop the timer upon _exiting_ state 2. In `actionPerformed(ActionEvent e)`, use `e.getSource() == autoFlipTimer` to distinguish timer expiration from button clicks. What should happen on expiration of the `autoFlipTimer`? Note that, in the diagram above, `t.e.` stands for "__t__imer __e__xpiration". Re-run the application after making these updates and verify that the timer works as expected.

__Race conditions__. When in state 2, the user may click on a card facing down _almost_ at the same time as the `autoFlipTimer` expires. Imagine that the user clicks on a card and, before that event is handled, the `autoFlipTimer` expires. This gives rise to two events, which will be handled in sequence. First, the two non-matching cards are flipped so that they face down, the card that the user clicked on is flipped so it's facing up, and the state is set to 1. Then, on handling the timer expiration, the two same cards are flipped again (so they now face up (Oh, horror!!)) and the state is set to 0. Now we are in state 0 with three non-matching cards facing up! The situation that gives rise to this kind of bug is called a race condition (literally a race between the user and the timer in this case) and the bugs that race conditions produce are often extremely hard to detect and often cause applications to crash. Microsoft was once notorious for having such bugs in its operating system resulting in the infamous "blue screen". In our case, the bug may be avoided by checking that the state still equals 2 when handling the timer expiration event. If you did that in your code before reading this paragraph, then, bravo, you are a true programmer! 

####Step 5

Time for the final touches. Have you addressed the case when the user has matched all the cards and reached the end of the game? What would be the most user friendly thing to do in this situation? Close abruptly or offer the user another game? How can you score the performance of the user? Should the score be shown only at the end of a game or while the user is playing. What about sound effects?  
