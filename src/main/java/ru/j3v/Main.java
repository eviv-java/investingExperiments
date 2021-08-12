package ru.j3v;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j3v.broker.BrokerAccount;
import ru.j3v.broker.ExchangeService;
import ru.j3v.broker.NoCashException;
import ru.j3v.broker.TimeService;
import ru.j3v.io.DataReader;
import ru.j3v.io.FileDataReader;
import org.math.plot.Plot2DPanel;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import org.springframework.context.*;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static java.time.temporal.ChronoUnit.MONTHS;

public class Main {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private BrokerAccount ba;
    private ExchangeService es;
    private TimeService ts;

    public Main(ApplicationContext context) {
        ba = (BrokerAccount)context.getBean("brokerAccount");
        es = (ExchangeService)context.getBean("exchangeService");
        ts = (TimeService)context.getBean("timeService");
    }

    public static void main(String[] args) throws Exception {

        ApplicationContext context = new ClassPathXmlApplicationContext("context.xml");
        for (int year = 1; year<30; year++) {
            Triple result = new Main(context).experiment(year);
            System.out.println("Investing for " + year + " years:");
            System.out.println("Minimal coeficient: " + result.a);
            System.out.println("Maxumal coeficient: " + result.b);
            System.out.println("Average coeficient: " + result.c);
            System.out.println();
        }
    }

    private Triple experiment(int year) throws NoCashException {
        ts.setCurrentDate(new Date(13, 1, 1));
        LocalDate limitDate = new Date(121, 01, 01).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        limitDate = limitDate.minusYears(year);

        ArrayList<Double> coefs = new ArrayList<Double>();

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
            LOGGER.info("\n");
            LOGGER.info("*****************************");
            LOGGER.info("Started in " + startDate);
            LOGGER.info("Put " + 12 * year * 10 + " standard buckets of food");
            LOGGER.info("Now it is " + ts.getCurrentDate());
            BigDecimal portfolioPrice = ba.assetAmount("SPX").multiply(es.getPrice("SPX")).add(ba.currencyAmount("USD"));
            LOGGER.info("Totally input $" + totalInput);
            LOGGER.info("Portfolio costs $" + portfolioPrice);
            BigDecimal resultWithInflation = portfolioPrice.divide(es.getInflation(), 0, RoundingMode.FLOOR);
            LOGGER.info("You can buy " + resultWithInflation + " standard buckets of food");
            BigDecimal coefficient = resultWithInflation.divide(new BigDecimal(12 * year * 10), 2, RoundingMode.FLOOR);
            LOGGER.info("Coefficient is " + coefficient);
            LOGGER.info("*****************************");
            LOGGER.info("\n");

            coefs.add(coefficient.doubleValue());

            ts.setCurrentDate(startDate);
            ts.passMonths(1);

        }
        Collections.sort(coefs);
        return new Triple(coefs.get(0), coefs.get(coefs.size() - 1), coefs.get(coefs.size()/2));
    }

    class Triple {
        double a;
        double b;
        double c;

        public Triple(double a, double b, double c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public double getA() {
            return a;
        }

        public void setA(double a) {
            this.a = a;
        }

        public double getB() {
            return b;
        }

        public void setB(double b) {
            this.b = b;
        }

        public double getC() {
            return c;
        }

        public void setC(double c) {
            this.c = c;
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
