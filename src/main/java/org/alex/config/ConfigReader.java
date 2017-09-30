package org.alex.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by alex on 24.09.17.
 */
public class ConfigReader {

    //fetch parameters from config file
    public static ConfigParameters parseConfig(String config) {
        ConfigParameters configParameters = ConfigParameters.getInstance();
        Properties properties = new Properties();
        FileInputStream inputStream;

        try {
            inputStream = new FileInputStream(config);
            properties.load(inputStream);

            configParameters.setAPI_KEY(properties.getProperty("API_KEY").trim());
            configParameters.setBaseBet(Double.parseDouble(properties.getProperty("baseBet").trim()));
            configParameters.setTarget(Double.parseDouble(properties.getProperty("target")));
            configParameters.setCondition(properties.getProperty("condition").trim());
            configParameters.setRollNumber(Integer.parseInt(properties.getProperty("rollNumber").trim()));
            configParameters.setProfitTarget(Integer.parseInt(properties.getProperty("profitTarget").trim()));
            configParameters.setOnLooseMultiplier(Double.parseDouble(properties.getProperty("onLooseMultiplier").trim()));
            configParameters.setPreBet(Integer.parseInt(properties.getProperty("preBet").trim()));
            configParameters.setSeedChangeFrequency(Integer.parseInt(properties.getProperty("seedChangeFrequency").trim()));
            configParameters.setMaxLooseStreak(Integer.parseInt(properties.getProperty("maxLooseStreak").trim()));
            configParameters.setAdjustTargetOnLooseStreak(Integer.parseInt(properties.getProperty("adjustTargetOnLooseStreak").trim()));

        } catch (FileNotFoundException e) {
            System.out.println("Config file not found!");
            System.exit(777);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return configParameters;
    }

}
