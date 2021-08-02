package broker;

import java.util.Date;
import java.util.Map;

public class ExchangeService {

    private TimeService timeService;

    private Map<String, Map<Date, Double>> assetsPrices;
    private Map<String, Map<Date, Double>> currencyPrices;
    private Map<String, String> assetCurrency;

    public ExchangeService(TimeService timeService) {
        this.timeService = timeService;

    }

    public String getCurrency(String asset) {
        return assetCurrency.get(asset);
    }


    public double getPrice(String asset) {
        Date date = timeService.getCurrentDate();
        return assetsPrices.get(asset).get(date);
    }
}
