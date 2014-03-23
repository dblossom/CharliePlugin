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
    final static Object monitor = new Object();

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
        
        if (hid.getSeat() == mySeat) {
            myTurn = false;
        }
    }

    @Override
    public void insure() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void bust(Hid hid) {
        if (hid.getSeat() == mySeat) {
            myTurn = false;
        }
    }

    @Override
    public void win(Hid hid) {

    }

    @Override
    public void blackjack(Hid hid) {
        if (hid.getSeat() == mySeat) {
            myTurn = false;
        }
    }

    @Override
    public void charlie(Hid hid) {
        if (hid.getSeat() == mySeat) {
            myTurn = false;
        }
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

        if (hid.getSeat() == mySeat) {
            myTurn = true;

            while (myTurn && !myHand.isBroke()) {
                // Make the move by spawning a new thread.
                new Thread(
                        new Middleman(myDealer, this, upCard)).start();
            }
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
     *
     */
    private final Card upCard;
    
    /**
     * The Advisor is used to determine which play to make.
     */
    private final IAdvisor advisor = new BasicStrategy();

    public Middleman(Dealer d, MyBot b, Card c) {
        dealer = d;
        player = b;
        upCard = c;
    }

    @Override
    public void run() {

        Play advice = advisor.advise(player.getHand(), upCard);
        
        switch (advice) {
            case DOUBLE_DOWN:
            // Not implemented, so fall through to STAY.
            case HIT:
                dealer.hit(player, player.getHand().getHid());
                break;
            case SPLIT:
            // Not implemented, so fall through to STAY.
            case STAY:
                player.myTurn = false;
                dealer.stay(player, player.getHand().getHid());
                break;
        }
    }
}
