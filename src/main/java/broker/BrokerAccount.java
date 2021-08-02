package broker;

import java.util.*;

public class BrokerAccount {

    private ExchangeService exchangeService;

    private Map<String, Queue<Chunk>> assets;
    private Map<String, Queue<Chunk>> currencies;
    private double brokerCommission;
    private double taxRate;

    public BrokerAccount(double brokerCommission, double taxRate,
                         ExchangeService exchangeService) {
        this.brokerCommission = brokerCommission;
        this.taxRate = taxRate;
        this.exchangeService = exchangeService;
        assets = new HashMap<>();
        currencies = new HashMap<>();
    }

    public void buyAsset(String asset, double amount) {
        String currency = exchangeService.getCurrency(asset);
        double price = exchangeService.getPrice(asset);
        double cost = price * amount;
        if (currencyAmount(currency) >= cost) {
            
            assets.get(asset).peek();
        }

    }

    public double currencyAmount(String currency) {
        Queue<Chunk> cash = currencies.get(currency);
        double amount = 0.0;
        for (Chunk each: cash) {
            amount += each.getAmount();
        }
        return amount;
    }

    public class Chunk {
        private double price;
        private double amount;
        private Date date;

        public Chunk(double price, double amount, Date date) {
            this.price = price;
            this.amount = amount;
            this.date = date;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        @Override
        public String toString() {
            return "Chunk{" +
                    "price=" + price +
                    ", amount=" + amount +
                    ", date=" + date +
                    '}';
        }
    }
}
