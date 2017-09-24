package org.alex.config;

import lombok.Data;

/**
 * Created by alex on 24.09.17.
 */
@Data
public class ConfigParameters {

    private String API_KEY;
    private double betAmount = 1;
    private double baseBet;
    private double target;
    private String condition;
    private int seedChangeFrequency;
    private int rollNumber;
    private int profitTarget;
    private double onLooseMultiplier;
    private int preBet;
    private int maxLooseStreak;
    private double targetOnLooseStreak;
    private String conditionOnLooseStreak;

    private static ConfigParameters instance;

    private ConfigParameters() {

    }

    public static ConfigParameters getInstance() {
        if (instance == null) {
            instance = new ConfigParameters();
        }
        return instance;
    }

}
