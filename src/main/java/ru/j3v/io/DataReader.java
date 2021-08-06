package ru.j3v.io;

import java.util.Date;
import java.util.TreeMap;

public interface DataReader {
    TreeMap<Date, Double> readStocks();
    TreeMap<Date, Double> readInfl();
}
