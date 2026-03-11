package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;

public class MoviesHandler extends BaseHttpHandler {
    
    public MoviesHandler(MoviesStore store) {
        super(store);
    }


    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        switch (method.toUpperCase()) {
            case "GET" -> handleGetRequest(httpExchange);
            case "POST" -> handlePostRequest(httpExchange);
            default -> defaultGetRequest(httpExchange);
        }
    }

    private void defaultGetRequest(HttpExchange httpExchange) {
    }

    private void handlePostRequest(HttpExchange httpExchange) {
    }

    private void handleGetRequest(HttpExchange httpExchange)  {
        String response = new Gson().toJson(store.getMoviesMap().entrySet().toArray());
        sendResponse(httpExchange, response, 200);
//        sendResponse(HttpExchange , String stringResponse, int statusCode)
    }
}
