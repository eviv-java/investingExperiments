package ru.j3v.broker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.j3v.io.ParamsProvider;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static java.time.temporal.ChronoUnit.MONTHS;


@Service
public class TimeService {

    private Date currentDate;
    private ParamsProvider paramsProvider;

    @Autowired
    public TimeService(ParamsProvider paramsProvider) {
        this.paramsProvider = paramsProvider;
        this.currentDate = this.paramsProvider.initialDate();
    }

    public Date getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }

    public Date passOneMonth() {
        return passMonths(1L);
    }

    public Date passMonths(long m) {
        LocalDate localDate = currentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        currentDate = Date.from(localDate.plusMonths(m).atStartOfDay(ZoneId.systemDefault()).toInstant());
        return currentDate;
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
}
