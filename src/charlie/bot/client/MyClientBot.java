package charlie.bot.client;

import charlie.actor.Courier;
import charlie.advisor.BasicStrategy;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Seat;
import charlie.plugin.IAdvisor;
import charlie.plugin.IGerty;
import charlie.util.Play;
import charlie.view.AMoneyManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a client side bot that will simulate a human player.
 *
 * @author D. Blossom, M. Ali, J. Muro
 */
public class MyClientBot implements IGerty {
    protected Logger LOG = LoggerFactory.getLogger(MyClientBot.class);
    protected Courier courier;
    protected AMoneyManager moneyManager;
    
    /**
     * Shoe size
     */
    protected int shoeSize;

    /**
     * Our HID
     */
    protected Hid myHid;
    
    /**
     * Our hand
     */
    protected Hand myHand;
    
    /**
     * Is our turn
     */
    protected boolean myTurn;

    /**
     * Hi-Lo Running Count
     */
    protected static int RUNNING_COUNT = 0;
    
    /**
     * True count
     */
    protected static double TRUE_COUNT = 0;

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
     * A counter to track track the max bet placed.
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
     * The current bet
     */
    protected int betAmount = 0;

    @Override
    public void go() {
        LOG.info("Go received");

        //if True count is < 1 bet min, other wise bet min times true count
        //max(1, (true count + 1)) * MIN_BET
        betAmount = (TRUE_COUNT < 1) ? MIN_BET : ((int) (TRUE_COUNT + 1) * MIN_BET);

        //clear the current bet if we are going
        //to change the bet amount
        if(betAmount != moneyManager.getWager()){
            moneyManager.clearBet();
            //places chips on table in increments of 5, 25, 100
            placeChipsOnTable(betAmount);
        }
        
        //get current bet
        int currentBet = moneyManager.getWager();
        
        //tell dealer our bet and sidebet and receive an HID
        //use that HID to create our hand.
        this.myHid = courier.bet(currentBet, 0);
        this.myHand = new Hand(myHid);
        LOG.info("My HID is: " + myHid);
        
        //total amount bet for ALL games
        totalBetAmt += currentBet;
        
        //is current bet larger than max, if so set current
        //to new max. We want to know about our largest bet
        maxBet = (maxBet < currentBet) ? currentBet : maxBet;

        LOG.info("Total Bet: " + totalBetAmt);
        LOG.info("Max Bet: " + maxBet);

    }

    @Override
    public void setCourier(Courier courier) {
        this.courier = courier;
        LOG.info("Courier Set to: " + courier);
    }

    @Override
    public void setMoneyManager(AMoneyManager moneyManager) {
        this.moneyManager = moneyManager;
        LOG.info("AMoneyManager set to: " + moneyManager);
    }

    @Override
    public void update() {
    }

    @Override
    public void render(Graphics2D g) {
        
        //lays out the stats on the left side bottom of the
        //ATABLE display, shows, card counting system, 
        //decks left in shoe, running count, true count
        //and other items pertaining to the game
        double s = ((double) shoeSize / (double) 52);
        
        DecimalFormat f = new DecimalFormat("0.00");
        Font ruleFont = new Font("Ariel", Font.PLAIN, 11);
        g.setColor(Color.WHITE);
        g.setFont(ruleFont);
        g.drawString("Card Counting System:", 10, 200);
        g.drawString("HI-LOW", 10, 215);
        g.drawString("Shoe size: " + f.format(s), 10, 230);
        g.drawString("Running Count: " + RUNNING_COUNT, 10, 245);
        g.drawString("True Count: " + f.format(TRUE_COUNT), 10, 260);
        g.drawString("Games played: " + gamesPlayed, 10, 275);
        g.drawString("Minutes played: " + minutesPlayed, 10, 290);
        g.drawString("Max bet amount: " + maxBet, 10, 305);
        g.drawString("Mean bet amount per game: " + meanBet, 10, 320);
        g.drawString("BJ | Charlie | Wins | Breaks | Loses | Pushes", 10, 335);
        g.drawString("" + blackjacks + " | " + charlies + " | " + wins + " | " +
                busts + " | " + losses + " | " + pushes, 10, 350);
    }

    @Override
    public void startGame(List<Hid> hids, int shoeSize) {

        //We want to know how many minutes we have played
        long nowMilliseconds = System.currentTimeMillis() - startMilliseconds;
        minutesPlayed = TimeUnit.MILLISECONDS.toMinutes(nowMilliseconds);
        LOG.info("The game has been running for " + minutesPlayed + " minutes");

        //it is not our turn yet!
        myTurn = false;
        //just incase old dealer upCard is hanging around
        upCard = null;
        //what game are we starting?
        ++gamesPlayed;
        LOG.info("This is game number: " + gamesPlayed);

        //set our shoeSize
        this.shoeSize = shoeSize;
        LOG.info("Shoe size set: " + shoeSize);

    }

    @Override
    public void endGame(int shoeSize) {
        this.shoeSize = shoeSize;
        LOG.info("End of game, shoe size: " + shoeSize);
        
        //set the mean bet amount from total games and
        //the total bet amount
        meanBet = (double) (totalBetAmt / gamesPlayed);
        
        //Here we update the True Count which is the
        // running count / decks in shoe
        double decksInShoe = ((double) this.shoeSize / (double) 52);

        if (RUNNING_COUNT != 0) {
            TRUE_COUNT = ((double) RUNNING_COUNT / decksInShoe);
        }

    }

    @Override
    public void deal(Hid hid, Card card, int[] values) {

        LOG.info("Deal received...");

        //update the running count from a card value
        // +1 for 2 - 6 
        // -1 from 10, J, Q, K, A
        // any other card no change
        if (card.value() >= 2 && card.value() <= 6) {
            ++RUNNING_COUNT;
        }

        if (card.value() == 10 || card.isAce() || card.isFace()) {
            --RUNNING_COUNT;
        }
        LOG.info("Running count is now: " + RUNNING_COUNT);

        //set the dealers upCard for testing Basic Strategy
        if (hid.getSeat() == Seat.DEALER && upCard == null) {
            upCard = card;
            LOG.info("Dealers Card: " + upCard);
        }

        //hit our hand with the incoming card
        if (hid.getSeat() == Seat.YOU) {
            myHand.hit(card);
        }
        
        // if it is currently our turn, let us play.
        if(myTurn){
            play(hid);
        }
    }

    @Override
    public void insure() {

    }

    @Override
    public void bust(Hid hid) {
        busts++;
        LOG.info("Bust received count now " + busts);
    }

    @Override
    public void win(Hid hid) {
        wins++;
        LOG.info("Win received count now " + wins);
    }

    @Override
    public void blackjack(Hid hid) {
        blackjacks++;
        LOG.info("Backjack received count now " + blackjacks);
    }

    @Override
    public void charlie(Hid hid) {
        charlies++;
        LOG.info("Charlie received count now " + charlies);
    }

    @Override
    public void lose(Hid hid) {
        losses++;
        LOG.info("Lose recieved count now " + losses);
    }

    @Override
    public void push(Hid hid) {
        pushes++;
        LOG.info("Push recieved count now " + pushes);
    }

    @Override
    public void shuffling() {
        //reset card counting variables.
        RUNNING_COUNT = 0;
        TRUE_COUNT = 0;
        LOG.info("Shuffling received");
    }

    @Override
    public void play(Hid hid) {
        LOG.info("Play received...");

        //is it our turn
        if (hid.getSeat() == Seat.YOU) {
            //set flag that it is our turn
            myTurn = true;
            //get a suggested play
            IAdvisor advisor = new BasicStrategy();
            Play play = advisor.advise(myHand, upCard);
            //let us make that play
            makePlay(play);
            
        } else {
            //turn over or not our turn
            //check if flag is set and
            //if so, set it to false
            if (myTurn) {
                myTurn = false;
            }
        }
    }
    
    /**
     * This method will choose a chip for this auto bot to click
     * It will help bring the real life to the game, and look cleaner
     * than a bunch of $5 chips on the table.
     * 
     * @param betAmount the amount we are going to bet
     */
    protected void placeChipsOnTable(int betAmount){
        
        //So, we want the remainders as that should
        //tell us how many of each amount we need
        int hundreds = betAmount / 100;
        int twofives = (betAmount % 100) / 25;
        int fives = ((betAmount % 100) % 25) / 5;

        //since we need to provide increments of 5, 25, or 100 
        while ((hundreds > 0) || (twofives > 0) || (fives > 0)) {
            try {
                //slow the chip placement down
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(MyClientBot.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //each round put a bet for that increment if it is not 0.
            if (hundreds > 0) {
                moneyManager.upBet(100);
            }

            if (twofives > 0) {
                moneyManager.upBet(25);
            }

            if (fives > 0) {
                moneyManager.upBet(5);
            }

            //decrement the "remainders"
            hundreds--;
            twofives--;
            fives--;

        }
    }
    
    /**
     * Given a Play it will tell the courier what to do with that play
     * This will ensure we still have control over the basic strategy.
     * 
     * @param play the play the basic strategy suggests we make. 
     */
    protected void makePlay(Play play){
        
        //quick pause to give the "thinking" effect
        try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(MyClientBot.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        switch (play) {
            
            case SPLIT:
                //split is not implemented so we need to do something else
                splitPlay(myHand);
                break;

            case DOUBLE_DOWN:
                //not first hand cannot double down just hit
                if (myHand.size() != 2) {
                    courier.hit(myHid);
                }
                //first hand double down is allowed
                courier.dubble(myHid);
                //set the turn flag off since once we double turns over
                myTurn = !myTurn;
                break;

            case HIT:
                courier.hit(myHid);
                break;

            case STAY:
                courier.stay(myHid);
                //our turn is over.
                myTurn = !myTurn;
                break;
            }
        }
    
    /**
     * What to do in the case of a split since Charlie does not implement
     * split we basically convert to the value and make a move
     * 
     * @param myHand the hand which is a pair.
     */
     protected void splitPlay(Hand myHand) {
         
         //how did we even get in here?
         if (!myHand.isPair()) {
             return;
         }

         //pair of 2, 3, 4. Value equivalent is hit
         if (((myHand.getValue() == 4)
           || (myHand.getValue() == 6)
           || (myHand.getValue() == 8))) {
             
             courier.hit(myHid);
             return;
         }

         //pair of 5's
         if (myHand.getValue() == 10) {
             //dealer has A or 10 value card just hit
             if (upCard.isAce() || upCard.value() == 10) {
                 courier.hit(myHid);
                 return;
             }
             //otherwise double
             courier.dubble(myHid);
             return;
         }

         //pair of 6's or A's ( 11 + 1 )
         if (myHand.getValue() == 12 || myHand.getCard(0).isAce()) {
             
             if (upCard.value() == 2 || upCard.value() == 3) {
                 courier.hit(myHid);
             }
             if (upCard.value() > 3 && upCard.value() < 7) {
                 courier.stay(myHid);
             }
             courier.hit(myHid);
             return;
         }

         //pair of 7's or 8's
         if (myHand.getValue() == 14 || myHand.getValue() == 16) {
             if (upCard.value() < 7) {
                 courier.stay(myHid);
                 return;
             }
             courier.hit(myHid);
             return;
         }
         //pair of 10's and anything larger than 16
         if (myHand.getValue() > 16 && myHand.getValue() < 22) {
             courier.stay(myHid);
         }
     }
}