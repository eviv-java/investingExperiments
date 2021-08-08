package ru.j3v.broker;

public class NoCashException extends Exception {
    public NoCashException(String s) {
        super(s);
    }

    public NoCashException(Exception e) {
        super(e);
    }
}
