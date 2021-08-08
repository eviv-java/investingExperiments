package ru.j3v.broker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.j3v.io.ParamsProvider;

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
    private ParamsProvider paramsProvider;

    @Autowired
    public BrokerAccount(ExchangeService exchangeService,
                         TimeService timeService,
                         ParamsProvider paramsProvider) {
        this.exchangeService = exchangeService;
        this.timeService = timeService;
        this.paramsProvider = paramsProvider;
        assets = new HashMap<>();
        currencies = new HashMap<>();
        brokerCommission = paramsProvider.brokerCommission();
        taxRate = paramsProvider.taxRate();
    }

    public void buyAsset(String asset, double amount) throws NoCashException{
        String currency = exchangeService.getCurrency(asset);
        double price = exchangeService.getPrice(asset);
        double cost = price * amount;
        if (currencyAmount(currency) >= cost) {
            try {
                double commission = Math.round(cost * brokerCommission) * 0.01;
                reduceCurrency(currency, cost + commission);
                Queue<Chunk> assetAccount;
                if (assets.get(asset) != null) {
                    assetAccount = assets.get(asset);
                } else {
                    assetAccount = new LinkedList<>();
                    assets.put(asset, assetAccount);
                }
                assetAccount.add(new Chunk(price, amount, timeService.getCurrentDate()));
            } catch (Exception e) {
                throw new NoCashException(e);
            }
        }
    }

    public void sellAsset(String asset, double amount) throws Exception {
        String currency = exchangeService.getCurrency(asset);
        double price = exchangeService.getPrice(asset);
        if (assetAmount(asset) >= amount) {
            Queue<Chunk> stocks = assets.get(asset);
            if (stocks == null) {
                throw new NoStocksAmount("There are no " + asset + " in your account");
            }
            double cashIncome = amount * exchangeService.getPrice(asset);
            double commission = Math.round(cashIncome * brokerCommission) * 0.01;
            double taxes = 0.0;
            while (amount > 0) {
                Chunk curChunk = stocks.peek();
                double chunkAmount = curChunk.getAmount();
                double chunkPrice = curChunk.getPrice();
                Date chunkDate = curChunk.getDate();
                if (chunkAmount > amount) {
                    chunkAmount -= amount;
                    amount -= amount;
                    curChunk.setAmount(chunkAmount);
                    // calculate taxes
                    taxes += calculateTaxes(asset, curChunk, amount);
                } else {
                    amount -= chunkAmount;
                    stocks.poll();
                    // calculate taxes
                    taxes += calculateTaxes(asset, curChunk, amount);
                }
            }
            System.out.println("Sold " + amount + " stocks of " + asset + " for " + cashIncome + currency);
            System.out.println("Taxes: " + taxes);
            System.out.println("Commission: " + commission);
            currencies.get(currency).add(new Chunk(1.0, cashIncome - taxes - commission, new Date()));
        }
    }

    private double calculateTaxes(String ticker, Chunk curChunk, double amount) {
        LocalDate today = timeService.getCurrentDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate buyDate = curChunk.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();;
        if (!buyDate.plusYears(3).isAfter(today)) {
            return 0;
        }
        double curPrice = exchangeService.getPrice(ticker);
        double buyPrice = curChunk.getPrice();
        if (curPrice <= buyPrice) {
            return 0;
        } else {
            double income = (curPrice - buyPrice) * amount;
            return Math.round(income * taxRate) * 0.01;
        }
    }

    public void inputCash(String currency, double amount) {
        Queue<Chunk> cash;
        if (currencies.get(currency) != null) {
            cash = currencies.get(currency);
        } else {
            cash = new LinkedList<>();
            currencies.put(currency, cash);
        }
        cash.add(new Chunk(1.0, amount, timeService.getCurrentDate()));
    }

    private void reduceCurrency(String currency, double amount) throws NoCashException{
        Queue<Chunk> cash = currencies.get(currency);
        if (cash == null) {
            throw new NoCashException("There is no " + currency + " account.");
        }
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

    public double currencyAmount(String currency) {
        Queue<Chunk> cash = currencies.get(currency);
        double amount = 0.0;
        if (cash == null) {
            return 0.0;
        }
        for (Chunk each: cash) {
            amount += each.getAmount();
        }
        return amount;
    }

    public double assetAmount(String asset) {
        Queue<Chunk> stocks = assets.get(asset);
        double amount = 0.0;
        if (stocks == null) {
            return 0.0;
        }
        for (Chunk each: stocks) {
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
