
package charlie.bs.section1;

import charlie.card.Card;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import charlie.advisor.BasicStrategy;
import charlie.card.Card.Suit;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Seat;
import charlie.plugin.IAdvisor;
import charlie.util.Play;
import static org.junit.Assert.assertEquals;

/**
 * @author Joe
 */
public class Test00_12_2 {
    
    IAdvisor advisor = null;
    
    public Test00_12_2() {
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
    public void testOne() {
       // Create a hand valued at 12.
       Hand testHand = new Hand(new Hid(Seat.YOU));
       testHand.hit(new Card(Card.KING, Suit.CLUBS));
       testHand.hit(new Card(2, Suit.CLUBS));
       
       // What does the advisor suggest?
       Play actual = advisor.advise(testHand, new Card(2, Suit.CLUBS));
       // What _should_ the advisor suggest?
       Play expected = Play.HIT;
       // Verify that the actual value equals the expected value.
       assertEquals("Fail", expected, actual);
    }
    
    @Test
    public void testTwo() {
       // Create a hand valued at 12.
       Hand testHand = new Hand(new Hid(Seat.YOU));
       testHand.hit(new Card(Card.KING, Suit.CLUBS));
       testHand.hit(new Card(2, Suit.CLUBS));
       
       // What does the advisor suggest?
       Play actual = advisor.advise(testHand, new Card(6, Suit.CLUBS));
       // What _should_ the advisor suggest?
       Play expected = Play.STAY;
       // Verify that the actual value equals the expected value.
       assertEquals("Fail", expected, actual);
    }
}
