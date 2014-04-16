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

package charlie.sidebet.view;

import charlie.audio.Effect;
import charlie.audio.SoundFactory;
import charlie.card.Hid;
import charlie.plugin.ISideBetView;
import charlie.view.AMoneyManager;
import charlie.view.sprite.Chip;
import charlie.view.sprite.ChipButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the side bet view
 * @author Ron Coleman, Ph.D.
 */
public class SideBetView implements ISideBetView {
    private final Logger LOG = LoggerFactory.getLogger(SideBetView.class);
    
    public final static int X = 400;
    public final static int Y = 200;
    public final static int DIAMETER = 50;
    
    protected Font font = new Font("Arial", Font.BOLD, 18);
    protected BasicStroke stroke = new BasicStroke(3);
    
    // See http://docs.oracle.com/javase/tutorial/2d/geometry/strokeandfill.html
    protected float dash1[] = {10.0f};
    protected BasicStroke dashed
            = new BasicStroke(3.0f,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER,
                    10.0f, dash1, 0.0f);   

    protected List<ChipButton> buttons;
    protected int amt = 0;
    protected AMoneyManager moneyManager;

    //Array list of chips that are used for side bets
    protected List<Chip> chips = new ArrayList<>();
    
    //the images used for the side bets
    public final static String FIVE = "./images/chip-5-1.png";
    public final static String TWENTYFIVE = "./images/chip-25-1.png";
    public final static String HUNDRED = "./images/chip-100-1.png";
    
    //for the payout info that is placed on table
    protected final String SUPER = "Super 7       pays    3:1";
    protected final String EXACTLY = "Exactly 13    pays  10:1";
    protected final String ROYAL = "Royal Match pays  25:1";
    
    //if game is over or not - start with not
    private boolean gameOver = false;
    
    //the bet if taken + for win - for lose
    double bet = 0.0;
    
    public SideBetView() {
        LOG.info("side bet view constructed");
    }
    
    /**
     * Sets the money manager.
     * @param moneyManager 
     */
    @Override
    public void setMoneyManager(AMoneyManager moneyManager) {
        this.moneyManager = moneyManager;
        this.buttons = moneyManager.getButtons();
    }
    
    /**
     * Registers a click for the side bet.
     * @param x X coordinate
     * @param y Y coordinate
     */
    @Override
    public void click(int x, int y) {
        
        // Test if any chip button has been pressed.
        for(ChipButton button: buttons) {
            if(button.isPressed(x, y)) {
                SoundFactory.play(Effect.CHIPS_IN);
                amt += button.getAmt();
                LOG.info("A. side bet amount "+button.getAmt()+" updated new amt = "+amt);
                
                //This block of code will randomly place a chip on the table
                //just to the right of the side bet circle
                int n = chips.size();
                int xStart = (X-DIAMETER/2) + 60;
                int yStart = (Y-DIAMETER/2);
                Random ran = new Random();
                Image img = getChipImage(button.getAmt());
                int placeX = xStart + n * (img.getWidth(null))/3 + ran.nextInt(10)-10;
                int placeY = yStart + ran.nextInt(5)-5;
                Chip chip = new Chip(img, placeX, placeY, button.getAmt());
                chips.add(chip);
                //block end
            } 
        }
        
        //Tests if sidebet has been cleared
        if(inRange(x, y)) {
            SoundFactory.play(Effect.CHIPS_OUT);
            amt = 0;
            LOG.info("B. side bet amount cleared");
            chips.clear();
        }
    }

    /**
     * Informs view the game is over and it's time to update the bankroll for the hand.
     * @param hid Hand id
     */
    @Override
    public void ending(Hid hid) {
        
        //set gameover flag to true so we can render win or lose label
        gameOver = true;
        
        bet = hid.getSideAmt();
        
        if(bet == 0)
            return;		
        
        LOG.info("side bet outcome = "+bet);
        
        // Update the bankroll
        moneyManager.increase(bet);
 
        LOG.info("new bankroll = "+moneyManager.getBankroll());
    }

    /**
     * Informs view the game is starting
     */
    @Override
    public void starting() {
        //reset the gameover flag to remove label
        gameOver = false;
    }

    /**
     * Gets the side bet amount.
     * @return Bet amount
     */
    @Override
    public Integer getAmt() {
        //the amt being bet by the player
        return amt;
    }

    /**
     * Updates the view
     */
    @Override
    public void update() {
    }

    /**
     * Renders the view
     * @param g Graphics context
     */
    @Override
    public void render(Graphics2D g) {
        // Draw the at-stake place on the table
        g.setColor(Color.RED); 
        g.setStroke(dashed);
        g.drawOval(X-DIAMETER/2, Y-DIAMETER/2, DIAMETER, DIAMETER);
        
        //Sidebet payout label / text
        Font ruleFont = new Font("Ariel", Font.PLAIN, 11);
        g.setColor(Color.YELLOW);
        g.setFont(ruleFont);
        g.drawString(SUPER, X+33, Y-12);
        g.drawString(EXACTLY, X+33, Y);
        g.drawString(ROYAL, X+33, Y+12);

        // Draw the at-stake amount
        FontMetrics fm = g.getFontMetrics(font);
        String text = ""+amt;
        int x = X - fm.charsWidth(text.toCharArray(), 0, text.length()) / 2;
        int y = Y + fm.getHeight() / 4;
        g.setFont(font);
        g.setColor(Color.WHITE);
        g.drawString(""+amt, x, y);
        
        for(Chip chip: chips){
            chip.render(g);
        }
        
        //if game over flag is set we will
        //paint resulting labels if sidebet
        //was made.
        if(gameOver){
            if(bet > 0)
                drawResult("WIN!", g);
            if(bet < 0)
                drawResult("LOSE", g);
        }
    }
    
    private boolean inRange(int x, int y) {
        //gets the x and y start position of sidebet oval
        int xStart = (X-DIAMETER/2);
        int yStart = (Y-DIAMETER/2);
        
        //if we are within the oval return true. Formula from AtStakeSprite
        return (x > xStart && x < xStart+DIAMETER && y > yStart && y < yStart+DIAMETER);
    }

    private void drawResult(String name, Graphics2D g) {

        String s7 = "SUPER 7", rm = "ROYAL MATCH", ex = "EXACTLY 13";

        Font result = new Font("Ariel", Font.BOLD, 18);

        if (name.equals("LOSE!")) {
            //Draw LOSE
            g.setFont(result);
            g.setColor(Color.RED);
            g.fill3DRect(X + 65, Y - 20, 62, 25, true);
            g.setColor(Color.WHITE);
            g.drawString(name, X + 61, Y);
        } else {
            //Draw WIN
            g.setFont(result);
            g.setColor(Color.GREEN);
            g.fill3DRect(X + 58, Y - 20, 60, 25, true);
            g.setColor(Color.BLACK);
            g.drawString(name, X + 60, Y);

            //get the sidebet amount to compare
            Double d = (bet / amt);
            if (d == 3.0) {
                
                //Draw Super 7
                g.setFont(result);
                g.setColor(Color.YELLOW);
                g.fill3DRect(X + 58, Y + 5, 95, 25, true);
                g.setColor(Color.BLACK);
                g.drawString(s7, X + 60, Y + 25);
            }
            if (d == 1.0) {
                
                // Draw Exactly 13
                g.setFont(result);
                g.setColor(Color.YELLOW);
                g.fill3DRect(X + 58, Y + 5, 122, 25, true);
                g.setColor(Color.BLACK);
                g.drawString(ex, X + 60, Y + 25);
            }
            if (d == 25.0) {
                
                //Draw Royal Match
                g.setFont(result);
                g.setColor(Color.YELLOW);
                g.fill3DRect(X + 58, Y + 5, 150, 25, true);
                g.setColor(Color.BLACK);
                g.drawString(rm, X + 60, Y + 25);
            }
        }

    }

    
    private Image getChipImage(int chipAmt){
        
        Image img = null;
        switch (chipAmt){
            
            case 5: 
                img = (new ImageIcon(FIVE).getImage());
                break;
                
            case 25:
                img = (new ImageIcon(TWENTYFIVE).getImage());
                break;
                
            case 100:
                img = (new ImageIcon(HUNDRED).getImage());
                break;
        }
        
        return img;
    }
}
