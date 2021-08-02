package io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileDataReader implements DataReader {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    private SimpleDateFormat dateMonthNumFormat = new SimpleDateFormat("M dd, yyyy", Locale.US);
    private NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);


    public TreeMap<Date, Double> readStocks() {
        List<String> lines = readFile("/home/j3v/spxMonthly1913-2021.txt");
        TreeMap<Date, Double> result = new TreeMap<>();
        for (String line: lines) {
            String[] parts = line.split("\t");
            Date date;
            Double price;
            try {
                date = dateFormat.parse(parts[0]);
                price = new Double(numberFormat.parse(parts[1]).toString());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            result.put(date, price);
        }
        return result;
    }

    public TreeMap<Date, Double> readInfl() {
        List<String> lines = readFile("/home/j3v/cpiMonthly1913-2021.txt");
        TreeMap<Date, Double> result = new TreeMap<>();
        for (String line: lines) {
            String[] parts = line.split("\t");
            String year = parts[0];
            for (int month = 1; month <=12; month++) {
                if (parts[month].length() > 1) {
                    Date date;
                    Double price;
                    try {
                        date = dateMonthNumFormat.parse("" + month + " 01, " + year);
                        price = new Double(numberFormat.parse(parts[month]).toString());
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
        List<String> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                result.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
