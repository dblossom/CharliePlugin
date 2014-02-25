package charlie.advisor;

import charlie.card.Card;
import charlie.card.Hand;
import charlie.plugin.IAdvisor;
import charlie.util.Play;
import java.util.ArrayList;

/**
 * Assignment: Charlie Advisor Plugin.
 * 
* @author Mohammed Ali, Dan Blossom, Joseph Muro
 */
public class BasicStrategy implements IAdvisor {

    private static final int numbers[] = {17, 16, 15, 14, 13, 12, 11, 10, 9};
    ArrayList<ArrayList<Play>> hardValues;

    private static final int[] ace = {21, 20, 19, 18, 17, 16, 15, 14, 13};
    ArrayList<ArrayList<Play>> aceValues;

    private static final int pair[] = {16, 20, 18, 14, 12, 10, 8, 6, 4};
    ArrayList<ArrayList<Play>> pairValues;

    
    public BasicStrategy() {
        // Initilizes the arrays used to implement the basic strategy.
        init();
    }
    
    /**
     * Returns a Play using the basic strategy.
     *     
     * @param myHand the player's hand
     * @param upCard the dealer's upcard
     * @return the appropriate Play according to the basic strategy card.
     */
    @Override
    public Play advise(Hand myHand, Card upCard) {
            
        // -2 on the dealer's upCard will give
        // exact index value in the list.
        int dhand = upCard.value() - 2;
        Play thePlay = null;
        if (myHand.isPair()) {
            // Go to the pair array
            int sum;
            if (containsAce(myHand)) {
                // Possibility of Double Ace's
                sum = get_sum(myHand);
                if (sum == 12 || sum == 2) {
                    // Two possible values of an ace pair
                    thePlay = aceValues.get(0).get(dhand);
                }
            } else {
                // Not an ace so just perform the usual tasks
                sum = get_sum(myHand);
                int index = get_index(pair, sum);
                thePlay = pairValues.get(index).get(dhand);
            }
        } else if (myHand.size() == 2 && containsAce(myHand)) {
            // Go to the ac's array
            int index = get_index(ace, myHand.getValue());
            thePlay = aceValues.get(index).get(dhand);
        } else if (myHand.getValue() >= 9 && myHand.getValue() <= 17) {
            // Go to number array
            int index = get_index(numbers, myHand.getValue());
            thePlay = hardValues.get(index).get(dhand);
        } else if (myHand.getValue() >= 5 && myHand.getValue() <= 8) {
            // Always hit between 5-8 inclusive
            thePlay = Play.HIT;
        } else {
            // Always Stay when 17+
            thePlay = Play.STAY;
        }

        return thePlay;
    }

    /**
     * Instantiates and populates each of the three lookup arrays used to 
     * implement the basic strategy.
     */
    private void init() {
        populateSoftList();
        populateAceList();
        populatePairList();
    }

    /**
     * Determines whether the specified hand contains an ace.
     *     
     * @param myhand the hand to check for an ace
     * @return true if the hand contains an ace, false otherwise.
     */
    private boolean containsAce(Hand myhand) {
        boolean found = false;
        for (int i = 0; i < myhand.size(); i++) {
            if (myhand.getCard(0).isAce()) {
                found = true;
            }
        }
        return found;
    }

    /**
     * Total value of the hand.
     *     
     * @param hand to computer the sum
     * @return the sum of the hand
     */
    private int get_sum(Hand hand) {
        int total = 0;
        for (int i = 0; i < hand.size(); i++) {
            total += hand.getCard(i).value();
        }
        return total;
    }

    /**
     * Builds and populates the pair array. This array corresponds to section 4 
     * of the basic strategy card.
     */
    private void populatePairList() {
        pairValues = new ArrayList<>();
        for (int i = 0; i < pair.length; i++) {
            pairValues.add(new ArrayList<Play>());
        }

        //Row1 (AA,88)
        populate(0, 1, 10, Play.SPLIT, pairValues);
        //Row2 (10,10)
        populate(1, 1, 10, Play.STAY, pairValues);
        //Row3(9,9)
        populate(2, 1, 5, Play.SPLIT, pairValues);
        populate(2, 6, 6, Play.STAY, pairValues);
        populate(2, 7, 8, Play.SPLIT, pairValues);
        populate(2, 9, 10, Play.STAY, pairValues);
        //Row4 (7,7)
        populate(3, 1, 6, Play.SPLIT, pairValues);
        populate(3, 7, 10, Play.HIT, pairValues);
        //Row5 (6,6)
        populate(4, 1, 5, Play.SPLIT, pairValues);
        populate(4, 6, 10, Play.HIT, pairValues);
        //Row6 (5,5)
        populate(5, 1, 8, Play.DOUBLE_DOWN, pairValues);
        populate(5, 9, 10, Play.HIT, pairValues);
        //Row7 (4,4)
        populate(6, 1, 3, Play.HIT, pairValues);
        populate(6, 4, 5, Play.SPLIT, pairValues);
        populate(6, 6, 10, Play.HIT, pairValues);
        //Row8-9 (3,3) (2,2)
        for (int i = 7; i <= 8; i++) {
            populate(i, 1, 6, Play.SPLIT, pairValues);
            populate(i, 7, 10, Play.HIT, pairValues);
        }
    }

    /**
     * Populates the array to be used when the player's hand contains an ace. 
     * This array corresponds to section 3 of the basic strategy card.
     */
    private void populateAceList() {
        aceValues = new ArrayList<>();
        for (int i = 0; i < ace.length; i++) {
            aceValues.add(new ArrayList<Play>());
        }

        //Row 1-3 (A10-A8)
        for (int i = 0; i < 3; i++) {
            populate(i, 1, 10, Play.STAY, aceValues);
        }
        
        //Row 4 (A7)
        populate(3, 1, 1, Play.STAY, aceValues);
        populate(3, 2, 5, Play.DOUBLE_DOWN, aceValues);
        populate(3, 6, 7, Play.STAY, aceValues);
        populate(3, 8, 10, Play.HIT, aceValues);
        //Row 5 (A6)
        populate(4, 1, 1, Play.HIT, aceValues);
        populate(4, 2, 5, Play.DOUBLE_DOWN, aceValues);
        populate(4, 6, 10, Play.HIT, aceValues);
        //Row 6-7 (A5) (A4)
        for (int i = 5; i <= 6; i++) {
            populate(i, 1, 2, Play.HIT, aceValues);
            populate(i, 3, 5, Play.DOUBLE_DOWN, aceValues);
            populate(i, 6, 10, Play.HIT, aceValues);
        }
        //Row 8-9 (A3) (A2)
        for (int i = 7; i <= 8; i++) {
            populate(i, 1, 3, Play.HIT, aceValues);
            populate(i, 4, 5, Play.DOUBLE_DOWN, aceValues);
            populate(i, 6, 10, Play.HIT, aceValues);
        }
    }

    /**
     * Builds and populates the numbers array. This array corresponds to 
     * section 1 of the basic strategy card.
     */
    private void populateSoftList() {
        hardValues = new ArrayList<>();
        for (int i = 0; i < numbers.length; i++) {
            hardValues.add(new ArrayList<Play>());
        }

        //Row1 (17)
        populate(0, 1, 10, Play.STAY, hardValues);
        //Row2 - Row5 (16-13)
        for (int i = 0; i < 4; i++) {
            populate(i + 1, 1, 5, Play.STAY, hardValues);
            populate(i + 1, 6, 10, Play.HIT, hardValues);
        }
        //Row 6 (12)
        populate(5, 1, 2, Play.HIT, hardValues);
        populate(5, 3, 5, Play.STAY, hardValues);
        populate(5, 6, 10, Play.HIT, hardValues);
        //Row 7 (11)
        populate(6, 1, 9, Play.DOUBLE_DOWN, hardValues);
        populate(6, 10, 10, Play.HIT, hardValues);
        //Row 8 (10)
        populate(7, 1, 8, Play.DOUBLE_DOWN, hardValues);
        populate(7, 9, 10, Play.HIT, hardValues);
        //Row 9 (9)
        populate(8, 1, 1, Play.HIT, hardValues);
        populate(8, 2, 5, Play.DOUBLE_DOWN, hardValues);
        populate(8, 6, 10, Play.HIT, hardValues);
    }

    /**
     * Populates the given array with the specified Play value.
     *     
     * @param index the index of the array
     * @param from the start index
     * @param to the end index
     * @param play the fill value
     * @param list the list to be populated.
     */
    private void populate(int index, int from, int to, Play play, 
            ArrayList<ArrayList<Play>> list) {
        for (int i=from; i <= to; i++) {
            list.get(index).add(play);
        }
    }

    /**
     * Index of the specified value in the specified array
     *     
     * @param array the array to find the index in.
     * @param target the index of this target
     * @return the index of the target
     */
    private int get_index(int[] array, int target) {
        int temp = -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) {
                temp = i;
            }
        }

        return temp;
    }
}
