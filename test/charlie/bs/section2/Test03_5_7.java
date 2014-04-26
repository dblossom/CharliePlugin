
package charlie.bs.section2;

import charlie.advisor.BasicStrategy;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Seat;
import charlie.plugin.IAdvisor;
import charlie.util.Play;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class Test03_5_7 {

    IAdvisor advisor = null;

    public Test03_5_7() {
        advisor = new BasicStrategy();
    }

    @Test
    public void testSeven() {
        // Create a hand valued at 11.
        Hand testHand = new Hand(new Hid(Seat.YOU));
        testHand.hit(new Card(6, Card.Suit.CLUBS));
        testHand.hit(new Card(5, Card.Suit.CLUBS));

        // What does the advisor suggest?
        Play actual = advisor.advise(testHand, new Card(7, Card.Suit.CLUBS));
        // What _should_ the advisor suggest?
        Play expected = Play.DOUBLE_DOWN;
        // Verify that the actual value equals the expected value.
        assertEquals("Fail", expected, actual);
    }

    @Test
    public void testEight() {
        // Create a hand valued at 11.
        Hand testHand = new Hand(new Hid(Seat.YOU));
        testHand.hit(new Card(6, Card.Suit.CLUBS));
        testHand.hit(new Card(5, Card.Suit.CLUBS));

        // What does the advisor suggest?
        Play actual = advisor.advise(testHand, new Card(Card.ACE, Card.Suit.CLUBS));
        // What _should_ the advisor suggest?
        Play expected = Play.HIT;
        // Verify that the actual value equals the expected value.
        assertEquals("Fail", expected, actual);
    }
}
