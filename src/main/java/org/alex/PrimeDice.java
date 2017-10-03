package org.alex;

import org.alex.config.ConfigParameters;
import org.alex.config.ConfigReader;
import org.alex.entity.Bet;
import org.alex.entity.UserStats;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;


/**
 * @author Alex
 * Main class that handles API requests and implements betting strategy
 */
public class PrimeDice {

    //basic variables
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";
    private static double profit = 0;
    private static int rollNumber = 1;
    private static int currentLooseStreak = 0;
    private boolean isLooseStreak = false;
    private static String configFile;
    private double betAmount = 0;
    private double currentTarget;

    //class instances
    private static UserStats stats = new UserStats();
    private static PrimeDice dice = new PrimeDice();
    private static ConfigParameters configParameters;

    //main method
    public static void main(String[] args) throws Exception {
        dice.login();
        Thread.sleep(1000);
        dice.doMartingale();
    }

    private void login() {
        Scanner in = new Scanner(System.in);
        System.out.println("Please specify path to config.txt file:");
        configFile = in.nextLine();
        configParameters = ConfigReader.parseConfig(configFile);

        if ((configParameters.getCondition().equals("<") && configParameters.getAdjustTargetOnLooseStreak() < 0)
                || (configParameters.getCondition().equals(">") && configParameters.getAdjustTargetOnLooseStreak() > 0)) {
            System.out.println("WARNING! Your current target adjustment will result in a harder target on loose streaks\r\n" +
                    "This may lead to a rapid loss of money\r\n" +
                    "Are you sure you want to continue? (Y/N)");
            if (!in.nextLine().equalsIgnoreCase("Y")) {
                System.exit(5);
            }
        }

            try {
                dice.getStats();
                betAmount = configParameters.getBetAmount();
                currentTarget = configParameters.getTarget();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                in.close();
        }
    }

    //main betting loop
    private void doMartingale() throws Exception {

        Bet currentBet;
        while (rollNumber < configParameters.getRollNumber() && profit < configParameters.getProfitTarget()) {
            try {

                if (configParameters.getPreBet() > 0) {
                    if (!isLooseStreak) {
                        betAmount = 0;
                    }
                    if (currentLooseStreak >= configParameters.getPreBet() && !isLooseStreak) {
                        betAmount = configParameters.getBaseBet();
                        isLooseStreak = true;
                    }
                }

                if (configParameters.getMaxLooseStreak() > 0 && currentLooseStreak >= configParameters.getMaxLooseStreak()) {
                    if (configParameters.getAdjustTargetOnLooseStreak() != 0) {
                        currentTarget += configParameters.getAdjustTargetOnLooseStreak();
                        if (currentTarget > 98) {
                            currentTarget = 98;
                        }
                        System.out.println("Loose streak of " + currentLooseStreak + " ! Adjusting target: " + configParameters.getAdjustTargetOnLooseStreak() +
                                " New target: " + currentTarget);
                    } else {
                        betAmount = configParameters.getBaseBet();
                    }
                }

                if (rollNumber % configParameters.getSeedChangeFrequency() == 0) {
                    dice.changeSeed();
                }

                currentBet = dice.makeBet();
                currentBet.printRoll();
                rollNumber = Bet.getRollNumber();
                profit = currentBet.getSessionProfit();

                if (!currentBet.isWin()) {
                    currentLooseStreak++;
                    betAmount *= configParameters.getOnLooseMultiplier();
                } else {
                    currentLooseStreak = 0;
                    betAmount = configParameters.getBaseBet();
                    isLooseStreak = false;
                    if (configParameters.getCondition().equals("<") && currentTarget > configParameters.getTarget()) {
                        currentTarget -= configParameters.getAdjustTargetOnLooseStreak();
                    } else if (configParameters.getCondition().equals(">") && currentTarget < configParameters.getTarget()) {
                        currentTarget += configParameters.getAdjustTargetOnLooseStreak();
                    }
                }
                Thread.sleep(300);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getStats() throws Exception {

        CloseableHttpClient client = HttpClients.createDefault();
        String url = "https://api.primedice.com/api/users/1?api_key=" + configParameters.getAPI_KEY();
        HttpGet get = new HttpGet(url);
        get.addHeader("Agent", USER_AGENT);
        CloseableHttpResponse response = client.execute(get);
        try {
            int status = response.getStatusLine().getStatusCode();

            if (status != 200) {
                if (status == 401) {
                    System.out.println("Invalid API key.");
                    System.exit(0);
                }
                throw new Exception("Connection failed: " + status + " " + response.getStatusLine().getReasonPhrase());
            }
            HttpEntity entity = response.getEntity();
            String apiOutput = EntityUtils.toString(entity);

            JSONObject user = new JSONObject(apiOutput).getJSONObject("user");
            stats.parseStats(user);
            stats.printStats();
        } finally {
            response.close();
        }
    }

    private Bet makeBet() throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost("https://api.primedice.com/api/bet?api_key=" + configParameters.getAPI_KEY());

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("amount", String.valueOf(betAmount)));
        params.add(new BasicNameValuePair("target", String.valueOf(currentTarget)));
        params.add(new BasicNameValuePair("condition", configParameters.getCondition()));
        post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        CloseableHttpResponse response = client.execute(post);
        try {
            int status = response.getStatusLine().getStatusCode();
            if (status != 200) {
                if (status == 400) {
                    System.out.printf("Not enough funds or bad betting parameters.");
                    System.exit(0);
                }
                throw new Exception("Connection failed: " + status + " " + response.getStatusLine().getReasonPhrase());
            }

            HttpEntity entity = response.getEntity();
            String apiOutput = EntityUtils.toString(entity);

            JSONObject jsonBet = new JSONObject(apiOutput).getJSONObject("bet");
            Bet bet = new Bet();
            bet.parseBet(jsonBet);
            Bet.setRollNumber(Bet.getRollNumber()+1);
            bet.setSessionProfit(bet.getSessionProfit()+bet.getProfit());

            return bet;

        } finally {
            response.close();
        }
    }

    private void changeSeed() throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost("https://api.primedice.com/api/seed?api_key=" + configParameters.getAPI_KEY());

        List<NameValuePair> params = new ArrayList<>(1);
        params.add(new BasicNameValuePair("seed", generateRandomString()));
        post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        CloseableHttpResponse response = client.execute(post);
        try {
            int status = response.getStatusLine().getStatusCode();
            if (status != 200) {
                throw new Exception("Connection failed: " + status + " " + response.getStatusLine().getReasonPhrase());
            }
            System.out.println("Seed changed!");
        } finally {
            response.close();
        }
    }

    private String generateRandomString() {
        return UUID.randomUUID().toString().substring(0, 30);
    }
}
