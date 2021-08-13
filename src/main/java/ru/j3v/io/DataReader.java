package ru.j3v.io;

import java.math.BigDecimal;
import java.util.Date;
import java.util.TreeMap;

public interface DataReader {
    TreeMap<Date, BigDecimal> readStocks();
    TreeMap<Date, BigDecimal> readInfl();
    TreeMap<Date, BigDecimal> read3MBonds();
}
