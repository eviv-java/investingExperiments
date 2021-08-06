package ru.j3v.broker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class BrokerAccount {

    private ExchangeService exchangeService;

    private Map<String, Queue<Chunk>> assets;
    private Map<String, Queue<Chunk>> currencies;
    private double brokerCommission;
    private double taxRate;
    private TimeService timeService;

    @Autowired
    public BrokerAccount(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
        assets = new HashMap<>();
        assets.put("SPX", new LinkedList<>());
        currencies = new HashMap<>();
    }

    public void buyAsset(String asset, double amount) {
        String currency = exchangeService.getCurrency(asset);
        double price = exchangeService.getPrice(asset);
        double cost = price * amount;
        if (currencyAmount(currency) >= cost) {
            reduceCurrency(currency, cost);
            assets.get(asset).add(new Chunk(price, amount, timeService.getCurrentDate()));
        }

    }

    private void reduceCurrency(String currency, double amount) {
        Queue<Chunk> cash = currencies.get(currency);
        while(amount > 0) {
            double currentAmount = cash.peek().getAmount();
            if (currentAmount > amount) {
                currentAmount -= amount;
                amount -= amount;
                cash.peek().setAmount(currentAmount);
            } else {
                amount -= currentAmount;
                cash.poll();
            }
        }
    }

    public static void main(String[] args) {
        LocalDate zeroDate = LocalDate.of(1913, 1, 1);
        Date initialDate =  Date.from(zeroDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        TimeService timeService = new TimeService();
        ExchangeService exchangeService = new ExchangeService(timeService);
        BrokerAccount ba = new BrokerAccount(exchangeService);
        ba.currencies = new HashMap<>();
        ba.timeService = timeService;
        Queue<Chunk> currensiesChuks = new LinkedList<>();
        currensiesChuks.add(new Chunk(1.0, 100.0, initialDate));
        currensiesChuks.add(new Chunk(1.0, 150.0, initialDate));
        currensiesChuks.add(new Chunk(1.0, 10.0, initialDate));
        currensiesChuks.add(new Chunk(1.0, 10.0, initialDate));
        currensiesChuks.add(new Chunk(1.0, 1000.0, initialDate));
        ba.currencies.put("USD", currensiesChuks);
        ba.buyAsset("SPX", 10);
        ba.buyAsset("SPX", 30);
    }

    public double currencyAmount(String currency) {
        Queue<Chunk> cash = currencies.get(currency);
        double amount = 0.0;
        for (Chunk each: cash) {
            amount += each.getAmount();
        }
        return amount;
    }

    public static class Chunk {
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
