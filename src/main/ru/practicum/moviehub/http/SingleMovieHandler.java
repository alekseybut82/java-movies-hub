package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SingleMovieHandler extends BaseHttpHandler {

    public SingleMovieHandler(MoviesStore store) {
        super(store);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();

        switch (method.toUpperCase()) {
            case "GET" -> handleGetRequest(httpExchange);
            case "DELETE" -> handleDeleteRequest(httpExchange);
            default -> defaultRequest(httpExchange);
        }
    }

    private void handleGetRequest(HttpExchange httpExchange) throws IOException {
        String[] pathParts = httpExchange.getRequestURI().getPath().split("/");

        if (pathParts.length != 3) sendErrorResponse(httpExchange, "Ошибка в path запроса",
                List.of("Ресурс по указанному пути отсутствует"), 404);

        Optional<Integer> movieIdOpt = getMoviesID(pathParts);

        if (movieIdOpt.isPresent()) {
            Integer movieId = movieIdOpt.get();
            Movie movie =  store.getMovie(movieId);
            if (movie == null) {
                sendErrorResponse(httpExchange,"Фильм не найден", List.of(), 404);
                return;
            }
            String response = new Gson().toJson(Map.of("id", movieId, "movie", movie));
            sendResponse(httpExchange, response, 200);
        } else sendErrorResponse(httpExchange,"Ошибка в параметре запроса",
                List.of("Некорректный идентификатор кино"), 400);
    }

    private void defaultRequest(HttpExchange httpExchange) throws IOException {
        sendErrorResponse(httpExchange,"Метод запроса не поддерживается",
                List.of(), 405);
    }

    private void handleDeleteRequest(HttpExchange httpExchange) throws IOException {
        String[] pathParts = httpExchange.getRequestURI().getPath().split("/");

        if (pathParts.length != 3) sendErrorResponse(httpExchange, "Ошибка в path запроса",
                List.of("Ресурс по указанному пути отсутствует"), 404);

        Optional<Integer> movieIdOpt = getMoviesID(pathParts);

        if (movieIdOpt.isPresent()) {
            if (store.getMoviesMap().remove(movieIdOpt.get()) == null) {
                sendErrorResponse(httpExchange,"Фильм не найден", List.of(), 404);
            }
            sendResponse(httpExchange, "", 204);
        } else sendErrorResponse(httpExchange,"Ошибка в параметре запроса",
                List.of("Некорректный идентификатор кино"), 400);
    }

    public Optional<Integer> getMoviesID(String[] pathParts) {
        try {
            return Optional.of(Integer.parseInt(pathParts[2]));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }

    }

}
