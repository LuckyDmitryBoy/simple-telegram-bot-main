package io.proj3ct.SpringDemoBot.service;

import io.proj3ct.SpringDemoBot.model.WeartherModel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

//https://api.openweathermap.org/data/2.5/weather?q=London&units=metric&appid=0282ba41b2f60f80e0bc62d1d9574aaa
public class WeartherService {
    public static String getWearther(String message, WeartherModel model) throws IOException {//url  с которого берем инфу т.е.api адресс
        URL url = new URL("https://api.openweathermap.org/data/2.5/weather?q="+message+"&units=metric&appid=0282ba41b2f60f80e0bc62d1d9574aaa");
        Scanner scanner = new Scanner((InputStream) url.getContent());//вводим нужный город
        String result = "";//считываем результат и сохраняем в переменную
        while (scanner.hasNext()){
            result +=scanner.nextLine();
        }
        JSONObject object = new JSONObject(result);

        model.setName(object.getString("name"));
        JSONObject main=object.getJSONObject("main");//json в jsone
        model.setTemp(main.getDouble("temp"));
        model.setHumidity(main.getDouble("humidity"));
        JSONArray getArray=object.getJSONArray("weather");
        for (int i=0;i<getArray.length();i++){
            JSONObject obj=getArray.getJSONObject(i);
            model.setIcon((String) obj.get("icon"));
            model.setMain((String) obj.get("main"));
        }


        return "City: " + model.getName() + "\n" +
                "Temperature: " + model.getTemp() + "C"+"\n" +
                "Humidity: " + model.getHumidity() + "%"+"\n" +
                "Main: "+model.getMain()+"\n" +
                "https://openweathermap.org/img/w/" + model.getIcon()+".png";

    }

    }

