/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.bot.server;

import charlie.advisor.BasicStrategy;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Dealer;
import charlie.dealer.Seat;
import charlie.plugin.IAdvisor;
import charlie.plugin.IBot;
import charlie.util.Constant;
import charlie.util.Play;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class MyBot implements IBot {

    /**
     * A reference to this Bot's seat.
     */
    private Seat mySeat;

    /**
     * A reference to this Bot's dealer.
     */
    private Dealer myDealer;

    /**
     * A reference to this Bot's hand.
     */
    private Hand myHand;

    /**
     * A reference to the dealer's upCard.
     */
    private Card upCard;

    /**
     * A flag to indicate whether we are waiting for a response from the dealer.
     */
    boolean myTurn = false;

    /**
     *
     */
    final Object monitor = new Object();

    private static final Logger log = Logger.getLogger(MyBot.class.getName());

    @Override
    public Hand getHand() {
        log.log(Level.INFO, "MyBot returned hand to the dealer.");
        return myHand;
    }

    @Override
    public void setDealer(Dealer dealer) {

        myDealer = dealer;
        log.log(Level.INFO, "MyBot has bene assigned dealer {0}", dealer.toString());
    }

    @Override
    public void sit(Seat seat) {
        // The dealer gave us a seat.
        mySeat = seat;
        // Use the seat to create our Hand.
        myHand = new Hand(new Hid(seat));
        // make a min bet
        //TODO: add card counting.
        myHand.getHid().setAmt(Constant.BOT_MIN_BET);
    }

    @Override
    public void startGame(List<Hid> hids, int shoeSize) {
        log.info("Received startGame message from dealer.");
    }

    @Override
    public void endGame(int shoeSize) {
        log.info("Received endGame message from dealer.");
    }

    @Override
    public void deal(Hid hid, Card card, int[] values) {

        // Cache the dealer's upcard.
        // Assumes that the first card addressed for the dealer is the upCard.
        if (hid.getSeat() == Seat.DEALER && upCard == null) {
            upCard = card;
        }
        
        // If this deal is to us and it is currently our turn...
        if (hid.getSeat() == mySeat && myTurn && !myHand.isBroke() &&
                !myHand.isCharlie() && !myHand.isBlackjack()) {
            new Thread(
                    new Middleman(myDealer, this, upCard)).start();
        }
    }

    @Override
    public void insure() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void bust(Hid hid) {
        
    }

    @Override
    public void win(Hid hid) {

    }

    @Override
    public void blackjack(Hid hid) {
        
    }

    @Override
    public void charlie(Hid hid) {
        
    }

    @Override
    public void lose(Hid hid) {

    }

    @Override
    public void push(Hid hid) {

    }

    @Override
    public void shuffling() {
        log.info("Received shuffling message from dealer.");
    }

    @Override
    public void play(Hid hid) {
        
        // Check if it is our turn.
        if (hid.getSeat() == mySeat) {
            myTurn = true;
            // Make the move by spawning a new thread.
            new Thread(
                    new Middleman(myDealer, this, upCard)).start();
        } else {
            // Check if our turn is over.
            if (myTurn) {
                // Indicate that our turn is over.
                myTurn = !myTurn;
            }
        }
    }

    class Middleman implements Runnable {

        /**
         * The Bot this Middleman is making moves on behalf of.
         */
        private final MyBot player;

        /**
         * The dealer for this Middleman's Bot.
         */
        private final Dealer dealer;

        /**
         * A reference to the dealer's upcard
         */
        private final Card upCard;

        /**
         * The Advisor is used to determine which play to make.
         */
        private final IAdvisor advisor = new BasicStrategy();
        
        private static final int WAIT_TIME = 2000;

        public Middleman(Dealer d, MyBot b, Card c) {
            dealer = d;
            player = b;
            upCard = c;
        }

        @Override
        public void run() {

            Play advice = advisor.advise(player.getHand(), upCard);

            try {
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException ex) {
                Logger.getLogger(MyBot.class.getName()).log(Level.SEVERE, null, ex);
            }

            switch (advice) {
                
                case SPLIT:
                    splitAltPlay(player.myHand, upCard);
                    break;
                    
                case DOUBLE_DOWN:
                    //not first hand cannot double down just hit
                    if(player.getHand().size() != 2){
                        dealer.hit(player, player.getHand().getHid());
                        break;
                    }
                    //first hand double down is allowed
                    dealer.doubleDown(player, player.getHand().getHid());
                    break;
                    
                case HIT:
                    dealer.hit(player, player.getHand().getHid());
                    break;
                
                case STAY:
                    dealer.stay(player, player.getHand().getHid());
                    break;
            }
        }
    /**
     * Method returns an alternative to split since it is not implemented
     * basically uses value and forgets it is a pair
     * @param hand the hand to revalue
     * @param upCard dealers card
     * @return the new play
     */
    private void splitAltPlay(Hand hand, Card upCard){
        
        //how did we even get in here?
        if(!hand.isPair())
            return;
        
        //pair of 2, 3, 4. Value equivalent is hit
        if(((hand.getValue() == 4)
        ||  (hand.getValue() == 6)
        ||  (hand.getValue() == 8))){
            
            dealer.hit(player, player.getHand().getHid());
            return;

        }
        
        //pair of 5's
        if(hand.getValue() == 10){
            //dealer has A or 10 value card just hit
            if(upCard.isAce() ||upCard.value() == 10){
                dealer.hit(player, player.getHand().getHid());
                return;
            }
            //otherwise double
            dealer.doubleDown(player, player.getHand().getHid());
            return;
        }
        
        //pair of 6's or A's ( 11 + 1 )
        if(hand.getValue() == 12 || hand.getCard(0).isAce()){
            
            if(upCard.value() == 2 || upCard.value() == 3)
                dealer.hit(player, player.getHand().getHid());
            
            if(upCard.value() > 3 && upCard.value() < 7)
                dealer.stay(player, player.getHand().getHid());
            
            dealer.hit(player, player.getHand().getHid());
            
            return;
        }
        
        //pair of 7's or 8's
        if(hand.getValue() == 14 || hand.getValue() == 16){
            
            if(upCard.value() < 7){
                dealer.stay(player, player.getHand().getHid());
                return;
            }
            
            dealer.hit(player, player.getHand().getHid());
            return;
        }
        
        //pair of 10's and anything larger than 16
        if(hand.getValue() > 16 && hand.getValue() < 22){
            dealer.stay(player, player.getHand().getHid());
        }
        
    }
    }

}

