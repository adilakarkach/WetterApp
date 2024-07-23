import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

//hier soll über die API Wetterdaten eingeholt werden um sie auf der GUI darzustellen

public class WeatherApp {

    public static JSONObject getWeatherData(String locationName){

        JSONArray locationData = getLocationData(locationName);

        //Längen und Breitengrade
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        String urlString = "https://api.open-meteo.com/v1/forecast?" +
        "latitude=" + latitude + "&longitude=" + longitude +"&hourly=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=Europe%2FBerlin";


        try{

            HttpURLConnection conn = fetchApiResponse(urlString);

            if(conn.getResponseCode() != 200){
                System.out.printf("Error: Could not connect to API");
                return null;
            }

            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());

            while(scanner.hasNext()){
                resultJson.append(scanner.nextLine());
            }

            scanner.close();

            //URL-Verbindung abbrechen
            conn.disconnect();

            JSONParser parser = new JSONParser();
            JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            //hole aus dem JSON Objekt die Stundendaten
            JSONObject hourly = (JSONObject) resultsJsonObj.get("hourly");

            //wir brauchen die Daten für die aktuelle Stunde
            //demnach brauchen wir den index der aktuellen Stunde
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            //Temperatur
            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
            double temperature = (double) temperatureData.get(index);

            JSONArray weathercode = (JSONArray) hourly.get("weather_code");
            String weatherCondition = convertWeatherCode((long) weathercode.get(index));

            //Feuchtigkeit
            JSONArray relativeHumidity = (JSONArray) hourly.get("relative_humidity_2m");
            long humidity = (long) relativeHumidity.get(index);

            //windgeschwindigkeit
            JSONArray windSpeed = (JSONArray) hourly.get("wind_speed_10m");
            double windspeed = (double) windSpeed.get(index);


            //kreire JSON data object für das frontend
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("windspeed", windspeed);

            return weatherData;

        }catch(IOException e){
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }


        return null;

    }

    // die geografischen Daten passend zum Namen des Ortes
    public static JSONArray getLocationData(String locationName){
        //ersetze Leerzeichen durch "+" um dem API Format gerecht zu werden
        locationName  = locationName.replaceAll(" ", "+");

        //API URL
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locationName + "&count=10&language=en&format=json";

        try{
            //rufe API auf und erhalte eine Antwort
            HttpURLConnection conn = fetchApiResponse(urlString);

            //HTTP response status check
            if(conn.getResponseCode() != 200) {
                System.out.printf("Error: Could not connect to API");
                return null;
            }
            else{
                //speichere die Daten in unserem StringBuilder
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());

                while(scanner.hasNext()){
                    resultJson.append(scanner.nextLine());
                }

                scanner.close();

                //URL-Verbindung abbrechen
                conn.disconnect();

                //wir brauchen JSON-Format deswegen:
                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));



                //generiere Liste der Ortsdaten aus dem eingegbenen String des Endusers
                JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
                return locationData;

            }

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }



    }

    private static HttpURLConnection fetchApiResponse(String urlString){
        try{
            //verbinde
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //request Methode auf get setzen
            conn.setRequestMethod("GET");

            //verbinde mit API
            conn.connect();
            return conn;

        }catch(IOException e){
            e.printStackTrace();
        }
        //verbindung nicht möglich
        return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList){
        String currentTime = getCurrentTime();
        int index;

        //Iteriere durch die timeList um den index der aktuellen Zeit herauszufinden
        for(int i = 0; i < timeList.size(); i++){

            if(timeList.get(i)==currentTime){

                return i;
            }
        }

        return 0;
    }

    public static String getCurrentTime(){
        LocalDateTime currentDateTime = LocalDateTime.now();

        //Daten formatieren in 2024-07-23T14:00 (um dem Format der API zu entsprechen)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");

        String formattedDateTime = currentDateTime.format(formatter);

        return formattedDateTime;
    }

    //wandelt den Wettercode in natürliche Sprache um
    private static String convertWeatherCode(long weathercode){

        String weatherCondition = "";

        if(weathercode == 0L){
            // clear
            weatherCondition = "Clear";
        }else if(weathercode > 0L && weathercode <= 3L){
            // cloudy
            weatherCondition = "Cloudy";
        }else if((weathercode >= 51L && weathercode <= 67L)
                || (weathercode >= 80L && weathercode <= 99L)){
            // rain
            weatherCondition = "Rain";
        }else if(weathercode >= 71L && weathercode <= 77L){
            // snow
            weatherCondition = "Snow";
        }

        return weatherCondition;

    }

}
