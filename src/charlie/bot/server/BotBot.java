package charlie.bot.server;

import charlie.actor.Courier;
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
import charlie.message.view.to.Bust;

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
        synchronized(dealer){
            Play bs = getPlay(hand, upCard);
            while(hand.getValue() < 21 && bs != Play.STAY){
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(BotBot.class.getName()).log(Level.SEVERE, null, ex);
//                }
                if(bs == Play.DOUBLE_DOWN && hand.size() == 2){
                    hid.dubble();
                    dealer.doubleDown(this, this.hid);
                    System.out.println("BET AMT: " + hid.getAmt());
                }
                if(bs == Play.HIT)
                    dealer.hit(this, this.hid);
                if(bs == Play.SPLIT){
                    //split not implemented
                    //might have to refactor Basic Strat
                    //just stay I guess for now ... ? 
                    //I mean 9,9 is 18 -> do not want to split that
                    //however 4,4 is 8 -> prob want to hit that ...
                    dealer.stay(this, this.hid);
                }
                if(bs == Play.DOUBLE_DOWN && hand.size() != 2)
                    dealer.hit(this, this.hid);
                if(bs == Play.STAY)
                    dealer.stay(this, this.hid);
                if(bs == null)
                    dealer.stay(this, this.hid); 
                if(hand.getValue() < 22)
                    bs = getPlay(hand, upCard);
            }
            //I think this stay is causing "invalid stay" errors
            //My guess is because we already "stayed, but now calling it again?"
            //So, how else do we notify the dealer we are done?
            //I think we need to implement the Request methods (Bust, Win, etc)
            //However, those classes do not seem to be useful ...
            System.out.println("Just before 'Stay(IPlayer, Hid)' after loop...");
            dealer.stay(this,this.hid);
        }
    }
    private Play getPlay(Hand hand, Card upCard){
        BasicStrategy bs = new BasicStrategy();
        return bs.advise(hand, upCard);
    }
}    