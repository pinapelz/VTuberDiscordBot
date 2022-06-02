package holodex;

import com.google.gson.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HolodexApi {

    public HolodexApi(){

    }
    public String isLiveData(String channelID){
        String apiResult = "";
        try {
            URL url = new URL("https://holodex.net/api/v2/live?channel_id="+channelID+"&status=live");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestProperty("accept", "application/json");
            BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                apiResult = inputLine;
            }
            in.close();
            System.out.println("Response with code: " + http.getResponseCode());
            http.disconnect();

        } catch (Exception E) {

        }

        return apiResult;
    }
}
