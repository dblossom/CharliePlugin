package charlie.advisor;

import charlie.card.Card;
import charlie.card.Hand;
import charlie.plugin.IAdvisor;
import charlie.util.Play;
import java.util.Arrays;
import java.util.HashMap;

/**
 * TODO: Add class description.
 * @author Dan Blossom, Mohamad Ali, Joe Muro
 */
public class BasicStrategyOld implements IAdvisor
{
    private final Play[][] suggestion = new Play[26][10];
    private final HashMap<Integer, Integer> findColumn = new HashMap<>();
    private final HashMap<Integer, Integer> findRow = new HashMap<>();
    private boolean isInit = false;
    
    /**
     * Method returns what a player should do determined
     * by players hand and dealers up card.
     * 
     * Method overrides charlie.plugin.IAvisor.advise(Hand, Card)
     * @param myHand players hand
     * @param upCard dealers up card
     * @return suggested play given players hand and dealers up card.
     */
    @Override
    public Play advise(Hand myHand, Card upCard) 
    {
        if(!isInit)
        {
            buildPlayArray();
            buildColumnMap();
            buildRowMap();
            isInit = true;
        }
        return getPlay(myHand, upCard);
    }
    
    /**
     * Method that will take in a players hand and a dealers upcard
     * and return the suggestion based off the basic strategy card provided
     * by Professor Coleman.
     * 
     * @param playerHand the players hand
     * @param dealerCard the dealers up card
     * @return the suggested play. IE Play.HIT
     */
    private Play getPlay(Hand playerHand, Card dealerCard)
    {   
        int columnLocation;
        //If the dealer is showing J,Q,K value is 10.
        if(dealerCard.isFace())
        {
            columnLocation = 8;
        }
        else
        {
            columnLocation = findColumn.get(dealerCard.value());
        }
        int rowLocation;
        int[] testForSoft = playerHand.getValues();
        //if hand is a pair we need to apply a hash for 50
        if(playerHand.isPair())
        {
            int hashValue = hashForRowMap(playerHand.getCard(0).value(), 50);
            rowLocation = findRow.get(hashValue);
            return suggestion[rowLocation][columnLocation];
        }
        //if hand has some kind of "soft" value, we apply a hash of 1199
        //this should mean we are holding an Ace, any other time we use
        //the 'hard' value as a backup from breaking the hand and should
        //apply that values basic strategy.
        else if(testForSoft[0] != testForSoft[1] && playerHand.size() == 2)
        {
            int hashValue = hashForRowMap(playerHand.getValue(), 1199);
            rowLocation = findRow.get(hashValue);
            return suggestion[rowLocation][columnLocation];
        }
        else
        {
            rowLocation = findRow.get(playerHand.getValue());
            return suggestion[rowLocation][columnLocation];
        }
    }
    private void buildPlayArray()
    {
        //ROW 0
        //17+ is always stay
        Arrays.fill(suggestion[0], Play.STAY);
        
        //ROWS 1 - 5
        //player has value of 5 - 8 always hit
        //suggestion[1-4][0-4] are all stay
        fillLoop(suggestion, 1, 4, 0, 4, Play.STAY);
        //suggestion[1-5][5-9] are hit
        fillLoop(suggestion, 1, 5, 5, 9 ,Play.HIT);
        //suggestion[5][0-1] are hit
        fillLoop(suggestion, 5, 5, 0, 1, Play.HIT);
        //suggestion[5][2-4] are stay
        fillLoop(suggestion, 5, 5, 2, 4, Play.STAY);
        
        //ROW 6
        //player has 11 always double except when dealer has A
        Arrays.fill(suggestion[6], 0, 9, Play.DOUBLE_DOWN);
        //player has 11 and dealer has A
        suggestion[6][9] = Play.HIT;
        
        //ROW 7
        //player has 10 always double except when dealer has 10 or A
        Arrays.fill(suggestion[7], 0, 8, Play.DOUBLE_DOWN);
        //player has 10 and dealer has 10 or A
        Arrays.fill(suggestion[7], 8, 10, Play.HIT);
        
        //ROW 8
        //player has 9 dealer has 2 we hit
        suggestion[8][0] = Play.HIT;
        //player has 9 dealer has 3 - 6 we double
        Arrays.fill(suggestion[8], 1, 5, Play.DOUBLE_DOWN);
        //player has 9 dealer has 7 - A we just hit
        Arrays.fill(suggestion[8], 5, 10, Play.HIT);
        
        //ROW 9
        Arrays.fill(suggestion[9], Play.HIT);
        
        //ROW 10
        //soft 19 and up is always stay (A8 - A10)
        Arrays.fill(suggestion[10], Play.STAY);
        
        //ROWS 11 - 16
        //player has soft 18 (A, 7) and dealer has 2 we stay
        suggestion[11][0] = Play.STAY;
        //player has soft 17 (A, 6) and dealer has 2 we hit
        suggestion[12][0] = Play.HIT;
        //suggestion[11-12][1-4] we double
        fillLoop(suggestion, 11, 12, 1, 4, Play.DOUBLE_DOWN);
        Arrays.fill(suggestion[11], 5, 8, Play.STAY);
        Arrays.fill(suggestion[11], 7, 10, Play.HIT);
        //player has soft 13 - 16 and dealer has 2 or 3 we hit
        fillLoop(suggestion, 13, 16, 0, 1, Play.HIT);
        //player has soft 15 or 16 and dealer has 4 - 6 we double
        fillLoop(suggestion, 13, 14, 2, 4, Play.DOUBLE_DOWN);
        //player has soft 13 or 14 and dealer has 4 we hit
        fillLoop(suggestion, 15, 16, 2, 3, Play.HIT);
        //player has soft 13 or 14 and dealer has 5 or 6 we double
        fillLoop(suggestion, 15, 16, 3, 4, Play.DOUBLE_DOWN);
        //suggestion[12-16][5-9] we hit
        fillLoop(suggestion, 12, 16, 5, 9, Play.HIT);
        
        //ROWS 17 & 18
        //always split A's and 8's
        Arrays.fill(suggestion[17], Play.SPLIT);
        //always stay on 10,10
        Arrays.fill(suggestion[18], Play.STAY);

        //ROWS 19 - 25
        //suggestion[19-21][0-4] we split
        fillLoop(suggestion, 19, 21, 0, 4, Play.SPLIT);
        //suggestion[24-25][0-5] we split
        fillLoop(suggestion, 24, 25, 0, 5, Play.SPLIT);
        //suggestion[20-21][6-9] we hit
        fillLoop(suggestion, 20, 21, 6, 9, Play.HIT);
        //suggestion[23-25][6-9] we hit
        fillLoop(suggestion, 23, 25, 6, 9, Play.HIT);
        //the single guys when dealer has 7
        suggestion[19][5] = Play.STAY;
        suggestion[20][5] = Play.SPLIT;
        suggestion[21][5] = Play.HIT;
        suggestion[23][5] = Play.HIT;
        //player has 9's dealer has 8 or 9 we split
        Arrays.fill(suggestion[19], 6, 8, Play.SPLIT);
        //player has 9's dealer has 10 or A we stay
        Arrays.fill(suggestion[19], 8, 10, Play.STAY);
        //player has 5's we double except when dealer has 10 or A
        Arrays.fill(suggestion[22], 0, 8, Play.DOUBLE_DOWN);
        //player has 5's and dealer has 10 or A we hit
        Arrays.fill(suggestion[22], 8, 10, Play.HIT);
        //player has 4's and dealer has 2, 3, or 4 we hit
        Arrays.fill(suggestion[23], 0, 3, Play.HIT);
        //player has 4's and dealer has 5 or 6 we split
        Arrays.fill(suggestion[23], 3, 5, Play.SPLIT);
    }
    /**
     * A way to fill an array where fillArray does not satisfy my requirements.
     * @param playArray the array (2D)
     * @param rowStart what row or y location to start at (IE: 0 means 0)
     * @param rowEnd what row or y location to stop at at (IE: 1 means it will place value in 1)
     * @param colStart what col or x location to start at (same as row)
     * @param colEnd what col or x location to end at (same as row)
     * @param play what is the play :: IE: Play.STAY
     */
    private void fillLoop(Play[][] playArray, 
                            int rowStart, 
                            int rowEnd,
                            int colStart,
                            int colEnd,
                            Play play)
    {
        while(rowStart < (rowEnd + 1))
        {
            for(int i = colStart; i < (colEnd + 1); i++)
                suggestion[rowStart][i] = play;
            rowStart++;
        }
    }
    
    private void buildColumnMap()
    {
        int cardValue = 2; // lowest card
        for(int i = 0; i < 9; i++)
        {
            findColumn.put(cardValue++, i);
        }
        findColumn.put(Card.ACE, 9);
    }
    /**
     * A simple method to apply a "hash" or make unique keys since
     * A players hand values have multiple meanings depending on what
     * the hand contains ... IE 4, 4 is 8 but is a different play than
     * 5, 3.
     * @param valueToHash players hand value
     * @param hash what to multiple the value by to make a unique key
     * @return the two values multiplied together ...
     */
    private int hashForRowMap(int valueToHash, int hash)
    {
        return (valueToHash * hash);
    }
    /**
     * This method builds the hashmap that will return the 'Y' location
     * where the players hand is located.
     */
    private void buildRowMap()
    {
        //This is ugly...I was thinking 3 maps but this just seems easier...
        //Rather then doing 3 if statements to find the quad where the 
        //players had will fall...
        
        //rows 0 - 9 with 17+ and 5-7 added manually to return index 
        findRow.put(18, 0);
        findRow.put(19,0);
        findRow.put(20,0);
        findRow.put(21,0);
        findRow.put(5,9);
        findRow.put(6,9);
        findRow.put(7,9);
        int cardValue = 17;
        for(int i = 0; i < 10; i++)
        {
            findRow.put(cardValue--, i);
        }
        
        //rows 10 - 16 "soft values", hand's with an ACE.
        //let us give the "soft" vaule a hash of 1199
        //not sure how AK, AQ, and AJ work together yet ....
        findRow.put(hashForRowMap(20,1199), 10);
        findRow.put(hashForRowMap(21,1199), 10);
        cardValue = 19;
        for(int i = 10; i < 17; i++)
        {
            int hashValue = hashForRowMap(cardValue--, 1199);
            findRow.put(hashValue, i);
        }
        
        //Rows 17 - 25 all the pairs ... let's hash value of 50
        findRow.put(hashForRowMap(1, 50), 17);
        findRow.put(hashForRowMap(8, 50), 17);
        cardValue = 10;
        for(int i = 18; i < 25; i++)
        {
            if(cardValue == 8)
                cardValue = 7;
            findRow.put(hashForRowMap(cardValue--, 50), i);
        }
    }
    /**
     * This method is to test the 2D array.
     * @param playArray the array to test
     */
    private void printStratCard(Play[][] playArray)
    {
        for(int row = 0; row < playArray.length; row++)
        {
            if(row < 10)
                System.out.print("0" + row + ": ");
            else
                System.out.print(row + ": ");
            for (Play play : playArray[row]) {
                if(play == Play.SPLIT)
                {
                    System.out.print(" P");
                }
                else
                {
                    char playChar = play.toString().charAt(0);
                    System.out.print(" " + playChar);
                }
            }
            System.out.println();
        }
        System.out.println("Hit = H; Stand = S; Double Down = D; Splt = P");
    }
}