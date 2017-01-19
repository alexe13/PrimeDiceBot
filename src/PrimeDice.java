package src;

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
 * Created by Alex on 02.01.2017.
 */
public class PrimeDice {

    //basic variables
    private static String API_KEY;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";
    private static final String LESS = "<";
    private static final String MORE = ">";
    private static double profit = 0;
    private static int rollNumber = 1;

    //betting parameters
    private static double betAmount = 1;
    private static double baseBet = 1;
    private static double target = 49.5; //0 - 99.99
    public static String condition = MORE;
    private static int seedChangeFrequency = 100;   //change seed after indicated amount of bets
    private static int rollTarget = 300;
    private static int profitTarget = 400;    // target profit in satoshi
    private static double onLooseMultiplier = 2.5;

    public static UserStats stats = new UserStats();
    public static PrimeDice dice = new PrimeDice();

    public static void main(String[] args) throws Exception {

        Bet currentBet;

        dice.login();
        dice.getStats();

        while (rollNumber < rollTarget && profit < profitTarget) {
            try {
                if (rollNumber == seedChangeFrequency) {
                    dice.changeSeed();
                }

                if (stats.getBalance() < betAmount) {
                    System.out.println("Not enough funds.");
                    Thread.sleep(3000);
                    System.exit(0);
                }

                currentBet = dice.makeBet();
                currentBet.printRoll();
                rollNumber = currentBet.getRollNumber();
                profit = currentBet.getSessionProfit();


                if (!currentBet.isWin())
                {
                    betAmount *= onLooseMultiplier;
                } else {
                    betAmount = baseBet;
                }
                Thread.sleep(330);
            } catch (Exception e) {
                System.out.println(e);
                Thread.sleep(1000);
            }
        }
    }

    public void login() {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter your API key:");
        API_KEY = in.nextLine();
        in.close();
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
                throw new Exception("Connection failed: " + status + " " + response.getStatusLine().getReasonPhrase());
            }
            HttpEntity entity = response.getEntity();
            String apiOutput = EntityUtils.toString(entity);
            System.out.println(apiOutput);

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
                throw new Exception("Connection failed: " + status + " " + response.getStatusLine().getReasonPhrase());
            }

            HttpEntity entity = response.getEntity();
            String apiOutput = EntityUtils.toString(entity);
            //System.out.println(apiOutput.toString());

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

    public void changeSeed() throws Exception { //TODO: fix 429 HTTP error
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
            HttpEntity entity = response.getEntity();
            //System.out.println(response);
            String apiOutput = EntityUtils.toString(entity);
            //System.out.println(apiOutput);
            System.out.println("Seed changed!");
        } finally {
            response.close();
        }
    }

    public String generateRandomString() {
        return UUID.randomUUID().toString().substring(0, 30);
    }
}
