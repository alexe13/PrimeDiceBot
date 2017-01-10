package src;

import org.json.JSONObject;

/**
 * Created by Alex on 08.01.2017.
 */
public class Bet {

    private double amount;
    private double target;
    private double profit;
    private boolean isWin;
    private String condition;
    private double roll;
    private static int rollNumber = 1;

    public static void setRollNumber(int rollNumber) {
        Bet.rollNumber = rollNumber;
    }

    public Bet () {

    }

    public void parseBet(JSONObject obj) {
        this.amount = obj.getDouble("amount");
        this.target = obj.getDouble("target");
        this.profit = obj.getDouble("profit");
        this.isWin = obj.getBoolean("win");
        this.condition = obj.getString("condition");
        this.roll = obj.getDouble("roll");
    }

    public double getAmount() {
        return amount;
    }

    public double getTarget() {
        return target;
    }

    public double getProfit() {
        return (double)((int) Math.round(profit*100))/100;       //round profit to 2 decimals
    }

    public boolean isWin() {
        return isWin;
    }

    public String getCondition() {
        return condition;
    }

    public double getRoll() {
        return roll;
    }

    public int getRollNumber() {
        return rollNumber;
    }

    public String getResult() {
        if (isWin) return "Win";
        else return "Loss";
    }

    public void printRoll() {
        System.out.println("№: " + getRollNumber() + " " + getCondition() + getTarget() + " Amount: " + getAmount() + " Roll: " + getRoll() + " " + getResult() + " Profit: " + getProfit());
    }
}
