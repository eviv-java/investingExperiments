package ru.j3v.broker;

import ru.j3v.io.DataReader;
import ru.j3v.io.FileDataReader;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class ExchangeService {

    private TimeService timeService;

    private Map<String, Map<Date, BigDecimal>> assetsPrices;
    private Map<Date, BigDecimal> inflation;
    private Map<String, Map<Date, BigDecimal>> currencyPrices;
    private Map<String, String> assetCurrency;
    private Map<Date, BigDecimal> bonds3m;

    public ExchangeService(TimeService timeService) {
        this.timeService = timeService;
        assetsPrices = new HashMap<>();
        DataReader dr = new FileDataReader();
        assetsPrices.put("SPX", dr.readStocks());
        inflation = dr.readInfl();
        assetCurrency = new HashMap<>();
        assetCurrency.put("SPX", "USD");
        bonds3m = dr.read3MBonds();
    }

    public String getCurrency(String asset) {
        return assetCurrency.get(asset);
    }

    public BigDecimal getPrice(String asset) {
        Date date = timeService.getCurrentDate();
        return assetsPrices.get(asset).get(date);
    }

    public BigDecimal getInflation() {
        Date date = timeService.getCurrentDate();
        return inflation.get(date);
    }

    public Set<String> assetsSet() {
        return assetsPrices.keySet();
    }

    public BigDecimal getBondsYield() {
        Date date = timeService.getCurrentDate();
        BigDecimal yield = null;
        do {
            yield = bonds3m.get(date);
            date = dayBack(date);
        } while (yield == null);
        return yield;
    }

    private Date dayBack(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Date.from(localDate.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

    }
}
