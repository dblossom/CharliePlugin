
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

/**
 * @author Joe Muro
 */
public class Test02_5_2 {

    IAdvisor advisor = null;

    public Test02_5_2() {
        advisor = new BasicStrategy();
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testFive() {
        // Create a hand valued at 9.
        Hand testHand = new Hand(new Hid(Seat.YOU));
        testHand.hit(new Card(6, Card.Suit.CLUBS));
        testHand.hit(new Card(3, Card.Suit.CLUBS));

        // What does the advisor suggest?
        Play actual = advisor.advise(testHand, new Card(2, Card.Suit.CLUBS));
        // What _should_ the advisor suggest?
        Play expected = Play.HIT;
        // Verify that the actual value equals the expected value.
        assertEquals("Fail", expected, actual);
    }

    @Test
    public void testSix() {
        // Create a hand valued at 9.
        Hand testHand = new Hand(new Hid(Seat.YOU));
        testHand.hit(new Card(6, Card.Suit.CLUBS));
        testHand.hit(new Card(3, Card.Suit.CLUBS));

        // What does the advisor suggest?
        Play actual = advisor.advise(testHand, new Card(6, Card.Suit.CLUBS));
        // What _should_ the advisor suggest?
        Play expected = Play.DOUBLE_DOWN;
        // Verify that the actual value equals the expected value.
        assertEquals("Fail", expected, actual);
    }
}
