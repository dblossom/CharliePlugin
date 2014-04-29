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
import java.awt.Graphics2D;
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
    
    protected Courier courier;
    protected AMoneyManager moneyManager;
    protected int shoeSize;
    protected int MIN_BET = 5;
    protected Logger LOG = LoggerFactory.getLogger(MyClientBot.class);
    protected Hid myHid;
    protected Hand myHand;
    protected boolean myTurn;
    
    //Keep track of "now" in milliseconds
    protected long startMilliseconds = System.currentTimeMillis();
    
    //Keep track of minutes played
    protected long minutesPlayed;
    
    //Dealers upCard
    protected Card upCard;
    
    //Keep track of wins, lose, bj, busts, charlie, push set all to zero
    protected static int wins, losses, bjs, busts, charlies, pushes = 0;
    
    //Keeps track of max bet played
    protected static int maxBet;
    
    //Keeps track of mean bet amount
    protected static double meanBet;
    
    //Keeps track of number of games played
    protected static int gamesPlayed;
    
    //Keeps track of total amount bet
    protected static int totalBet;
    
    @Override
    public void go() {
        LOG.info("In Go");

        //for now always add 5 to bet on table
        moneyManager.upBet(MIN_BET);
        //get current bet
        int currentBet = moneyManager.getWager();
        //tell dealer our bet and sidebet
        courier.bet(currentBet, 25);
        //total amount bet for ALL games
        totalBet = totalBet + currentBet;
        //is current bet larger than max, if so set current
        //to new max. We want to know about our largest bet
        maxBet = (maxBet < currentBet) ? currentBet : maxBet;
        
        LOG.info("Total Bet: " + totalBet);
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
        
    }

    @Override
    public void startGame(List<Hid> hids, int shoeSize) {
        
        //We want to know how many minutes we have played
        //Do we care about seconds and maybe hours too?
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
        //TODO: remember to divide by 52 to get cards remaining
        this.shoeSize = shoeSize;
        LOG.info("Shoe size set: " + shoeSize);
        
        //get and set our hand and hid from the  list
        //of passed in hids.
        for(Hid hid: hids){
            if(Seat.YOU == hid.getSeat()){
                this.myHid = hid;
                this.myHand = new Hand(hid);
            }
        }
    }

    @Override
    public void endGame(int shoeSize) {
        
        //doing this after the game because go() and startGame()
        //do not seem to wait for each other and we get a div by
        //zero error because gamesPlayed is zero. - could move it to go()?
        meanBet = (double) (totalBet / gamesPlayed);
        
        this.shoeSize = shoeSize;
        LOG.info("End of game, shoe size: " + shoeSize);
    }

    @Override
    public void deal(Hid hid, Card card, int[] values) {
        
        LOG.info("Deal received...");
        
        //set the dealers upCard
        if(hid.getSeat() == Seat.DEALER && upCard == null){
            upCard = card;
            LOG.info("Dealers Card: " + upCard);
        }
        
        //hit our hand with the incoming card
        if(hid.getSeat() == Seat.YOU)
            myHand.hit(card);
        
        // If this deal is to us and it is currently our turn...
        if (hid.getSeat() == Seat.YOU && myTurn && !myHand.isBroke() &&
                !myHand.isCharlie() && !myHand.isBlackjack()) {
            //spawn thread to decide what move to make
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
       LOG.info("Play received...");

       //our turn ... 
       if (hid.getSeat() == Seat.YOU){
           //set flag to indicate our turn
           myTurn = true;
           //spawn thread to make move
           new Thread(new Middleman(myHand, upCard)).start();
       }
       else{
           //turn over or not our turn
           //check if flag is set and
           //if so, set it to false
           if(myTurn)
               myTurn = false;
       }
    }
    
    /**
     * Inner class to be a worker thread spawned for the
     * autobot to make a move
     * 
     * @author D.Blossom, M. Ali, J.Muro
     */
    
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
                    splitPlay(myHand);
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
        
        /**
         * What to do in the case of a split since
         * Charlie does not implement split we basically
         * convert to the value and make a move
         */
        protected void splitPlay(Hand myHand){
            
            //how did we even get in here?
            if(!myHand.isPair())
                return;
        
            //pair of 2, 3, 4. Value equivalent is hit
            if(((myHand.getValue() == 4)
                ||  (myHand.getValue() == 6)
                ||  (myHand.getValue() == 8))){
            
            courier.hit(myHid);
            return;

        }
        
            //pair of 5's
            if(myHand.getValue() == 10){
                //dealer has A or 10 value card just hit
                if(upCard.isAce() ||upCard.value() == 10){
                    courier.hit(myHid);
                    return;
                }
                //otherwise double
                courier.dubble(myHid);
                return;
            }
        
            //pair of 6's or A's ( 11 + 1 )
            if(myHand.getValue() == 12 || myHand.getCard(0).isAce()){
            
                if(upCard.value() == 2 || upCard.value() == 3)
                    courier.hit(myHid);
            
                if(upCard.value() > 3 && upCard.value() < 7)
                    courier.stay(myHid);
            
                courier.hit(myHid);
            
                return;
            }
        
            //pair of 7's or 8's
            if(myHand.getValue() == 14 || myHand.getValue() == 16){
            
                if(upCard.value() < 7){
                    courier.stay(myHid);
                    return;
                }
            
                courier.hit(myHid);
                return;
            }
        
            //pair of 10's and anything larger than 16
            if(myHand.getValue() > 16 && myHand.getValue() < 22){
                courier.stay(myHid);
            }
        }
    }
}
