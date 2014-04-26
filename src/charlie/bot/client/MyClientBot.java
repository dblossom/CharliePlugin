package charlie.bot.client;

import charlie.actor.Courier;
import charlie.card.Card;
import charlie.card.Hid;
import charlie.dealer.Seat;
import charlie.plugin.IGerty;
import charlie.util.Play;
import charlie.view.AMoneyManager;
import java.awt.Graphics2D;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a client side bot that will simulate a human player.
 * 
 * @author D. Blossom, M. Ali, J. Muro
 */
public class MyClientBot implements IGerty {
    
    protected Courier courier;
    protected AMoneyManager moneyManager;
    protected int shoeSize;
    protected int MIN_BET = 5;
    protected Logger LOG = LoggerFactory.getLogger(MyClientBot.class);
    protected Hid myHid;
    protected boolean myTurn;
    
    @Override
    public void go() {
        LOG.info("In Go");
        
        moneyManager.upBet(MIN_BET);
        
        //umm sidebet works but does
        //not seem to display on table?
        courier.bet(MIN_BET, 5);
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
        
    }

    @Override
    public void startGame(List<Hid> hids, int shoeSize) {
        this.shoeSize = shoeSize;
        LOG.info("Shoe size set: " + shoeSize);
        
        for(Hid hid: hids){
            if(Seat.YOU == hid.getSeat()){
                this.myHid = hid;
            }
        }
    }

    @Override
    public void endGame(int shoeSize) {
        this.shoeSize = shoeSize;
        LOG.info("End of game, shoe size: " + shoeSize);
    }

    @Override
    public void deal(Hid hid, Card card, int[] values) {
        LOG.info("Deal");
    }

    @Override
    public void insure() {
        
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
       
    }

    @Override
    public void play(Hid hid) {
       LOG.info("Play");

       if (hid.getSeat() == myHid.getSeat()){
           courier.stay(hid);
       }
    }
    
}
