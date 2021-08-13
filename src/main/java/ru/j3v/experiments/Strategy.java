package ru.j3v.experiments;

import ru.j3v.broker.NoCashException;

public interface Strategy {
    AccumulateAndHoldStrategy.Triple experiment(int year) throws NoCashException;

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
}
