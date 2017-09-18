package org.alex;

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

import java.io.*;
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
    private static String API_KEY;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";
    private static double profit = 0;
    private static int rollNumber = 1;
    private static int currentLooseStreak = 0;
    private boolean isLooseStreak = false;
    private static String configFile;

    //betting parameters
    private static double betAmount = 1;
    private static double baseBet;
    private static double target;
    public static String condition;
    private static int seedChangeFrequency;
    private static int rollTarget;
    private static int profitTarget;
    private static double onLooseMultiplier;
    private static int preBet;
    private static int maxLooseSreak;

    //class instances
    public static UserStats stats = new UserStats();
    public static PrimeDice dice = new PrimeDice();

    //main method
    public static void main(String[] args) throws Exception {
        dice.login();
        Thread.sleep(1000);
        dice.doMartingale();
    }

    public void login() {
        Scanner in = new Scanner(System.in);
        System.out.println("Please specify path to config.txt file:");
        configFile = in.nextLine();
        parseConfig(configFile);
        try {
            dice.getStats();
        }
        catch (Exception e) {
            System.out.println(e);
        }
        finally {
            in.close();
        }
    }

    //fetch parameters from config file
    public void parseConfig (String config) {
        ArrayList<String> parameters = new ArrayList<>();
        try {
            String line;
            BufferedReader reader = new BufferedReader(new FileReader(config));
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("*") || line.isEmpty()) continue;
                parameters.add(line.trim());
            }

            if (parameters.size() != 10) {
                System.out.println("Error: config.txt is corrupted, please check parameters!");
                System.exit(0);
            }

            API_KEY = parameters.get(0);
            baseBet = Integer.parseInt(parameters.get(1));
            target = Double.parseDouble(parameters.get(2));
            condition = parameters.get(3);
            rollTarget = Integer.parseInt(parameters.get(4));
            profitTarget = Integer.parseInt(parameters.get(5));
            onLooseMultiplier = Double.parseDouble(parameters.get(6));
            preBet = Integer.parseInt(parameters.get(7));
            seedChangeFrequency = Integer.parseInt(parameters.get(8));
            maxLooseSreak = Integer.parseInt(parameters.get(9));
        }
        catch (FileNotFoundException f) {
            System.out.println("File not found.");
            System.exit(0);
        }
        catch (IOException io) {

        }
    }

    //main betting loop
    public void doMartingale() throws Exception {

        Bet currentBet;
        while (rollNumber < rollTarget && profit < profitTarget) {
            try {

                if (preBet > 0) {
                    if (!isLooseStreak) {
                        betAmount = 0;
                    }
                    if (currentLooseStreak >= preBet && !isLooseStreak) {
                        betAmount = baseBet;
                        isLooseStreak = true;
                    }
                }

                if (maxLooseSreak > 0 && currentLooseStreak >= maxLooseSreak ) {
                    betAmount = baseBet;
                }

                if (rollNumber % seedChangeFrequency == 0) {
                    dice.changeSeed();
                }

                currentBet = dice.makeBet();
                currentBet.printRoll();
                rollNumber = currentBet.getRollNumber();
                profit = currentBet.getSessionProfit();

                if (!currentBet.isWin()) {
                    currentLooseStreak++;
                    betAmount *= onLooseMultiplier;
                } else {
                    currentLooseStreak = 0;
                    betAmount = baseBet;
                    isLooseStreak = false;
                }
                Thread.sleep(300);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public void getStats() throws Exception {

        CloseableHttpClient client = HttpClients.createDefault();
        String url = "https://api.primedice.com/api/users/1?api_key=" + API_KEY;
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

    public Bet makeBet() throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost("https://api.primedice.com/api/bet?api_key=" + API_KEY);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("amount", String.valueOf(betAmount)));
        params.add(new BasicNameValuePair("target", String.valueOf(target)));
        params.add(new BasicNameValuePair("condition", condition));
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
            bet.setRollNumber(bet.getRollNumber()+1);
            bet.setSessionProfit(bet.getSessionProfit()+bet.getProfit());

            return bet;

        } finally {
            response.close();
        }
    }

    public void changeSeed() throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost("https://api.primedice.com/api/seed?api_key=" + API_KEY);

        List<NameValuePair> params = new ArrayList<NameValuePair>(1);
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

    public String generateRandomString() {
        return UUID.randomUUID().toString().substring(0, 30);
    }
}
