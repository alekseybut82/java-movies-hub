package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;

public class SingleMovieHandler extends BaseHttpHandler {

    public SingleMovieHandler(MoviesStore store) {
        super(store);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

    }
}
