package org.example.symulator_gieldy.Price;

import java.awt.*;

public class PriceContext {
    private PriceState state;

    public PriceContext() {
        this.state = new PriceStartState();
    }
    public void setState(PriceState state) {
        this.state = state;
    }
    public Color getColor(){
        return state.getColor();
    }
}
