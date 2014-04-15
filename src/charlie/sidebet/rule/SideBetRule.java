/*
 Copyright (c) 2014 Ron Coleman

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package charlie.sidebet.rule;

import charlie.card.Card;
import charlie.card.Card.Suit;
import charlie.card.Hand;
import charlie.plugin.ISideBetRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the side bet rule for Super 7.
 *
 * @author Ron Coleman
 */
public class SideBetRule implements ISideBetRule {
    
    private final Logger LOG = LoggerFactory.getLogger(SideBetRule.class);

    private static final Double PAYOFF_SUPER7 = 3.0;
    private static final Double ROYAL_MATCH = 25.0;
    private static final Double EXACTLY_13 = 10.0;

    /**
     * Apply rule to the hand and return the payout if the rule matches and the
     * negative bet if the rule does not match.
     *
     * @param hand Hand to analyze.
     * @return
     */
    @Override
    public double apply(Hand hand) {

        Double bet = hand.getHid().getSideAmt();
        LOG.info("side bet amount = "+bet);
        
        if (bet == 0) {
            return 0.0;
        }
        
        double sidePayoff = getHighestPayout(hand);
        LOG.info("side bet rule applying hand = "+hand);
        
        if (sidePayoff > 0) {
            return sidePayoff * bet;
        }
        
        return -bet;
    }

    /**
     * Determines the highest payout between super7,exactly13, and royal match
     *
     * @param hand
     * @return
     */
    private double getHighestPayout(Hand hand) {

        double sideBet = 0;

        if (isExactly13(hand)) {
            sideBet = EXACTLY_13;
            LOG.info("side bet EXACTLY 13 matches");
        } else if (isExactly13(hand) && isSuper7(hand)) {
            sideBet = EXACTLY_13;
            LOG.info("side bet EXACTLY 13 matches");
        } else if (isSuper7(hand)) {
            sideBet = PAYOFF_SUPER7;
            LOG.info("side bet SUPER 7 matches");
        } else if (isRoyalMatch(hand)) {
            sideBet = ROYAL_MATCH;
            LOG.info("side bet ROYAL MATCH matches");
        } else {
            sideBet = 0;
        }

        return sideBet;
    }

    /**
     * Determines if the hand's first card is a 7.
     *
     * @param hand
     * @return
     */
    private boolean isSuper7(Hand hand) {
        return hand.getCard(0).getRank() == 7;
    }

    /**
     * Determines if the hand is a royal match
     *
     * @param hand
     * @return
     */
    private boolean isRoyalMatch(Hand hand) {

        boolean royal = false;

        Suit s1 = hand.getCard(0).getSuit();
        Suit s2 = hand.getCard(1).getSuit();

        int card_1 = hand.getCard(0).getRank();
        int card_2 = hand.getCard(1).getRank();

        if ((card_1 == Card.KING && card_2 == Card.QUEEN) || (card_2 == Card.KING && card_1 == Card.QUEEN)) {
            if (s1 == s2) {
                royal = true;
            }
        }
        return royal;
    }

    /**
     * Determines if the hand value is 13 (first 2 cards)
     *
     * @param hand
     * @return
     */
    private boolean isExactly13(Hand hand) {
        int card_1 = hand.getCard(0).getRank();
        int card_2 = hand.getCard(1).getRank();
        
        return (card_1 + card_2 == 13);
    }
}