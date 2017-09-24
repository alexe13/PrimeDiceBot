package org.alex.entity;

import lombok.Data;
import org.alex.config.ConfigParameters;
import org.json.JSONObject;

/**
 * Created by Alex on 08.01.2017.
 */
@Data
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

    public static int getRollNumber() {
        return rollNumber;
    }

    public double getSessionProfit() {
        return (double) ((int) Math.round(sessionProfit * 100)) / 100;
    }

    public void setSessionProfit(double sessionProfit) {
        Bet.sessionProfit = sessionProfit;
    }

    public void parseBet(JSONObject obj) {
        this.amount = obj.getDouble("amount");
        this.target = obj.getDouble("target");
        this.profit = obj.getDouble("profit");
        this.isWin = obj.getBoolean("win");
        this.condition = obj.getString("condition");
        this.roll = obj.getDouble("roll");
    }

    public double getProfit() {
        return (double) ((int) Math.round(profit * 100)) / 100;       //round profit to 2 decimals
    }


    public String getResult() {
        if (isWin) return "Win";
        else return "Loss";
    }

    public void printRoll() {  //formatted output for cleaner representation in the console
        String numberFormat = String.format("%1$-10s", getRollNumber()+"/"+ ConfigParameters.getInstance().getRollNumber());
        String amountFormat = String.format("%1$-9s", getAmount());
        String rollFormat = String.format("%1$-7s", getRoll());
        String resultFormat = String.format("%1$-7s", getResult());
        String profitFormat = String.format("%1$-9s", getProfit());
        String sessionProfitFormat = String.format("%1$-20s", getSessionProfit()+"/"+ConfigParameters.getInstance().getProfitTarget());

        System.out.println("№:" + numberFormat + " "
                + getCondition() + getTarget() +
                "    Amount: " + amountFormat +
                " Roll: " + rollFormat + " "
                + resultFormat + " Profit: "
                + profitFormat + " Session Profit: " + sessionProfitFormat);
    }
}
