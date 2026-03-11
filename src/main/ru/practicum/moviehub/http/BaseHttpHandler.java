package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {

    protected final MoviesStore store;

    protected final Gson gson = new Gson();

    public BaseHttpHandler(MoviesStore store) {
        this.store = store;
    }

    public void sendResponse(HttpExchange httpExchange, String stringResponse, int statusCode) {
        byte[] byteResponse = stringResponse.getBytes(StandardCharsets.UTF_8);

        try (OutputStream os = httpExchange.getResponseBody()) {
            httpExchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            httpExchange.sendResponseHeaders(statusCode, byteResponse.length);
            os.write(byteResponse);
        } catch (Exception e) {
            //
//            finally {
//            httpExchange.close();}
        }
    }

}