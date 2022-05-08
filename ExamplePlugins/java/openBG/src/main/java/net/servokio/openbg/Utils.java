package net.servokio.openbg;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Utils {
    public static String[] httpGet(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; openBGAliucordPlugin/0.0.1; +http://servokio.ru)");
        con.setRequestProperty("Accept-Charset", "UTF-8");
        con.setDoOutput(true);

        int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String line;
        StringBuffer response = new StringBuffer();

        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        String[] answer = new String[]{"code","response"};
        answer[0] = String.valueOf(responseCode);
        answer[1] = response.toString();
        return answer;
    }

    public static String[] httpPost(String url, String urlParameters) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; openBGAliucordPlugin/0.0.1; +http://servokio.ru)");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept-Encoding", "gzip,deflate");
        con.setRequestProperty("Accept-Charset", "UTF-8");

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());

        wr.write(urlParameters.getBytes(StandardCharsets.UTF_8));
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        String[] answer = new String[]{"code","response"};
        answer[0] = String.valueOf(responseCode);
        answer[1] = response.toString();
        return answer;
    }
}
