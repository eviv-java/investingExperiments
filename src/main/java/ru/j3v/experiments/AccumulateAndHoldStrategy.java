package ru.j3v.experiments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.j3v.broker.BrokerAccount;
import ru.j3v.broker.ExchangeService;
import ru.j3v.broker.NoCashException;
import ru.j3v.broker.TimeService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

@Component
public class AccumulateAndHoldStrategy implements Strategy{

    private static final Logger LOGGER = LoggerFactory.getLogger(AccumulateAndHoldStrategy.class);

    private TimeService timeService;
    private BrokerAccount brokerAccount;
    private ExchangeService exchangeService;

    public AccumulateAndHoldStrategy(TimeService timeService, BrokerAccount brokerAccount, ExchangeService exchangeService) {
        this.timeService = timeService;
        this.brokerAccount = brokerAccount;
        this.exchangeService = exchangeService;
    }

    public Triple experiment(int year) throws NoCashException {
        timeService.setCurrentDate(new Date(13, 1, 1));
        LocalDate limitDate = new Date(121, 01, 01).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        limitDate = limitDate.minusYears(year);

        ArrayList<Double> coefs = new ArrayList<Double>();

        while (timeService.getCurrentDate().before(Date.from(limitDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))) {
            Date startDate = timeService.getCurrentDate();
            brokerAccount.clear();

            BigDecimal totalInput = BigDecimal.ZERO;

            for (int i = 0; i < 12 * year; i++) {
                BigDecimal inputAmount = exchangeService.getInflation().multiply(BigDecimal.TEN);
                brokerAccount.inputCash("USD", inputAmount);
                totalInput = totalInput.add(inputAmount);
                BigDecimal amountSpx = brokerAccount.amountForCash("SPX", brokerAccount.currencyAmount("USD"));
                brokerAccount.buyAsset("SPX", amountSpx);
                timeService.passMonths(1);
            }
            LOGGER.info("\n");
            LOGGER.info("*****************************");
            LOGGER.info("Started in " + startDate);
            LOGGER.info("Put " + 12 * year * 10 + " standard buckets of food");
            LOGGER.info("Now it is " + timeService.getCurrentDate());
            BigDecimal portfolioPrice = brokerAccount
                    .assetAmount("SPX")
                    .multiply(exchangeService.getPrice("SPX")).add(brokerAccount.currencyAmount("USD"));
            LOGGER.info("Totally input $" + totalInput);
            LOGGER.info("Portfolio costs $" + portfolioPrice);
            BigDecimal resultWithInflation = portfolioPrice.divide(exchangeService.getInflation(), 0, RoundingMode.FLOOR);
            LOGGER.info("You can buy " + resultWithInflation + " standard buckets of food");
            BigDecimal coefficient = resultWithInflation.divide(new BigDecimal(12 * year * 10), 2, RoundingMode.FLOOR);
            LOGGER.info("Coefficient is " + coefficient);
            LOGGER.info("*****************************");
            LOGGER.info("\n");

            coefs.add(coefficient.doubleValue());

            timeService.setCurrentDate(startDate);
            timeService.passMonths(1);

        }
        Collections.sort(coefs);
        return new Triple(coefs.get(0), coefs.get(coefs.size() - 1), coefs.get(coefs.size()/2));
    }


}
