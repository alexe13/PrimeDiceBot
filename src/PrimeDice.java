package src;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;


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
import org.json.*;


/**
 * Created by Alex on 02.01.2017.
 */
public class PrimeDice {

    private static String API_KEY;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";
    private static double betAmount = 2;
    private static double target = 50; //0 - 99.99
    private static final String LESS = "<";
    private static final String MORE = ">";

    public UserStats stats = new UserStats();
    public Bet currentBet = new Bet();

    public static void main(String[] args) throws Exception {
        PrimeDice dice = new PrimeDice();
        dice.login();
        dice.getStats();
        //sample betting loop
        for (int i = 0; i < 10; i++) {  //TODO: implement betting strategy
            try {
                dice.makeBet();
                Thread.sleep(290);
            }
            catch (Exception e) {
                Thread.sleep(1000);
            }
        }
        dice.changeSeed();
    }

    public void login() {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter your API key:");
        API_KEY = in.nextLine();
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
        }
        finally {
            response.close();
        }

    }

    public void makeBet() throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost("https://api.primedice.com/api/bet?api_key=" + API_KEY);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("amount", String.valueOf(betAmount)));
        params.add(new BasicNameValuePair("target", String.valueOf(target)));
        params.add(new BasicNameValuePair("condition", MORE));
        post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        CloseableHttpResponse response = client.execute(post);
        try {
            int status = response.getStatusLine().getStatusCode();
            if (status != 200) {
                throw new Exception("Connection failed: " + status + " " + response.getStatusLine().getReasonPhrase());
            }

            HttpEntity entity = response.getEntity();

            String apiOutput = EntityUtils.toString(entity);
            //System.out.println(apiOutput);

            JSONObject bet = new JSONObject(apiOutput).getJSONObject("bet");
            currentBet.parseBet(bet);
            currentBet.printRoll();
            currentBet.setRollNumber(currentBet.getRollNumber()+1);

        }
        finally {
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
            System.out.println(response);
            String apiOutput = EntityUtils.toString(entity);
            System.out.println(apiOutput);
        }
        finally {
            response.close();
        }
    }

    public String generateRandomString () {
        return UUID.randomUUID().toString().substring(0, 30);
    }
}
