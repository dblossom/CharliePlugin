package charlie.bot.server;

import charlie.advisor.BasicStrategy;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Dealer;
import charlie.dealer.Seat;
import charlie.plugin.IBot;
import charlie.util.Play;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements a bot
 * @author D. Blossom, M. Ali, J. Muro
 */
public class BotBot implements IBot, Runnable{
    
    private Dealer dealer;
    private Hand hand;
    private Hid hid;
    private Card upCard;
    private List<Hid> hids;
    private int shoeSize = 0;
    private static int bustCount = 0; 
    private final long THINK_TIME = 2000;
    
    @Override
    public Hand getHand(){
        return hand;
    }

    @Override
    public void setDealer(Dealer dealer) {
        this.dealer = dealer;
    }

    @Override
    public void sit(Seat seat) {
        hand = new Hand(new Hid(seat));
        hid = new Hid(hand.getHid());
        hid.setAmt(5.0);
    }

    @Override
    public void startGame(List<Hid> hids, int shoeSize) {
        this.hids = new ArrayList<>(hids);
        this.shoeSize = shoeSize;
    }

    @Override
    public void endGame(int shoeSize) {
        this.shoeSize = shoeSize;
    }

    @Override
    public void deal(Hid hid, Card card, int[] values) {

        //we want the dealers card for checking the strategy...
        //this seems like a bit of a hack
        if(Seat.DEALER == hid.getSeat()){
            if(card != null)
                upCard = new Card(card);
        }
        
        //this is the bots hand not sure what to do with
        //this stuff yet. Spawn a thread to maybe hit again?
        if(hid.getSeat() == this.hid.getSeat()){
            
        }
    }

    @Override
    public void insure() {
        
    }

    @Override
    public void bust(Hid hid) {
        if(this.hid == hid){
            ++bustCount;
        }
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
        if(this.hid.getSeat() == hid.getSeat()){
            new Thread(this).start();
        }
    }

    @Override
    public void run() {
                
        synchronized(this){
            
            //look like we are thinking
            //do we want to make this random
            //also do we want the entire synch
            //block in the try statement
            try {
                wait(THINK_TIME);
            } catch (InterruptedException ex) {
                Logger.getLogger(BotBot.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //lets get a suggestion off strategy card
            Play suggestion = getPlay(hand, upCard);
            
            //split is not implemented so we need to
            //make an alternative move
            if(suggestion == Play.SPLIT){
                suggestion = splitAltPlay(hand, upCard);
            }
            
            //we might want to hit more than just once
            //also if play is double and not first move
            //we cannot do that, so since double is taking
            //a hit, just return hit.
            while(suggestion == Play.HIT || 
                 (suggestion == Play.DOUBLE_DOWN && hand.size() != 2)){
                
                dealer.hit(this, this.hid);
                
                if(hand.getValue() == 21 || hand.isBroke())
                    break;
                
                try {
                    Thread.sleep(THINK_TIME);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BotBot.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                //get next suggestion
                suggestion = getPlay(hand, upCard);
            }
            
            //double, add to bet and return
            if(suggestion == Play.DOUBLE_DOWN && hand.size() == 2){
                hid.dubble();
                dealer.doubleDown(this, this.hid);
                return;
            }
            
            //just going to stay
            if(suggestion == Play.STAY){
                dealer.stay(this, this.hid);
            }
        }
    }
    
    /**
     * Method returns an alternative to split since it is not implemented
     * basically uses value and forgets it is a pair
     * @param hand the hand to revalue
     * @param upCard dealers card
     * @return the new play
     */
    private Play splitAltPlay(Hand hand, Card upCard){
        
        //how did we even get in here?
        if(!hand.isPair())
            return Play.NONE;
        
        //pair of 2, 3, 4. Value equivalent is hit
        if(((hand.getValue() == 4)
        ||  (hand.getValue() == 6)
        ||  (hand.getValue() == 8))){
            
            return Play.HIT;
        }
        
        //pair of 5's
        if(hand.getValue() == 10){
            //nothing changes
            return getPlay(hand, upCard);
        }
        
        //pair of 6's or A's ( 11 + 1 )
        if(hand.getValue() == 12 || hand.getCard(0).isAce()){
            
            if(upCard.value() == 2 || upCard.value() == 3)
                return Play.HIT;
            if(upCard.value() > 3 && upCard.value() < 7)
                return Play.STAY;
            
            return Play.HIT;
        }
        
        //pair of 7's or 8's
        if(hand.getValue() == 14 || hand.getValue() == 16){
            
            if(upCard.value() < 7){
                return Play.STAY;
            }
            
            return Play.HIT;
        }
        
        //pair of 10's and anything larger than 16
        if(hand.getValue() > 16 && hand.getValue() < 22){
            return Play.STAY;
        }
        
        //should never be called
        return Play.NONE;
        
    }
    
    /**
     * Returns a basic strategy play
     * @param hand hand to evaluate
     * @param upCard dealers up card
     * @return suggested play
     */
    private Play getPlay(Hand hand, Card upCard){
        BasicStrategy bs = new BasicStrategy();
        return bs.advise(hand, upCard);
    }
}    