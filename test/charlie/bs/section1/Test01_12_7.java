/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.bs.section1;

import charlie.advisor.BasicStrategy;
import charlie.card.Card;
import charlie.card.Card.Suit;
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
 *
 * @author Muroj
 */
public class Test01_12_7 {

    IAdvisor advisor = null;
    
    public Test01_12_7() {
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

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test
    public void testThree() {
        // Create a hand valued at 12.
        Hand testHand = new Hand(new Hid(Seat.YOU));
        testHand.hit(new Card(Card.KING, Card.Suit.CLUBS));
        testHand.hit(new Card(2, Card.Suit.CLUBS));

        // What does the advisor suggest?
        Play actual = advisor.advise(testHand, new Card(7, Suit.CLUBS));
        // What _should_ the advisor suggest?
        Play expected = Play.HIT;
        // Verify that the actual value equals the expected value.
        assertEquals("Fail", expected, actual);
    }

    @Test
    public void testFour() {
        // Create a hand valued at 17.
        Hand testHand = new Hand(new Hid(Seat.YOU));
        testHand.hit(new Card(Card.KING, Card.Suit.CLUBS));
        testHand.hit(new Card(7, Card.Suit.CLUBS));

        // What does the advisor suggest?
        Play actual = advisor.advise(testHand, new Card(Card.ACE, Suit.SPADES));
        // What _should_ the advisor suggest?
        Play expected = Play.STAY;
        // Verify that the actual value equals the expected value.
        assertEquals("Fail", expected, actual);
    }
}