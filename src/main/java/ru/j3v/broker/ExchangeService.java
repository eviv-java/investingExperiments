package ru.j3v.broker;

import ru.j3v.io.DataReader;
import ru.j3v.io.FileDataReader;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExchangeService {

    private TimeService timeService;

    private Map<String, Map<Date, Double>> assetsPrices;
    private Map<String, Map<Date, Double>> currencyPrices;
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

    public double getPrice(String asset) {
        Date date = timeService.getCurrentDate();
        return assetsPrices.get(asset).get(date);
    }
}
