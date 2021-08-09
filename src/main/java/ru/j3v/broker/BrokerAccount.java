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
        double cost = Math.round(price * amount * 100) * 0.01;
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
                System.out.println("Bought " + amount + " " + asset + " for " + cost + " " + currency);
                System.out.println("Commission: " + commission);
            } catch (Exception e) {
                throw new NoCashException(e);
            }
        }
    }

    public void sellAsset(String asset, double initialAmount) throws Exception {
        String currency = exchangeService.getCurrency(asset);
        double amount = initialAmount;
        if (assetAmount(asset) >= amount) {
            Queue<Chunk> stocks = assets.get(asset);
            if (stocks == null) {
                throw new NoStocksAmount("There are no " + asset + " in your account");
            }
            double cashIncome = Math.round(amount * exchangeService.getPrice(asset) * 100) * 0.01;
            double commission = Math.round(cashIncome * brokerCommission) * 0.01;
            double taxes = 0.0;
            while (amount > 0) {
                Chunk curChunk = stocks.peek();
                double chunkAmount = curChunk.getAmount();
                if (chunkAmount > amount) {
                    chunkAmount -= amount;
                    taxes += calculateTaxes(asset, curChunk, amount);
                    amount -= amount;
                    curChunk.setAmount(chunkAmount);
                } else {
                    taxes += calculateTaxes(asset, curChunk, chunkAmount);
                    amount -= chunkAmount;
                    stocks.poll();
                }
            }
            System.out.println("Sold " + initialAmount + " stocks of " + asset + " for " + cashIncome + " " + currency);
            System.out.println("Taxes: " + Math.round(taxes * 100) * 0.01);
            System.out.println("Commission: " + commission);
            currencies.get(currency).add(new Chunk(1.0, Math.round((cashIncome - taxes - commission) * 100) * 0.01, new Date()));
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
        System.out.println("Added " + amount + " " + currency);
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

    public double amountForCash(String ticker, double cashAmount) {
        double price = exchangeService.getPrice(ticker);
        return Math.floor(cashAmount / (price * (1 + brokerCommission * 0.01)));
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
