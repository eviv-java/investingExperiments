package ru.j3v.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.j3v.io.ParamsProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class BrokerAccount {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(BrokerAccount.class);

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

    public void buyAsset(String asset, BigDecimal amount) throws NoCashException{
        String currency = exchangeService.getCurrency(asset);
        BigDecimal price = exchangeService.getPrice(asset);
        BigDecimal cost = price
                .multiply(amount)
                .setScale(2, RoundingMode.HALF_UP);
        if (currencyAmount(currency).compareTo(cost) >= 0) {
            try {
                BigDecimal commission = cost.multiply(new BigDecimal(brokerCommission))
                        .multiply(new BigDecimal("0.01"))
                        .setScale(2, RoundingMode.HALF_UP);
                reduceCurrency(currency, cost.add(commission));
                Queue<Chunk> assetAccount;
                if (assets.get(asset) != null) {
                    assetAccount = assets.get(asset);
                } else {
                    assetAccount = new LinkedList<>();
                    assets.put(asset, assetAccount);
                }
                assetAccount.add(new Chunk(price, amount, timeService.getCurrentDate()));
                LOGGER.debug("Bought {} {} for {} {} ", amount, asset, cost, currency);
                LOGGER.debug("Commission: {}", commission);
            } catch (Exception e) {
                throw new NoCashException(e);
            }
        }
    }

    public void sellAsset(String asset, BigDecimal initialAmount) throws Exception {
        String currency = exchangeService.getCurrency(asset);
        BigDecimal amount = initialAmount;
        if (assetAmount(asset).compareTo(amount) >= 0) {
            Queue<Chunk> stocks = assets.get(asset);
            if (stocks == null) {
                throw new NoStocksAmount("There are no " + asset + " in your account");
            }
            BigDecimal cashIncome = amount.multiply(exchangeService.getPrice(asset));
            BigDecimal commission = cashIncome
                    .multiply(new BigDecimal(brokerCommission))
                    .multiply(new BigDecimal("0.01"))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal taxes = BigDecimal.ZERO;
            while (amount.compareTo(BigDecimal.ZERO) > 0) {
                Chunk curChunk = stocks.peek();
                BigDecimal chunkAmount = curChunk.getAmount();
                if (chunkAmount.compareTo(amount) > 0) {
                    chunkAmount = chunkAmount.subtract(amount);
                    taxes = taxes.add(calculateTaxes(asset, curChunk));
                    amount = BigDecimal.ZERO;
                    curChunk.setAmount(chunkAmount);
                } else {
                    taxes = taxes.add(calculateTaxes(asset, curChunk));
                    amount = amount.subtract(chunkAmount);
                    stocks.poll();
                }
            }
            LOGGER.debug("Sold {} stocks of {} for {} {}", initialAmount, asset, cashIncome, currency);
            LOGGER.debug("Taxes: {}",taxes);
            LOGGER.debug("Commission: {}", commission);
            currencies.get(currency).add(new Chunk(BigDecimal.ONE, cashIncome.subtract(taxes).subtract(commission), new Date()));
        }
    }

    private BigDecimal calculateTaxes(String ticker, Chunk curChunk) {
        LocalDate today = timeService.getCurrentDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate buyDate = curChunk.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();;
        if (!buyDate.plusYears(3).isAfter(today)) {
            return BigDecimal.ZERO;
        }
        BigDecimal curPrice = exchangeService.getPrice(ticker);
        BigDecimal buyPrice = curChunk.getPrice();
        if (curPrice.compareTo(buyPrice) <= 0) {
            return BigDecimal.ZERO;
        } else {
            BigDecimal income = (curPrice.subtract(buyPrice)).multiply(curChunk.getAmount());
            return income.multiply(new BigDecimal(taxRate))
                    .multiply(new BigDecimal("0.01"))
                    .setScale(2, RoundingMode.HALF_UP);
        }
    }

    public void inputCash(String currency, BigDecimal amount) {
        Queue<Chunk> cash;
        if (currencies.get(currency) != null) {
            cash = currencies.get(currency);
        } else {
            cash = new LinkedList<>();
            currencies.put(currency, cash);
        }
        cash.add(new Chunk(BigDecimal.ONE, amount, timeService.getCurrentDate()));
        LOGGER.debug("Added {} {}", amount, currency);
    }

    private void reduceCurrency(String currency, BigDecimal amount) throws NoCashException{
        Queue<Chunk> cash = currencies.get(currency);
        if (cash == null) {
            throw new NoCashException("There is no " + currency + " account.");
        }
        while(amount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal currentAmount = cash.peek().getAmount();
            if (currentAmount.compareTo(amount) > 0) {
                currentAmount = currentAmount.subtract(amount);
                amount = BigDecimal.ZERO;
                cash.peek().setAmount(currentAmount);
            } else {
                amount = amount.subtract(currentAmount);
                cash.poll();
            }
        }
    }

    public BigDecimal currencyAmount(String currency) {
        Queue<Chunk> cash = currencies.get(currency);
        BigDecimal amount = BigDecimal.ZERO;
        if (cash == null) {
            return BigDecimal.ZERO;
        }
        for (Chunk each: cash) {
            amount = amount.add(each.amount);
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal assetAmount(String asset) {
        Queue<Chunk> stocks = assets.get(asset);
        BigDecimal amount = BigDecimal.ZERO;
        if (stocks == null) {
            return amount;
        }
        for (Chunk each: stocks) {
            amount = amount.add(each.getAmount());
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal amountForCash(String ticker, BigDecimal cashAmount) {
        BigDecimal price = exchangeService.getPrice(ticker);
        BigDecimal priceCoefficient = new BigDecimal(brokerCommission)
                .multiply(new BigDecimal("0.01"))
                .add(BigDecimal.ONE);
        return cashAmount.divide(price.multiply(priceCoefficient), 2, RoundingMode.FLOOR);
    }

    public void clear() {
        assets = new HashMap<>();
        currencies = new HashMap<>();
    }

    public static class Chunk {
        private BigDecimal price;
        private BigDecimal amount;
        private Date date;

        public Chunk(BigDecimal price, BigDecimal amount, Date date) {
            this.price = price;
            this.amount = amount;
            this.date = date;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
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
