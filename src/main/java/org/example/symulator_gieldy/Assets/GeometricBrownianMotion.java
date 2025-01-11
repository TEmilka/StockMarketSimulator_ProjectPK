package org.example.symulator_gieldy.Assets;

import java.util.Random;

public class GeometricBrownianMotion {

    private static final Random random = new Random();

    public static double calculate(double currentPrice, double drift, double volatility) {

        double randomShock = random.nextGaussian(); // Losowa wartość z rozkładu normalnego
        return currentPrice * Math.exp(drift - 0.5 * Math.pow(volatility, 2) + volatility * randomShock);
    }
}
