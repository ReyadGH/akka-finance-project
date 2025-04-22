package org.example.message;

import org.example.actor.QuoteGeneratorActor;
import org.example.mdo.Quote;

public class ProduceQuote implements QuoteGeneratorActor.Command {

    private Quote quote;

    public ProduceQuote() {
    }

    public ProduceQuote(Quote quote) {
        this.quote = quote;
    }

    public Quote getQuote() {
        return quote;
    }

    public void setQuote(Quote quote) {
        this.quote = quote;
    }
}
