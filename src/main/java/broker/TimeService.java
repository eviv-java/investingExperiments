package broker;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static java.time.temporal.ChronoUnit.MONTHS;

public class TimeService {

    private Date currentDate;

    public TimeService(Date initialDate) {
        this.currentDate = initialDate;
    }

    public Date getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }

    private Date passOneMonth() {
        LocalDate localDate = currentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        currentDate = Date.from(localDate.plusMonths(1L).atStartOfDay(ZoneId.systemDefault()).toInstant());
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
