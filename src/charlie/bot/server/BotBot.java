package charlie.bot.server;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Dealer;
import charlie.dealer.Seat;
import charlie.message.view.to.Deal;
import charlie.plugin.IBot;
import charlie.util.Play;
import java.util.List;


/**
 *
 * @author blossom
 */
public class BotBot implements IBot{

    private Dealer dealer;
    private Seat seat;
    private Hid hid;
    private List<Hid> hids;
    private Card card;
    private int shoeSize;
    private int[] values;
    private Hand hand;
    private Deal deal;
    
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
    }

    @Override
    public void startGame(List<Hid> hids, int shoeSize) {
        this.hids = hids;
        this.shoeSize = shoeSize;
    }

    @Override
    public void endGame(int shoeSize) {
        this.shoeSize = shoeSize;
    }

    @Override
    public void deal(Hid hid, Card card, int[] values) {
        this.deal = new Deal(hid, values, card);

    }

    @Override
    public void insure() {
        
    }

    @Override
    public void bust(Hid hid) {
        this.hid = hid;
    }
    
    @Override
    public void win(Hid hid) {
        this.hid = hid;
    }

    @Override
    public void blackjack(Hid hid) {
        this.hid = hid;
    }

    @Override
    public void charlie(Hid hid) {
        this.hid = hid;
    }

    @Override
    public void lose(Hid hid) {
        this.hid = hid;
    }

    @Override
    public void push(Hid hid) {
        this.hid = hid;
    }

    @Override
    public void shuffling() {
        
    }

    @Override
    public void play(Hid hid) {
        //dealer.hit(this, hid);
        if(this.seat == hid.getSeat()){
            deal.getCard();
        }
    }
    
}
