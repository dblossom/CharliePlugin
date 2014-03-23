
package charlie.bs.section3;

import charlie.advisor.BasicStrategy;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Seat;
import charlie.plugin.IAdvisor;
import charlie.util.Play;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class Test05_A2_7 {

    IAdvisor advisor = null;

    public Test05_A2_7() {
        advisor = new BasicStrategy();
    }

    @Test
    public void testEleven() {
        // Create a hand valued containing an ace.
        Hand testHand = new Hand(new Hid(Seat.YOU));
        testHand.hit(new Card(Card.ACE, Card.Suit.CLUBS));
        testHand.hit(new Card(7, Card.Suit.CLUBS));

        // What does the advisor suggest?
        Play actual = advisor.advise(testHand, new Card(7, Card.Suit.CLUBS));
        // What _should_ the advisor suggest?
        Play expected = Play.STAY;
        // Verify that the actual value equals the expected value.
        assertEquals("Fail", expected, actual);
    }

    @Test
    public void testTwelve() {
        // Create a hand containing an ace.
        Hand testHand = new Hand(new Hid(Seat.YOU));
        testHand.hit(new Card(Card.ACE, Card.Suit.CLUBS));
        testHand.hit(new Card(7, Card.Suit.CLUBS));

        // What does the advisor suggest?
        Play actual = advisor.advise(testHand, new Card(Card.ACE, Card.Suit.CLUBS));
        // What _should_ the advisor suggest?
        Play expected = Play.HIT;
        // Verify that the actual value equals the expected value.
        assertEquals("Fail", expected, actual);
    }
}
