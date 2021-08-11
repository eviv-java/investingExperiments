package ru.j3v.io;

import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class FileDataReader implements DataReader {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    private SimpleDateFormat dateMonthNumFormat = new SimpleDateFormat("M dd, yyyy", Locale.US);
    private NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);


    public TreeMap<Date, BigDecimal> readStocks() {
        List<String> lines = readFile("spxMonthly1913-2021.txt");
        TreeMap<Date, BigDecimal> result = new TreeMap<>();
        for (String line: lines) {
            String[] parts = line.split("\t");
            Date date;
            BigDecimal price;
            try {
                date = dateFormat.parse(parts[0]);
                price = new BigDecimal(numberFormat.parse(parts[1]).toString());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            result.put(date, price);
        }
        return result;
    }

    public TreeMap<Date, BigDecimal> readInfl() {
        List<String> lines = readFile("cpiMonthly1913-2021.txt");
        TreeMap<Date, BigDecimal> result = new TreeMap<>();
        for (String line: lines) {
            String[] parts = line.split("\t");
            String year = parts[0];
            for (int month = 1; month <=12; month++) {
                if (parts[month].length() > 1) {
                    Date date;
                    BigDecimal price;
                    try {
                        date = dateMonthNumFormat.parse("" + month + " 01, " + year);
                        price = new BigDecimal(numberFormat.parse(parts[month]).toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                    result.put(date, price);
                }
            }
        }
        return result;
    }

    private List<String> readFile(String filename) {

        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(filename);
        File file;
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + filename);
        } else {
            try {
                file = new File(resource.toURI());
                return Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }
}
