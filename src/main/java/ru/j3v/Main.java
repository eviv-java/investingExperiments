package ru.j3v;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import ru.j3v.broker.BrokerAccount;
import ru.j3v.broker.ExchangeService;
import ru.j3v.broker.NoCashException;
import ru.j3v.broker.TimeService;
import ru.j3v.io.DataReader;
import ru.j3v.io.FileDataReader;
import org.math.plot.Plot2DPanel;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.context.*;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static java.time.temporal.ChronoUnit.MONTHS;

public class Main {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

        ApplicationContext context = new ClassPathXmlApplicationContext("context.xml");

        BrokerAccount ba = (BrokerAccount)context.getBean("brokerAccount");
        ExchangeService es = (ExchangeService)context.getBean("exchangeService");
        TimeService ts = (TimeService)context.getBean("timeService");

        int year = 24;
        LocalDate limitDate = new Date(2018, 01, 01).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        limitDate = limitDate.minusYears(year);

        while (ts.getCurrentDate().before(Date.from(limitDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))) {
            Date startDate = ts.getCurrentDate();
            ba.clear();

            BigDecimal totalInput = BigDecimal.ZERO;

            for (int i = 0; i < 12 * year; i++) {
                BigDecimal inputAmount = es.getInflation().multiply(BigDecimal.TEN);
                ba.inputCash("USD", inputAmount);
                totalInput = totalInput.add(inputAmount);
                BigDecimal amountSpx = ba.amountForCash("SPX", ba.currencyAmount("USD"));
                ba.buyAsset("SPX", amountSpx);
                ts.passMonths(1);
            }
            LOGGER.warn("\n");
            LOGGER.warn("*****************************");
            LOGGER.warn("Started in " + startDate);
            LOGGER.warn("Put " + 12 * year * 10 + " standard buckets of food");
            LOGGER.warn("Now it is " + ts.getCurrentDate());
            BigDecimal portfolioPrice = ba.assetAmount("SPX").multiply(es.getPrice("SPX")).add(ba.currencyAmount("USD"));
            LOGGER.warn("Totally input $" + totalInput);
            LOGGER.warn("Portfolio costs $" + portfolioPrice);
            BigDecimal resultWithInflation = portfolioPrice.divide(es.getInflation(), 0, RoundingMode.FLOOR);
            LOGGER.warn("You can buy " + resultWithInflation + " standard buckets of food");
            LOGGER.warn("Coefficient is " + resultWithInflation.divide(new BigDecimal(12 * year * 10), 2, RoundingMode.FLOOR));
            LOGGER.warn("*****************************");
            LOGGER.warn("\n");

            ts.setCurrentDate(startDate);
            ts.passMonths(1);

        }
    }

//    private static void drawGraphics() {
//        DataReader dr = new FileDataReader();
//        TreeMap<Date, Double> spx = dr.readStocks();
//        TreeMap<Date, Double> infl = dr.readInfl();
//
//        Plot2DPanel plot = new Plot2DPanel();
//
//        double spxX[] = new double[spx.size()];
//        double spxY[] = new double[spx.size()];
//        int i = 0;
//        for (Map.Entry<Date, Double> entry : spx.entrySet()) {
//            spxX[i] = date2Long(entry.getKey());
//            spxY[i] = entry.getValue();
//            i++;
//        }
//        plot.addLinePlot("spx", spxX, spxY);
//
//        double infX[] = new double[infl.size()];
//        double infY[] = new double[infl.size()];
//        i = 0;
//        for (Map.Entry<Date, Double> entry : infl.entrySet()) {
//            infX[i] = date2Long(entry.getKey());
//            infY[i] = entry.getValue();
//            i++;
//        }
//        plot.addLinePlot("infl", infX, infY);
//
//
//        JFrame frame = new JFrame("a plot panel");
//        frame.setContentPane(plot);
//        frame.setVisible(true);
//        frame.setSize(new Dimension(800,600));
//    }

    private static Date nextMonth(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Date.from(localDate.plusMonths(1L).atStartOfDay(ZoneId.systemDefault()).toInstant());

    }

    private static Date lastYear(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Date.from(localDate.minusYears(1L).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static Long date2Long(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate zeroDate = LocalDate.of(1913, 1, 1);
        return zeroDate.until(localDate, MONTHS);
    }

    private static Date Long2Date(Long i) {
        LocalDate zeroDate = LocalDate.of(1913, 1, 1);
        return Date.from(zeroDate.plusMonths(i).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static void plotPoint(Plot2DPanel panel, double x, double y) {

        double halfSize = 1;

        double[] xx = {x - halfSize, x + halfSize, x + halfSize, x - halfSize, x - halfSize};
        double[] yy = {y - halfSize, y - halfSize, y + halfSize, y + halfSize, y - halfSize};

        panel.addLinePlot("my plot", xx, yy);

    }
}
