/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.bs.section4;

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
 *
 * @author Muroj
 */
public class Test06_22_2 {

    IAdvisor advisor = null;

    public Test06_22_2() {
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
    public void testThirteen() {
        // Create a hand consisting of a pair of 4s.
        Hand testHand = new Hand(new Hid(Seat.YOU));
        testHand.hit(new Card(4, Card.Suit.CLUBS));
        testHand.hit(new Card(4, Card.Suit.CLUBS));

        // What does the advisor suggest?
        Play actual = advisor.advise(testHand, new Card(2, Card.Suit.CLUBS));
        // What _should_ the advisor suggest?
        Play expected = Play.HIT;
        // Verify that the actual value equals the expected value.
        assertEquals("Fail", expected, actual);
    }

    @Test
    public void testFourteen() {
        // Create a hand consisting of a pair of 4s.
        Hand testHand = new Hand(new Hid(Seat.YOU));
        testHand.hit(new Card(4, Card.Suit.CLUBS));
        testHand.hit(new Card(4, Card.Suit.CLUBS));

        // What does the advisor suggest?
        Play actual = advisor.advise(testHand, new Card(6, Card.Suit.CLUBS));
        // What _should_ the advisor suggest?
        Play expected = Play.SPLIT;
        // Verify that the actual value equals the expected value.
        assertEquals("Fail", expected, actual);
    }
}
