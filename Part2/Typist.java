/**
 * Represents a typist in a typing race simulation.
 * Each typist has a username, unique identifying symbol, accuracy rating which affects progress and burnout risk,
 * progress tracker, burnout tracker and burnoutTurns tracker.
 * Typists can slide forward and backwards, as well as enter burnout which freezes them for a number of turns.
 *
 * @author Ananthan Sivakumaran
 * @version 1.2
 */

import java.util.ArrayList;
import java.util.List;

public class Typist
{
    // Fields of class Typist

    private String typistName;
    private char typistSymbol;
    private double typistAccuracy;
    private int typistProgress = 0;
    private boolean burntOut = false;
    private int burnoutTurns = 0;

    private boolean justMistyped = false;

    private String typingStyle;
    private String keyboardType;
    private String accessory;
    private String color;

    private String sponsor = "None";
    private double sponsorBonus = 0.0;
    private boolean sponsorConditionMet = false;

    private int burnoutCount = 0;
    private int totalKeystrokes = 0;
    private int correctKeystrokes = 0;
    private double startingAccuracy;

    private int cumulativePoints = 0;
    private double earnings = 0.0;
    private double bestWPM = 0.0;
    private List<Double> wpmHistory = new ArrayList<>();
    private List<Double> accuracyHistory = new ArrayList<>();

    // Constructor of class Typist
    /**
     * Constructor for objects of class Typist.
     * Creates a new typist with a given symbol, name, and accuracy rating.
     *
     * @param typistSymbol  a single Unicode character representing this typist (e.g. '①', '②', '③')
     * @param typistName    the name of the typist (e.g. "TURBOFINGERS")
     * @param typistAccuracy the typist's accuracy rating, between 0.0 and 1.0
     */


    public Typist(char typistSymbol, String typistName, double typistAccuracy)
    {
        this.setSymbol(typistSymbol);
        this.typistName = typistName;
        this.setAccuracy(typistAccuracy);
        this.startingAccuracy = typistAccuracy;
    }


    // Methods of class Typist
    /**
     * Sets this typist into a burnout state for a given number of turns.
     * A burnt-out typist cannot type until their burnout has worn off.
     *
     * @param turns the number of turns the burnout will last
     */
    public void burnOut(int turns)
    {
        this.burnoutTurns = turns;
        this.burntOut = true;
        this.burnoutCount++;
    }

    /**
     * Reduces the remaining burnout counter by one turn.
     * When the counter reaches zero, the typist recovers automatically.
     * Has no effect if the typist is not currently burnt out.
     */
    public void recoverFromBurnout()
    {
        if(this.isBurntOut()) {
            this.burnoutTurns -= 1;
        }
        
        if(this.getBurnoutTurnsRemaining()==0) {
            this.burntOut = false;
        }
    }

    /**
     * Returns the typist's accuracy rating.
     *
     * @return accuracy as a double between 0.0 and 1.0
     */
    public double getAccuracy()
    {
        return this.typistAccuracy; 
    }

    /**
     * Returns the typist's current progress through the passage.
     * Progress is measured in characters typed correctly so far.
     * Note: this value can decrease if the typist mistypes.
     *
     * @return progress as a non-negative integer
     */
    public int getProgress()
    {
        return this.typistProgress; 
    }

    /**
     * Returns the name of the typist.
     *
     * @return the typist's name as a String
     */
    public String getName()
    {
        return this.typistName; 
    }

    /**
     * Returns the character symbol used to represent this typist.
     *
     * @return the typist's symbol as a char
     */
    public char getSymbol()
    {
        return this.typistSymbol; 
    }

    /**
     * Returns the number of turns of burnout remaining.
     * Returns 0 if the typist is not currently burnt out.
     *
     * @return burnout turns remaining as a non-negative integer
     */
    public int getBurnoutTurnsRemaining()
    {
        return burnoutTurns; 
    }

    /**
     * Resets the typist to their initial state, ready for a new race.
     * Progress returns to zero, burnout is cleared entirely.
     */
    public void resetToStart()
    {
        typistProgress = 0;
        burnoutTurns = 0;
        burntOut = false;
        justMistyped = false;
        burnoutCount = 0;
        totalKeystrokes = 0;
        correctKeystrokes = 0;
        startingAccuracy = typistAccuracy;
        sponsorConditionMet = false;
    }

    /**
     * Returns true if this typist is currently burnt out, false otherwise.
     *
     * @return true if burnt out
     */
    public boolean isBurntOut()
    {
        return this.burntOut; 
    }

    /**
     * Advances the typist forward by one character along the passage.
     * Should only be called when the typist is not burnt out.
     */
    public void typeCharacter()
    {
        this.typistProgress += 1;
        this.totalKeystrokes++;
        this.correctKeystrokes++;
    }

    /**
     * Moves the typist backwards by a given number of characters (a mistype).
     * Progress cannot go below zero — the typist cannot slide off the start.
     *
     * @param amount the number of characters to slide back (must be positive)
     */
    public void slideBack(int amount)
    {
        this.typistProgress -= amount;

        if(this.typistProgress < 0) {
            this.typistProgress = 0;
        }

        this.totalKeystrokes++;
    }

    /**
     * Sets the accuracy rating of the typist.
     * Values below 0.0 should be set to 0.0; values above 1.0 should be set to 1.0.
     *
     * @param newAccuracy the new accuracy rating
     */
    public void setAccuracy(double newAccuracy)
    {
        this.typistAccuracy = newAccuracy;

        if(this.typistAccuracy < 0) {
            this.typistAccuracy = 0.0;
        }
        else if(this.typistAccuracy > 1) {
            this.typistAccuracy = 1.0;
        }
    }

    /**
     * Sets the symbol used to represent this typist.
     *
     * @param newSymbol the new symbol character
     */
    public void setSymbol(char newSymbol)
    {
        this.typistSymbol = newSymbol; 
    }

    //Gets justMistyped field
    public boolean justMistyped() 
    { 
        return this.justMistyped; 
    }

    //Sets justMistyped field
    public void setJustMistyped(boolean justMistyped) 
    { 
        this.justMistyped = justMistyped; 
    }

    public int getBurnoutCount() {
        return this.burnoutCount;
    }
    
    public int getTotalKeystrokes() {
        return this.totalKeystrokes;
    }

    public int getCorrectKeystrokes() {
        return this.correctKeystrokes;
    }

    public double getStartingAccuracy() {
        return this.startingAccuracy;
    }
    
    public double getAccuracyPercentage() {
        if (totalKeystrokes == 0) return 100.0;
        return (double) correctKeystrokes / totalKeystrokes * 100.0;
    }

    public double getBestWPM() {
        return this.bestWPM;
    }

    public int getCumulativePoints() {
        return this.cumulativePoints;
    }

    public double getEarnings() {
        return this.earnings;
    }

    public List<Double> getWpmHistory() {
        return this.wpmHistory;
    }

    public List<Double> getAccuracyHistory() {
        return this.accuracyHistory;
    }

    public void updateBestWPM(double wpm)
    {
        if (wpm > bestWPM) bestWPM = wpm;
    }

    public void addPoints(int points) { this.cumulativePoints += points; }
    public void addEarnings(double amount) { this.earnings += amount; }

    public void addRaceHistory(double wpm, double accuracy)
    {
        wpmHistory.add(wpm);
        accuracyHistory.add(accuracy);
    }

    public String getTypingStyle() { return typingStyle; }
    public String getKeyboardType() { return keyboardType; }
    public String getAccessory() { return accessory; }
    public String getColor() { return color; }

    public void setTypingStyle(String style) { this.typingStyle = style; }
    public void setKeyboardType(String keyboard) { this.keyboardType = keyboard; }
    public void setAccessory(String accessory) { this.accessory = accessory; }
    public void setColor(String color) { this.color = color; }

    public String getSponsor() { return sponsor; }
    public double getSponsorBonus() { return sponsorBonus; }
    public boolean isSponsorConditionMet() { return sponsorConditionMet; }

    public void setSponsor(String sponsor, double bonus)
    {
        this.sponsor = sponsor;
        this.sponsorBonus = bonus;
    }

    public void setSponsorConditionMet(boolean met) { this.sponsorConditionMet = met; }
}
