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
    
//imports that are used to understand other methods
//remove if not needed, or move to import lise if they are
//just easier to delete 
import java.util.ArrayList;
import java.util.Random;
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
    static int deal_count = 0;
    
    //fields to try and understand these other methods
    //and their purpose.
    private List<Hid> hids;
    private int shoeSize = 0;
    private static int bustCount = 0;
    private static int loseCount = 0;
    private static int blackjackCount = 0;
    private static int winCount = 0;
    private static int pushCount = 0;
    private static int charlieCount = 0;        
    
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
        System.out.println("********** startGame(List<Hid>, int) **********");
        for(int i = 0; i < hids.size(); i++){
            System.out.println("Item @ " + i + ": " + hids.get(i));
        }
        System.out.println("Shoe Size @ start of game: " + this.shoeSize);
    }

    @Override
    public void endGame(int shoeSize) {
        this.shoeSize = shoeSize;
        System.out.println("Shoe size @ end of game: " + this.shoeSize);
        System.out.println("WINS: " + winCount);
        System.out.println("LOSES: " + loseCount);
        System.out.println("BUSTS: " + bustCount);
        System.out.println("BLACKJACKS: " + blackjackCount);
        System.out.println("CHARLIES: " + charlieCount);
        System.out.println("PUSHES: " + pushCount);
    }

    @Override
    public void deal(Hid hid, Card card, int[] values) {
        System.out.println("********** START **********");
        System.out.println("deal(hid, card, int[]) call #: " + (++deal_count));
        if(card != null)
            System.out.println("The card is: " + card.toString());
        System.out.println("The seat for this call: " + hid.getSeat());
        System.out.println("**********  END  **********");

        //we want the dealers card for checking the strategy...
        //this seems like a bit of a hack
        if(Seat.DEALER == hid.getSeat()){
            Hand h = new Hand(hid);
            if(card != null)
                upCard = new Card(card);
        }
        if(hid.getSeat() == this.hid.getSeat()){
            //nothing
        }
    }

    @Override
    public void insure() {
        
    }

    @Override
    public void bust(Hid hid) {
        if(this.hid == hid){
            System.out.println("Busts: " + (++bustCount));
        }
    }
    
    @Override
    public void win(Hid hid) {
        if(this.hid == hid){
            System.out.println("Wins: " + (++winCount));
        }
    }

    @Override
    public void blackjack(Hid hid) {
        if(this.hid == hid){
            System.out.println("Blackjacks: " + (++blackjackCount));
        }
    }

    @Override
    public void charlie(Hid hid) {
        if(this.hid == hid){
            System.out.println("Charlies: " + (++charlieCount));
        }
    }

    @Override
    public void lose(Hid hid) {
        if(this.hid == hid){
            System.out.println("Loses: " + (++loseCount));
        }
    }

    @Override
    public void push(Hid hid) {
        if(this.hid == hid){
            System.out.println("Pushes: " + (++pushCount));
        }
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
                        
            Play suggestion = getPlay(hand, upCard);
            
            if(suggestion == Play.SPLIT){
                System.out.println("SPLIT");
                suggestion = splitAltPlay(hand, upCard);
            }
            
            while(suggestion == Play.HIT || 
                 (suggestion == Play.DOUBLE_DOWN && hand.size() != 2)){
                
                System.out.println("HIT / DOUBLE LOOP");
                dealer.hit(this, this.hid);
                suggestion = getPlay(hand, upCard);
                
                if(hand.getValue() == 21 || hand.isBroke())
                    return;

            }
            
            if(suggestion == Play.DOUBLE_DOWN && hand.size() == 2){
                System.out.println("DOUBLE");
                hid.dubble();
                dealer.doubleDown(this, this.hid);
                return;
            }
            
            if(suggestion == Play.STAY){
                System.out.println("STAY");
                dealer.stay(this, this.hid);
            }
        }
    }
    
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
    
    private Play getPlay(Hand hand, Card upCard){
        BasicStrategy bs = new BasicStrategy();
        return bs.advise(hand, upCard);
    }
}    