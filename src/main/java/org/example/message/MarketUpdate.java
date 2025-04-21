package org.example.message;

import org.example.actor.TraderActor;
import org.example.protocol.Quote;

public class MarketUpdate implements TraderActor.Command{

    private Quote quote;

    public MarketUpdate(Quote quote) {
        this.quote = quote;
    }

    public Quote getQuote() {
        return quote;
    }

    public void setQuote(Quote quote) {
        this.quote = quote;
    }

    @Override
    public String toString() {
        return "MarketUpdate{" +
                "quote=" + quote +
                '}';
    }
}
