import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WeatherAppGui extends JFrame{

    private JSONObject weatherData;

    public WeatherAppGui(){
        super("Weather App");

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setSize(450,650);

        //Gui in Bildschirmmitte
        setLocationRelativeTo(null);

        //Layout manager wird auf "null" gesetzt, damit Positionen der Komponenten manuell bestimmt werden
        setLayout(null);

        //verhindert Veränderung der Fenstergröße
        setResizable(false);

        addGuiComponents();

    }

    private void addGuiComponents(){
        //suchfeld
        JTextField searchTextField = new JTextField();

        //Position des Textfeldes festlegen
        searchTextField.setBounds(15,15, 351,45);

        //font und style

        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));

        add(searchTextField);


        //Wetter Bild
        JLabel weatherConditionImage = new JLabel(loadImage("C:\\Users\\Nie Wieder\\Desktop\\Webanwendung\\Webanwendung\\WeatherAppGUI\\src\\assets\\cloudy.png"));
        weatherConditionImage.setBounds(0, 125, 450, 217);
        add(weatherConditionImage);

        //Temperaturanzeige
        JLabel temperatureText = new JLabel("10 C");
        temperatureText.setBounds(0,350,450,54);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));

        //Text zentrieren
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        //Wetterbeschreibung
        JLabel weatherConditions = new JLabel("Cloudy");
        weatherConditions.setBounds(0, 405, 450,36);
        weatherConditions.setFont(new Font("Dialog", Font.PLAIN,32));
        weatherConditions.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditions);

        //Luftfeuchtigkeit
        JLabel humidityImage = new JLabel(loadImage("C:\\Users\\Nie Wieder\\Desktop\\Webanwendung\\Webanwendung\\WeatherAppGUI\\src\\assets\\humidity.png"));
        humidityImage.setBounds(15, 500, 74, 66);
        add(humidityImage);

        //Text: Luftfeuchtigkeit
        JLabel humidityText = new JLabel("<html><b>Humidity</b> 100%</html>");
        humidityText.setBounds(90,500,85,55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);

        //Windgeschwindigkeit Bild
        JLabel windspeedImage = new JLabel(loadImage("C:\\Users\\Nie Wieder\\Desktop\\Webanwendung\\Webanwendung\\WeatherAppGUI\\src\\assets\\windspeed.png"));
        windspeedImage.setBounds(220, 500, 74, 66);
        add(windspeedImage);

        //Text: Windgeschwindigkeit
        JLabel windspeedText = new JLabel("<html><b>Windspeed</b> 15km/h</html>");
        windspeedText.setBounds(310,500,85,55);
        windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(windspeedText);

        //search button
        JButton searchButton  = new JButton(loadImage("C:\\Users\\Nie Wieder\\Desktop\\Webanwendung\\Webanwendung\\WeatherAppGUI\\src\\assets\\search.png"));

        //Cursor soll ein Handgriffel sein wenn über dem Searchbutton
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375,13,47,45);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //Nutzereingabe
                String userInput = searchTextField.getText();

                //Glätte Nutzereingabe/Entferne whitespaces
                if(userInput.replaceAll("\\s","").length() <= 0){
                    return;
                }

                //In "Weatherapp" bearbeitete Daten in das JSONObject für die GUI laden
                weatherData = WeatherApp.getWeatherData(userInput);

                //GUI update
                String weatherCondition = (String) weatherData.get("weather_condition");

                //Abhängig von dem Wetter zu gegebener STunde soll das Bild angepasst werden (wolkig, sonnig etc.)
                switch (weatherCondition){
                    case "Clear": weatherConditionImage.setIcon(loadImage("src/assets/clear.png"));
                        break;
                    case "Cloudy": weatherConditionImage.setIcon(loadImage("src/assets/cloudy.png"));
                        break;
                    case "Rain": weatherConditionImage.setIcon(loadImage("src/assets/rain.png"));
                        break;
                    case "Snow": weatherConditionImage.setIcon(loadImage("src/assets/snow.png"));
                        break;
                }

                //Temperatur anpassen
                double temperature = (double) weatherData.get("temperature");

                temperatureText.setText(temperature + " C");

                //Wetterkonditionentext anpassen
                weatherConditions.setText(weatherCondition);

                //Luftfeuchtigkeitstext anpassen

                long humidity = (long) weatherData.get("humidity");
                humidityText.setText("<html><b>Humidity</b> " + humidity +"%</html>");

                //Windgeschwinddigkeit anpassen
                double windspeed = (double) weatherData.get("windspeed");
                windspeedText.setText("<html><b>Windspeed</b> " + windspeed + "km/h</html>");


            }
        });
        add(searchButton);

    }

    //lädt Bilder in unsere gui Komponenten
    private ImageIcon loadImage(String resourcePath){
        try {

            //liest Bild vom Pfad ein
            BufferedImage image = ImageIO.read(new File(resourcePath));

            //gibt das bild zurück damit es in unserer Komponente angezeigt werden kann
            return new ImageIcon(image);
        }catch(IOException e){
            e.printStackTrace();
        }

        System.out.println("Could not find resource");
        return null;
    }


}