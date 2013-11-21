package com.example.weather;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import android.graphics.Bitmap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Charm
 * Date: 19.11.13
 * Time: 15:58
 * To change this template use File | Settings | File Templates.
 */
public class ShowWeather extends Activity {
    String cityName;
    Bitmap imageBitmap;
    ArrayList <DetailsDay> days;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showweather);
        Intent intent = getIntent();
        cityName = intent.getStringExtra("city");
        int index = intent.getIntExtra("index", 0);

        String link = "http://weather.yahooapis.com/forecastrss?w=" + String.valueOf(index) + "&u=c";
        GetXml xml = new GetXml(link);
        xml.execute();

    }

    public class GetXml extends AsyncTask < Void, Void, ArrayList<String>> {
        String link;

        GetXml(String link) {
            this.link = link;
        }

        public void onPostExecute(ArrayList<String> data) {
            super.onPostExecute(data);

            TextView city = (TextView)findViewById(R.id.city);
            TextView date = (TextView)findViewById(R.id.lastupdate);
            TextView temperature = (TextView)findViewById(R.id.temperature);
            ImageView image = (ImageView)findViewById(R.id.image);
            TextView chill = (TextView)findViewById(R.id.chill);
            TextView direction = (TextView)findViewById(R.id.direction);
            TextView speed = (TextView)findViewById(R.id.speed);
            TextView humidity = (TextView)findViewById(R.id.humidity);
            TextView visibility = (TextView)findViewById(R.id.visibility);
            TextView pressure = (TextView)findViewById(R.id.pressure);
            TextView rising = (TextView)findViewById(R.id.rising);
            TextView sunrise = (TextView)findViewById(R.id.sunrise);
            TextView sunset = (TextView)findViewById(R.id.sunset);
            TextView sky = (TextView)findViewById(R.id.sky);
            TextView day1 = (TextView)findViewById(R.id.day1);
            TextView day2 = (TextView)findViewById(R.id.day2);
            TextView day3 = (TextView)findViewById(R.id.day3);

            city.setText(cityName);
            date.setText("last update: " + data.get(0));
            temperature.setText("Temperature:" + data.get(11));
            chill.setText("chill:" + data.get(1));
            direction.setText("direction:" + data.get(2));
            speed.setText("speed:" + data.get(3));
            humidity.setText("humidity:" + data.get(4));
            visibility.setText("visibility:" + data.get(5));
            pressure.setText("pressure:" + data.get(6));
            rising.setText("rising:" + data.get(7));
            sunrise.setText("sunrise:" + data.get(8));
            sunset.setText("sunset:" + data.get(9));
            sky.setText(data.get(10));
            image.setImageBitmap(imageBitmap);
            day1.setText(days.get(0).getAll());
            day2.setText(days.get(1).getAll());
            day3.setText(days.get(2).getAll());
        }

        @Override
        protected ArrayList <String> doInBackground(Void... params) {
            try {
                URL url = new URL(link);
                HttpURLConnection connect = (HttpURLConnection) url.openConnection();
                if (connect.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream input = connect.getInputStream();
                    DocumentBuilderFactory dBF = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dB = dBF.newDocumentBuilder();
                    Document document = dB.parse(input);
                    Element element = document.getDocumentElement();

                    NodeList nodelist = element.getElementsByTagName("channel");
                    Element entry = (Element) nodelist.item(0);
                    Element date = (Element) entry.getElementsByTagName("lastBuildDate").item(0);
                    Element wind = (Element) entry.getElementsByTagName("yweather:wind").item(0);
                    Element atmosphere = (Element) entry.getElementsByTagName("yweather:atmosphere").item(0);
                    Element astronomy = (Element) entry.getElementsByTagName("yweather:astronomy").item(0);


                    nodelist = element.getElementsByTagName("item");
                    entry = (Element) nodelist.item(0);
                    Element description = (Element) entry.getElementsByTagName("description").item(0);
                    String description_ = description.getFirstChild().getNodeValue();

                    String tmp = ((description_.replaceAll("<br />", "")).replaceAll("<b>", "")).replaceAll("<BR>", "").replaceAll("</b>", "").replaceAll("<BR />", "");

                    String[] weather = tmp.split("\n");
                    int index = weather[3].indexOf(",");
                    ArrayList<String> result = new ArrayList<String>(13);

                    result.add(date.getFirstChild().getNodeValue());       //date
                    result.add(wind.getAttribute("chill"));
                    result.add(wind.getAttribute("direction"));
                    result.add(wind.getAttribute("speed"));
                    result.add(atmosphere.getAttribute("humidity"));
                    result.add(atmosphere.getAttribute("visibility"));
                    result.add(atmosphere.getAttribute("pressure"));
                    result.add(atmosphere.getAttribute("rising"));
                    result.add(astronomy.getAttribute("sunrise"));
                    result.add(astronomy.getAttribute("sunset"));
                    result.add(weather[3].substring(0, index));   //sky
                    result.add(weather[3].substring(index + 2, weather[3].indexOf(" C")));      //temperature


                    try {
                        url = new URL(description_.substring(description_.indexOf("\"") + 1, description_.indexOf("\"", description_.indexOf("\"") + 1)));
                        connect = (HttpURLConnection) url.openConnection();
                        if (connect.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            input = connect.getInputStream();
                            imageBitmap = BitmapFactory.decodeStream(input);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    days = new ArrayList<DetailsDay>();
                    for(int i = 0; i < 3; i++) {
                        Element forecast = (Element) entry.getElementsByTagName("yweather:forecast").item(0);
                        entry.removeChild(forecast);
                        DetailsDay temp = new DetailsDay();
                        temp.setDay(forecast.getAttribute("day"));
                        temp.setDate(forecast.getAttribute("date"));
                        temp.setLow(forecast.getAttribute("low"));
                        temp.setHigh(forecast.getAttribute("high"));
                        temp.setSky(forecast.getAttribute("text"));
                        days.add(temp);
                    }
                    return result;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}