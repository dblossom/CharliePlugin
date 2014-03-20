package charlie.bot.server;
import charlie.actor.Courier;
import charlie.advisor.BasicStrategy;
import charlie.card.Card;
import charlie.card.Card.Suit;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Dealer;
import charlie.dealer.Seat;
import charlie.message.view.to.Deal;
import charlie.plugin.IBot;
import charlie.util.Play;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author blossom
 */
public class BotBot implements IBot, Runnable{

    private Dealer dealer;
    private Hand hand;
    private Hid hid;
    private List<Hid> hList;
    private int sSize;
    private Card upCard;
    
    
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
        hid.setAmt(10.0);
    }

    @Override
    public void startGame(List<Hid> hids, int shoeSize) {
        this.hList = new ArrayList<>(hids);
        this.sSize = shoeSize;
    }

    @Override
    public void endGame(int shoeSize) {
        sSize = shoeSize;
    }

    @Override
    public void deal(Hid hid, Card card, int[] values) {
        if(Seat.DEALER == hid.getSeat()){
            if(card != null)
                upCard = new Card(card);
            //upCard = new Card(5, Suit.DIAMONDS);
        }
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
        if(this.hid.getSeat() == hid.getSeat()){
            new Thread(this).start();
        }
    }

    @Override
    public void run() {
        synchronized(dealer){
            Responder r = new Responder(hand, upCard, dealer);
            Play bs = r.getPlay();
            while(bs != Play.STAY){
                if(bs == Play.DOUBLE_DOWN && hand.size() == 2)
                    dealer.doubleDown(this, this.hid);
                if(bs == Play.HIT)
                    dealer.hit(this, this.hid);
                if(bs == Play.SPLIT)
                    dealer.hit(this, this.hid);
                if(bs == Play.DOUBLE_DOWN && hand.size() != 2)
                    dealer.hit(this, this.hid);
                if(bs == null)
                    dealer.stay(this, this.hid);
                bs = r.getPlay();
            }
            dealer.stay(this, this.hid);
        }
    }
}    