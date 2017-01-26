package src;

import org.json.JSONObject;

/**
 * Created by Alex on 08.01.2017.
 */
public class Bet {

    private double amount;
    private double target;
    private double profit;
    private static double sessionProfit = 0;
    private boolean isWin;
    private String condition;
    private double roll;
    private static int rollNumber = 0;

    public static void setRollNumber(int rollNumber) {
        Bet.rollNumber = rollNumber;
    }

    public double getSessionProfit() {
        return (double) ((int) Math.round(sessionProfit * 100)) / 100;
    }

    public void setSessionProfit(double sessionProfit) {
        this.sessionProfit = sessionProfit;
    }

    public Bet() {


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
        return (double) ((int) Math.round(profit * 100)) / 100;       //round profit to 2 decimals
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

    public void printRoll() {  //formatted output for cleaner representation in the console
        String numberFormat = String.format("%1$-6s", getRollNumber());
        String amountFormat = String.format("%1$-9s", getAmount());
        String rollFormat = String.format("%1$-7s", getRoll());
        String resultFormat = String.format("%1$-7s", getResult());
        String profitFormat = String.format("%1$-9s", getProfit());
        String sessionProfitFormat = String.format("%1$-10s", getSessionProfit());

        System.out.println("№:" + numberFormat + " "
                + getCondition() + getTarget() +
                "    Amount: " + amountFormat +
                " Roll: " + rollFormat + " "
                + resultFormat + " Profit: "
                + profitFormat + " Session Profit: " + sessionProfitFormat);
    }
}
