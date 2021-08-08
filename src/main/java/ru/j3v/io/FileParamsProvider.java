package ru.j3v.io;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

@Component
public class FileParamsProvider implements ParamsProvider {

    private Properties properties;

    public FileParamsProvider() {
        properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("experiment.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Date initialDate() {
        String initialDateString = properties.getProperty("initialDate");
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        try {
            return sdf.parse(initialDateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Double brokerCommission() {
        return Double.parseDouble(properties.getProperty("brokerCommission"));
    }

    @Override
    public Double taxRate() {
        return Double.parseDouble(properties.getProperty("taxRate"));
    }
}
