package org.alex.entity;

import lombok.Data;
import org.alex.config.ConfigParameters;
import org.json.JSONObject;

/**
 * Created by Alex on 02.01.2017.
 */
@Data
public class UserStats {

    private double balance;
    private double wagered;
    private double profit;
    private int bets;
    private int wins;
    private int losses;

    public void parseStats(JSONObject obj) {
        this.balance = obj.getDouble("balance");
        this.wagered = obj.getDouble("wagered");
        this.profit = obj.getDouble("profit");
        this.bets = obj.getInt("bets");
        this.wins = obj.getInt("wins");
        this.losses = obj.getInt("losses");
    }

    public void printStats() {
        System.out.println("************ USER STATS ****************");
        System.out.println("Balance: " + getBalance() + " satoshi");
        System.out.println("Total wagered: " + getWagered() / 100000000 + " btc");
        System.out.println("Total profit: " + getProfit() / 100000000 + " btc");
        System.out.println("Luck: " + (int) ((double) getWins() / getBets() * 100) + " %");
        System.out.println("****************************************");
        if (ConfigParameters.getInstance().getPreBet() !=0) {
            System.out.println("Pre-bet is ON");
        }
    }
}
