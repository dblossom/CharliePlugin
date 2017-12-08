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
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a client side bot that will simulate a human player.
 *
 * @author blossom
 */
public class MySimpleBot implements ILogan {

    protected Logger logger = LoggerFactory.getLogger(MySimpleBot.class);
    protected Courier courier;
    protected AMoneyManager moneyManager;
    private final IAdvisor advisor = new BasicStrategy();
    protected HashMap<Hid, Hand> hands = new HashMap<>();
    protected boolean myTurn;
    protected Card upCard;
    protected static int MIN_BET = 5;
    protected final int betAmount = MIN_BET;

    @Override
    public void go() {
        
        moneyManager.clearBet();
        
        logger.info("Go received");

        moneyManager.upBet(betAmount);

        Hid hid = courier.bet(betAmount, 0);

        this.hands.put(hid, new Hand(hid));

        logger.info("Added hid: " + hid + "/nAdded hand: " + hands.get(hid));
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
    }

    @Override
    public void startGame(List<Hid> hids, int shoeSize) {
        myTurn = false;
        upCard = null;
    }

    @Override
    public void endGame(int shoeSize) {
    }

    @Override
    public void deal(Hid hid, Card card, int[] values) {

        logger.info("Deal received for hid: " + hid);

        // Store the dealers upCard for executing the Basic Strategy.
        if (hid.getSeat() == Seat.DEALER && upCard == null) {
            /**
             * We need to synchronize here because we need to ensure that we
             * receive the dealer's upcard before play(Hid hid) is invoked from
             * a separate thread.
             */
            synchronized (advisor) {
                upCard = card;
                // If the thread executing play(Hid hid) is waiting we wake it up
                // so that it can resume execution. It is safe to resume play because
                // the dealer's upcard is no longer null.
                advisor.notifyAll();
            }
            logger.info("Received dealers up card: " + upCard);
        } else if (hid.getSeat() == Seat.YOU) {
            Hand hand = hands.get(hid);
            hand.hit(card);
            logger.info("Just hit hand: " + hand + " with a " + card);
            logger.info("Hand HID: " + hand.getHid());

            if (myTurn) {
                play(hand.getHid());
            }
        }
    }

    @Override
    public void insure() {
    }

    @Override
    public void bust(Hid hid) {
        myTurn = false;
        logger.info("Bust received for hid: " + hid);
    }

    @Override
    public void win(Hid hid) {
        myTurn = false;
        logger.info("Win received for hid: " + hid);
    }

    @Override
    public void blackjack(Hid hid) {
        myTurn = false;
        logger.info("Blackjack received for hid: " + hid);
    }

    @Override
    public void charlie(Hid hid) {
        myTurn = false;
        logger.info("Charlie received for hid: " + hid);
    }

    @Override
    public void lose(Hid hid) {
        myTurn = false;
        logger.info("Lose received for hid: " + hid);
    }

    @Override
    public void push(Hid hid) {
        myTurn = false;
        logger.info("Push received for hid: " + hid);
    }

    @Override
    public void shuffling() {
        logger.info("Shuffle message received from dealer.");
    }

    @Override
    public void play(Hid hid) {
        logger.info("Play message received from dealer.");

        // Determine if it is our turn.
        if (hid.getSeat() == Seat.YOU) {
            
            // Let us try this little hackity hack
            Hand hand = hands.get(hid);
            if(hand.size() >= 5){
                myTurn = false;
                return; // or do we break?
            }
            
            // enable flag indicating that it is our turn
            myTurn = true;

            /**
             * We need to ensure that upcard is not null before passing it to
             * our advisor.
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
            
            Play play = advisor.advise(hand, upCard);
            logger.info("Play: " + play.toString() + " received!");
            logger.info("About to play hid: " + hid);
            logger.info("Hand's HID: " + hand.getHid());

            makePlay(play, hand);
        } else { // Either our turn is over or it is not our turn.
            logger.info("Is it my turn? " + myTurn);
            logger.info("No longer my turn..." + (myTurn = false));
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
        
        logger.info("makePlay("+play.toString()+", "+hand.toString()+")");

        // quick pause to give the "thinking" effect
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            logger.error(null, ex);
        }

        Hid hid = hand.getHid();
        
        logger.info("Hand's Hid is: " + hid);

        switch (play) {

            case SPLIT:
                if (hid.getSplit()) {
                    logger.info("Split Called in if(hid.getSplit())");
                    splitPlay(hand);
                    break;
                }
                logger.info("In split play but NOT in if(hid.getSplit())");
                courier.split(hid);
                // forcing hid.setSplit(true) // UGH!
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
                myTurn = false;
                break;

            case HIT:
                courier.hit(hid);
                break;

            case STAY:
                courier.stay(hid);
                // our turn is over.
                myTurn = false;
                break;
        }
    }

    /**
     * What to do in the case of a split since Charlie does not implement split
     * we basically convert to the value and make a move
     *
     * @param hand the hand which is a pair.
     */
    protected void splitPlay(Hand hand) {
        
        logger.info("In splitPlay(" + hand.toString() + ")");

        Hid hid = hand.getHid();
        
        logger.info("We have created from hand HID: " + hid);

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
                myTurn = false;
            }
            courier.hit(hid);
            return;
        }

        //pair of 7's or 8's
        if (hand.getValue() == 14 || hand.getValue() == 16) {
            if (upCard.value() < 7) {
                courier.stay(hid);
                myTurn = false;
                return;
            }
            courier.hit(hid);
            return;
        }
        //pair of 10's and anything larger than 16
        if (hand.getValue() > 16 && hand.getValue() < 22) {
            courier.stay(hid);
            myTurn = false;
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
