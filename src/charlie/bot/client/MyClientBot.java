package charlie.bot.client;

import charlie.actor.Courier;
import charlie.advisor.BasicStrategy;
import charlie.bot.server.MyBot;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Seat;
import charlie.plugin.IAdvisor;
import charlie.plugin.IGerty;
import charlie.util.Play;
import charlie.view.AMoneyManager;
import java.awt.Graphics2D;
import java.util.List;
import java.util.logging.Level;
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
    protected Hand myHand;
    protected boolean myTurn;
    

    protected Card upCard;
    
    protected MyBot myBot;
    
    //Keep track of wins, lose, bj, busts, charlie, push set all to zero
    protected static int wins, losses, bjs, busts, charlies, pushes = 0;
    
    @Override
    public void go() {
        LOG.info("In Go");
        
        //for now always add 5 to bet on table
        moneyManager.upBet(MIN_BET);
        
        //let us be honest to the dealer of what we put on the
        //table and call the "getWager()" method
        //just always make $25.00  side bets.
        courier.bet(moneyManager.getWager(), 25);
        
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
        
        myTurn = false;
        upCard = null;
        
        this.shoeSize = shoeSize;
        LOG.info("Shoe size set: " + shoeSize);
        
        for(Hid hid: hids){
            if(Seat.YOU == hid.getSeat()){
                this.myHid = hid;
                this.myHand = new Hand(hid);
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
        
        if(hid.getSeat() == Seat.DEALER && upCard == null){
            upCard = card;
            LOG.info("Dealers Card: " + upCard);
        }
        
        if(hid.getSeat() == Seat.YOU)
            myHand.hit(card);
        
        // If this deal is to us and it is currently our turn...
        if (hid.getSeat() == Seat.YOU && myTurn && !myHand.isBroke() &&
                !myHand.isCharlie() && !myHand.isBlackjack()) {
            
            new Thread(new Middleman(myHand, upCard)).start();
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
        
        bjs++;
        LOG.info("Backjack received count now " + bjs);
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
       LOG.info("Shuffling received");
    }

    @Override
    public void play(Hid hid) {
       LOG.info("Play");

       if (hid.getSeat() == Seat.YOU){
           
           myTurn = true;
           
           new Thread(new Middleman(myHand, upCard)).start();
           
       }
       else{
           if(myTurn)
               myTurn = false;
       }
       
    }
    
    protected void basicStrategyPlay(){
        
        BasicStrategy basicStrategy = new BasicStrategy();
        
        Play play = basicStrategy.advise(myHand, upCard);
        

    }   
    
    class Middleman implements Runnable{
        
        private final Card upCard;
        private final Hand myHand;
        private final IAdvisor advisor = new BasicStrategy();
        
        public Middleman(Hand myHand, Card upCard){
            this.upCard = upCard;
            this.myHand = myHand;
        }

        @Override
        public void run() {
            
            Play advise = advisor.advise(myHand, upCard);
            
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(MyClientBot.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            switch (advise) {
                
                case SPLIT:
                    //let us just stay
                    //going to make a simple
                    //default to simple basic strat
                    courier.stay(myHid);
                    break;
                    
                case DOUBLE_DOWN:
                    //not first hand cannot double down just hit
                    if(myHand.size() != 2){
                        courier.hit(myHid);
                    }
                    //first hand double down is allowed
                    courier.dubble(myHid);
                    myTurn = !myTurn;
                    break;
                    
                case HIT:
                    courier.hit(myHid);
                    break;
                
                case STAY:
                    courier.stay(myHid);
                    myTurn = !myTurn;
                    break;
            
            }
        }
    }
}