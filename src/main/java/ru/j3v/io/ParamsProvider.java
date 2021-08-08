package ru.j3v.io;

import java.util.Date;

public interface ParamsProvider {
    Date initialDate();
    Double brokerCommission();
    Double taxRate();
}
