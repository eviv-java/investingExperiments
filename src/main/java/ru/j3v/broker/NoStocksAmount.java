package ru.j3v.broker;

public class NoStocksAmount extends Exception {
    public NoStocksAmount(String s) {
        super(s);
    }

    public NoStocksAmount(Throwable cause) {
        super(cause);
    }
}
