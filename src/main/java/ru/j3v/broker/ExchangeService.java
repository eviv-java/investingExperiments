package ru.j3v.broker;

import ru.j3v.io.DataReader;
import ru.j3v.io.FileDataReader;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ExchangeService {

    private TimeService timeService;

    private Map<String, Map<Date, BigDecimal>> assetsPrices;
    private Map<String, Map<Date, BigDecimal>> currencyPrices;
    private Map<String, String> assetCurrency;

    public ExchangeService(TimeService timeService) {
        this.timeService = timeService;
        assetsPrices = new HashMap<>();
        DataReader dr = new FileDataReader();
        assetsPrices.put("SPX", dr.readStocks());
        assetCurrency = new HashMap<>();
        assetCurrency.put("SPX", "USD");
    }

    public String getCurrency(String asset) {
        return assetCurrency.get(asset);
    }

    public BigDecimal getPrice(String asset) {
        Date date = timeService.getCurrentDate();
        return assetsPrices.get(asset).get(date);
    }

    public Set<String> assetsSet() {
        return assetsPrices.keySet();
    }
}
