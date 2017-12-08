package charlie.bot.client;

import charlie.actor.Courier;
import charlie.advisor.BasicStrategy;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Seat;
import charlie.plugin.IAdvisor;
import charlie.plugin.ILogan;
import charlie.util.Play;
import charlie.view.AMoneyManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a client side bot that will simulate a human player.
 *
 * @author D. Blossom, M. Ali, J. Muro
 */
public class MyClientBot implements ILogan {

  protected Logger logger = LoggerFactory.getLogger(MyClientBot.class);
  protected Courier courier;
  protected AMoneyManager moneyManager;

  /**
   * We need an advisor to execute the basic strategy.
   */
  private final IAdvisor advisor = new BasicStrategy();
         
  /**
   * The number of cards in the shoe.
   */
  protected int shoeSize;

  /**
   * A reference to our hand.
   */
  protected HashMap<Hid, Hand> hands = new HashMap<>();
  /**
   * A flag indicating whether it is our turn.
   */
  protected boolean myTurn;

  /**
   * The Hi-Lo running count.
   */
  protected int runningCount = 0;

  /**
   * The true count (runningCount / (shoeSize / CARDS_IN_DECK)).
   */
  protected double trueCount = 0;
  
  /**
   * The number of cards in a standard deck.
   */
  protected static final int DECK_SIZE = 52;

  /**
   * The game start time in milliseconds.
   */
  protected final long startMilliseconds = System.currentTimeMillis();

  /**
   * A counter to track the total time played.
   */
  protected long minutesPlayed;

  /**
   * A reference to the dealer's upcard.
   */
  protected Card upCard;

  /**
   * A counter to track the number of games that resulted in a win.
   */
  protected int wins = 0;

  /**
   * A counter to track the number of games that resulted in a loss.
   */
  protected int losses = 0;

  /**
   * A counter to track the number of games that resulted in blackjack.
   */
  protected int blackjacks = 0;

  /**
   * A counter to track the number of games that resulted in a bust.
   */
  protected int busts = 0;

  /**
   * A counter to track the number of games that resulted in a charlie.
   */
  protected int charlies = 0;

  /**
   * A counter to track the number of games that resulted in a push.
   */
  protected int pushes = 0;

  /**
   * A counter to track the max bet placed.
   */
  protected int maxBet;

  /**
   * A counter to track the average bet amount.
   */
  protected double meanBet;

  /**
   * A counter to track the number of games played.
   */
  protected int gamesPlayed;

  /**
   * A counter to track the total amount bet.
   */
  protected int totalBetAmt;

  /**
   * The minimum bet amount required by the dealer.
   */
  protected static int MIN_BET = 5;

  /**
   * The current bet amount in dollars.
   */
  protected int betAmount = 0;
  
  @Override
  public void go() {
    logger.info("Go received");

    // if True count is < 1 bet min, other wise bet min times true count
    // max(1, (true count + 1)) * MIN_BET
    betAmount = (trueCount < 1) ? MIN_BET : ((int) (trueCount + 1) * MIN_BET);

    // clear the current bet if we are going
    // to change the bet amount
    if (betAmount != moneyManager.getWager()) {
      moneyManager.clearBet();
      //places chips on table in increments of 5, 25, 100
      placeChipsOnTable(betAmount);
    }

    // get current bet
    int currentBet = moneyManager.getWager();

    // Tell dealer our bet and sidebet and receive an HID
    Hid hid = courier.bet(currentBet,0);

    // store it in the hashmap && create a new hand
    this.hands.put(hid, new Hand(hid));
    
    // for debugging
    logger.info("My HID is: " + hid);

    // total amount bet for ALL games
    totalBetAmt += currentBet;

    // is current bet larger than max, if so set current
    // to new max. We want to know about our largest bet
    maxBet = (maxBet < currentBet) ? currentBet : maxBet;
    
    logger.info("Total Bet: " + totalBetAmt);
    logger.info("Max Bet: " + maxBet);
  }
  
  @Override
  public void setCourier(Courier courier) {
    this.courier = courier;
    logger.info("Courier Set to: " + courier);
  }
  
  @Override
  public void setMoneyManager(AMoneyManager moneyManager) {
    this.moneyManager = moneyManager;
    logger.info("AMoneyManager set to: " + moneyManager);
  }
  
  @Override
  public void update() {
  }
  
  @Override
  public void render(Graphics2D g) {

        // lays out the stats on the left side bottom of the
    // ATABLE display, shows, card counting system, 
    // decks left in shoe, running count, true count
    // and other items pertaining to the game
    double s = ((double) shoeSize / (double) 52);
    
    DecimalFormat f = new DecimalFormat("0.00");
    Font ruleFont = new Font("Ariel", Font.PLAIN, 11);
    g.setColor(Color.WHITE);
    g.setFont(ruleFont);
    g.drawString("Card Counting System:", 10, 200);
    g.drawString("HI-LOW", 10, 215);
    g.drawString("Shoe size: " + f.format(s), 10, 230);
    g.drawString("Running Count: " + runningCount, 10, 245);
    g.drawString("True Count: " + f.format(trueCount), 10, 260);
    g.drawString("Games played: " + gamesPlayed, 10, 275);
    g.drawString("Minutes played: " + minutesPlayed, 10, 290);
    g.drawString("Max bet: $" + maxBet, 10, 305);
    g.drawString("Average bet amount: $" + f.format(meanBet), 10, 320);
    g.drawString("BJ | Charlie | Wins | Breaks | Losses | Pushes", 10, 335);
    g.drawString("" + blackjacks + " | " + charlies + " | " + wins + " | "
            + busts + " | " + losses + " | " + pushes, 10, 350);
  }
  
  @Override
  public void startGame(List<Hid> hids, int shoeSize) {

    // We want to know how many minutes we have played
    long nowMilliseconds = System.currentTimeMillis() - startMilliseconds;
    minutesPlayed = TimeUnit.MILLISECONDS.toMinutes(nowMilliseconds);
    logger.info("The game has been running for " + minutesPlayed + " minutes");

    // it is not our turn yet!
    myTurn = false;
    
    // Just incase old dealer upCard is hanging around
    upCard = null;
    
    // increment the game count
    ++gamesPlayed;
    
    logger.info("This is game number: " + gamesPlayed);

    // set our shoeSize
    this.shoeSize = shoeSize;
    logger.info("Shoe size set: " + shoeSize);
  }
  
  @Override
  public void endGame(int shoeSize) {
    
    logger.info("End of game, shoe size: " + shoeSize);
    
    // Update the shoe size.
    this.shoeSize = shoeSize;

    // Update the average bet amount.
    meanBet = ((double)totalBetAmt / (double)gamesPlayed);

    // Here we update the true count.
    double decksInShoe = ((double)shoeSize / DECK_SIZE);
    
    trueCount = ((double) runningCount / decksInShoe);

  }
  
  @Override
  public void deal(Hid hid, Card card, int[] values) {
    
    logger.info("Deal received...");

    /**
     * Update the running count.
     *
     * +1 for 2-6 -1 for 10, J, Q, K, A +0 7-9
     */
    if (card.value() >= 2 && card.value() <= 6) {
      ++runningCount;
    }
    
    if (card.value() == 10 || card.isAce() || card.isFace()) {
      --runningCount;
    }
    logger.info("Updated running count to " + runningCount);

    // Store the dealers upCard for executing the Basic Strategy.
    if (hid.getSeat() == Seat.DEALER && upCard == null) {
      /**
       * We need to synchronize here because we need to ensure that we receive
       * the dealer's upcard before play(Hid hid) is invoked from a separate thread.
       */
      synchronized(advisor) {
        upCard = card;
        // If the thread executing play(Hid hid) is waiting we wake it up
        // so that it can resume execution. It is safe to resume play because
        // the dealer's upcard is no longer null.
        advisor.notifyAll();
      }
      logger.info("Received dealers up card: " + upCard);
    }

    // hit our hand with the incoming card
    if (hid.getSeat() == Seat.YOU) {
        Hand hand = hands.get(hid);
        hand.hit(card);
       // hands.put(hid, hand);
        logger.info("Our hand was hit with a " + card);
    }

    // if it is currently our turn, let us play.
    if (myTurn) {
      play(hid);
    }
  }
  
  @Override
  public void insure() {
    
  }
  
  @Override
  public void bust(Hid hid) {
      myTurn = !myTurn;
    busts++;
    logger.info("Bust received count now " + busts);
  }
  
  @Override
  public void win(Hid hid) {
      myTurn = !myTurn;
    wins++;
    logger.info("Win received count now " + wins);
  }
  
  @Override
  public void blackjack(Hid hid) {
      myTurn = !myTurn;
    blackjacks++;
    logger.info("Backjack received count now " + blackjacks);
  }
  
  @Override
  public void charlie(Hid hid) {
      myTurn = !myTurn;
    charlies++;
    logger.info("Charlie received count now " + charlies);
  }
  
  @Override
  public void lose(Hid hid) {
      myTurn = !myTurn;
    losses++;
    logger.info("Lose recieved count now " + losses);
  }
  
  @Override
  public void push(Hid hid) {
      myTurn = !myTurn;
    pushes++;
    logger.info("Push recieved count now " + pushes);
  }
  
  @Override
  public void shuffling() {
    logger.info("Shuffle message received from dealer.");
    
    // reset card counting variables.
    runningCount = 0;
    trueCount = 0;
  }
  
  @Override
  public void play(Hid hid) {
    logger.info("Play message received from dealer.");

    // Determine if it is our turn.
    if (hid.getSeat() == Seat.YOU) {
      // enable flag indicating that it is our turn
      myTurn = true;

      /**
       * We need to ensure that upcard is not null before passing it to our advisor.
       */
      synchronized (advisor) {
        while (upCard == null) {
          try {
            // Wait for the notification from the deal method.
            advisor.wait();
          } catch (InterruptedException ex) {
            logger.error(null, ex);
          }
        }
      }
      
      Hand hand = hands.get(hid);
      
      // Consult the basic strategy for our next play.
      Play play = advisor.advise(hand, upCard);
      
      logger.info("Play: " + play.toString() + " received!");

      // Make the suggested play.
      makePlay(play, hand);
    } else { // Either our turn is over or it is not our turn.
      // Reset the turn flag if necessary.
      if (myTurn) {
        myTurn = !myTurn;
      }
    }
  }

  /**
   * This method will choose a chip for this auto bot to click It will help
   * bring the real life to the game, and look cleaner than a bunch of $5 chips
   * on the table.
   *
   * @param betAmount the amount we are going to bet
   */
  protected void placeChipsOnTable(int betAmount) {

    // So, we want the remainders as that should
    // tell us how many of each amount we need
    int hundreds = betAmount / 100;
    int twentyfives = (betAmount % 100) / 25;
    int fives = ((betAmount % 100) % 25) / 5;

    // since we need to provide increments of 5, 25, or 100 
    while ((hundreds > 0) || (twentyfives > 0) || (fives > 0)) {
      try {
        // Delay the chip placement animation.
        Thread.sleep(1000);
      } catch (InterruptedException ex) {
        logger.error(null, ex);
      }

      // each round put a bet for that increment if it is not 0.
      if (hundreds > 0) {
        moneyManager.upBet(100);
        hundreds--;
      }
      
      if (twentyfives > 0) {
        moneyManager.upBet(25);
        twentyfives--;
      }
      
      if (fives > 0) {
        moneyManager.upBet(5);
        fives--;
      }
    }
  }

  /**
   * Given a Play it will tell the courier what to do with that play This will
   * ensure we still have control over the basic strategy.
   *
   * @param play the play the basic strategy suggests we make.
   * @param hand
   */
  protected void makePlay(Play play, Hand hand) {

    // quick pause to give the "thinking" effect
    try {
      Thread.sleep(2000);
    } catch (InterruptedException ex) {
      logger.error(null, ex);
    }
    Hid hid = null;
    // Get the current hid
    hid = hand.getHid();
    
    switch (play) {
      
      case SPLIT:
          if(hid.getSplit()){
              splitPlay(hand);
              break;
          }
        courier.split(hid);
        hid.setSplit(true);
        break;
      
      case DOUBLE_DOWN:
        // not first hand cannot double down just hit
        if (hand.size() != 2) {
          courier.hit(hid);
        }
        // first hand double down is allowed
        courier.dubble(hid);
        // set the turn flag off since once we double turns over
        myTurn = !myTurn;
        break;
      
      case HIT:
        courier.hit(hid);
        break;
      
      case STAY:
        courier.stay(hid);
        // our turn is over.
        myTurn = !myTurn;
        break;
    }
  }

  /**
   * What to do in the case of a split since Charlie does not implement split we
   * basically convert to the value and make a move
   *
   * @param hand the hand which is a pair.
   */
  protected void splitPlay(Hand hand) {
      
      Hid hid = hand.getHid();

    //how did we even get in here?
    if (!hand.isPair()) {
      return;
    }

    //pair of 2, 3, 4. Value equivalent is hit
    if (((hand.getValue() == 4)
            || (hand.getValue() == 6)
            || (hand.getValue() == 8))) {
      
      courier.hit(hid);
      return;
    }

    //pair of 5's
    if (hand.getValue() == 10) {
      // dealer has A or 10 value card just hit
      if (upCard.isAce() || upCard.value() == 10) {
        courier.hit(hid);
        return;
      }
      // otherwise double
      courier.dubble(hid);
      return;
    }

    //pair of 6's or A's ( 11 + 1 )
    if (hand.getValue() == 12 || hand.getCard(0).isAce()) {
      
      if (upCard.value() == 2 || upCard.value() == 3) {
        courier.hit(hid);
      }
      if (upCard.value() > 3 && upCard.value() < 7) {
        courier.stay(hid);
      }
      courier.hit(hid);
      return;
    }

    //pair of 7's or 8's
    if (hand.getValue() == 14 || hand.getValue() == 16) {
      if (upCard.value() < 7) {
        courier.stay(hid);
        return;
      }
      courier.hit(hid);
      return;
    }
    //pair of 10's and anything larger than 16
    if (hand.getValue() > 16 && hand.getValue() < 22) {
      courier.stay(hid);
    }
  }

    @Override
    public void split(Hid newHid, Hid origHid) {
        
        // Ensure each HID knows it cannot be split
        newHid.setSplit(true);
        origHid.setSplit(true);
        
        // Get the original hand for updating
        Hand origHand = hands.get(origHid);
        
        // create a new hand from splitting old hand
        Hand newHand = origHand.split(newHid);
        newHand.getHid().setSplit(true);
        
        // Remove the old hand from map
        this.hands.remove(origHid);
        
        // Add the updated hand back into map
        origHand.getHid().setSplit(true); // <-- interesting...
        this.hands.put(origHid, origHand);
        
        // Add the new hand back into map
        this.hands.put(newHid, newHand);
    }
}
