/**
 * A class to play the basic strategy
 */

package charlie.advisor;

import charlie.card.Card;
import charlie.card.Hand;
import charlie.plugin.IAdvisor;
import charlie.util.Play;

/**
 * 
 * @author Muro
 */
public class BasicStrategy implements IAdvisor {
    
    private static final Play H = Play.HIT;
    private static final Play S = Play.STAY;
    private static final Play D = Play.DOUBLE_DOWN;
    private static final Play SP = Play.SPLIT;
    
    private static final Play[][] PAIR_STRATEGY = {
        {SP, SP, SP, SP, SP, SP, H, H, H, H}, // 2,2
        {SP, SP, SP, SP, SP, SP, H, H, H, H}, // 3,3
        {H, H, H, SP, SP, SP, H, H, H, H}, // 4,4
        {D, D, D, D, D, D, D, D, H , H}, // 5,5
        {SP, SP, SP, SP, SP, H, H, H, H, H}, // 6,6
        {SP, SP, SP, SP, SP, SP, H, H, H, H}, // 7,7
        {SP, SP, SP, SP, SP, S, SP, SP, S, S}, // 9,9
        {S, S, S, S, S, S, S, S, S, S}, // 10, 10
        {SP, SP, SP, SP, SP, SP, SP, SP, SP, SP} // A,A 8,8
    };
    
    private static final Play[][] ACE_STRATEGY = {
        {H, H, H, D, D, H, H, H, H, H}, // A,2
        {H, H, H, D, D, H, H, H, H, H}, // A,3
        {H, H, D, D, D, H, H, H, H, H}, // A,4
        {H, H, D, D, D, H, H, H, H, H}, // A,5
        {H, D, D, D, D, H, H, H, H, H}, // A,6
        {S, D, D, D, D, S, S, H, H, H}, // A,7
        {S, S, S, S, S, S, S, S, S, S} // A, 8-10
    };
    
    private static final Play[][] HARD_STRATEGY = {
        {H, H, H, H, H, H, H, H, H, H}, // 5-8
        {H, D, D, D, D, H, H, H, H, H}, // 9
        {D, D, D, D, D, D, D, D, H, H}, // 10
        {D, D, D, D, D, D, D, D, D, H}, // 11
        {H, H, S, S, S, H, H, H, H, H}, // 12
        {S, S, S, S, S, H, H, H, H, H}, // 13
        {S, S, S, S, S, H, H, H, H, H}, // 14
        {S, S, S, S, S, H, H, H, H, H}, // 15
        {S, S, S, S, S, H, H, H, H, H}, // 16
        {S, S, S, S, S, S, S, S, S, S}, // 17+
    };

    @Override
    public Play advise(Hand myHand, Card upCard) {
        
        Play lookupResult = null;
        
        // The column index is the same for all sections of the basic strategy.
        int columnIndex = upCard.isAce() ? 9 : upCard.value() - 2;
        
        if (myHand.isPair()) {
            lookupResult = pairTableLookup(myHand, columnIndex);
        } else if (myHand.size() == 2 && hasAce(myHand)) {
            lookupResult = aceTableLookup(myHand, columnIndex);
        } else {
            lookupResult = hardTableLookup(myHand, columnIndex);
        }
        
        return lookupResult;
    }

/** 
 * Determines whether the specified hand contains an Ace.
 *
 * @param aHand the hand to be checked
 * @return true if the hand contains an ace, false otherwise.
*/
    private static boolean hasAce(Hand aHand) {
        for (int i = 0; i < aHand.size(); i += 1) {
            if (aHand.getCard(i).isAce()) {
                return true;
            }
        }
        return false;
    }

    private static Play pairTableLookup(Hand myHand, int column) {
        
        // The rank indexes the row of the basic strategy table.
        int rank = myHand.getCard(0).value();
        
        if (rank >= 2 && rank <= 7) { // A pair of twos up to a pair of sevens.
            return PAIR_STRATEGY[rank - 2][column];
        } else if (rank == 9 || rank == 10) { // A pair of nines or tens.
            return PAIR_STRATEGY[rank - 3][column];
        } else { // A pair of aces or eights.
            return PAIR_STRATEGY[8][column];
        }
    }

    private Play aceTableLookup(Hand myHand, int column) {
        
        // Determine the value of the non-ace card.
        // We use this value to index the row of the basic strategy table.
        int rank = myHand.getCard(0).isAce() ? // if true
                myHand.getCard(1).value() : // return this value
                myHand.getCard(0).value(); // else return this value
        
        if (rank >= 2 && rank <= 7) { // Ace paired with a two up to a seven.
            return ACE_STRATEGY[rank - 2][column];
        } else { // Ace paired with an eight, nine, or ten.
            return ACE_STRATEGY[6][column];
        }
    }

    private Play hardTableLookup(Hand myHand, int column) {
        
        // The hand value indexes the row of the basic strategy table.
        int handValue = myHand.getValue();
        
        if (handValue >= 5 && handValue <= 8) { // A value of five up to eight.
            return HARD_STRATEGY[0][column];
        } else if (handValue >= 9 && handValue <= 16) { // A value of nine up to sixteen.
            return HARD_STRATEGY[handValue - 8][column];
        } else {
            return HARD_STRATEGY[9][column];
        }
    }
}