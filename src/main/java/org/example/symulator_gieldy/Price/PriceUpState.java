package org.example.symulator_gieldy.Price;

import java.awt.*;

import java.io.Serializable;

public class PriceUpState implements PriceState{
    @Override
    public Color getColor() {
        int red = 0;
        int green = 50;
        int blue = 0;

        return new Color(red, green, blue);

    }
}
