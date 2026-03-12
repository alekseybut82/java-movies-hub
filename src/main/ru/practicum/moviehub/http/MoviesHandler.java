package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.*;

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

    private void defaultGetRequest(HttpExchange httpExchange) throws IOException {
        sendErrorResponse(httpExchange,"Метод запроса не поддерживается", List.of(), 405);
    }

    private void handleGetRequest(HttpExchange httpExchange) throws IOException {
        Map<String, String> quesryParams = getQueryParmas(httpExchange.getRequestURI().getQuery());
        String strYear = quesryParams.get("year");
        final boolean needFiter;
        final int year;

        if (strYear != null) {
            try {
                year = Integer.parseInt(strYear);
                needFiter = true;
            } catch (NumberFormatException exception) {
                sendErrorResponse(httpExchange, "Некорректный параметр запроса — year", List.of(), 400);
                return;
            }
        } else {
            needFiter = false;
            year = -1;
        }

        String response = new Gson().toJson(store
                .getMoviesMap()
                .entrySet()
                .stream()
                .filter(entry -> !needFiter || entry.getValue().getYear() == year)
                .map(entry -> Map.of("id", entry.getKey(), "movie", entry.getValue()))
                .toList());

        sendResponse(httpExchange, response, 200);
    }

    private Map<String, String> getQueryParmas(String query) {
        if (query == null) return Map.of();

        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            }
        }
        return result;
    }

    private void handlePostRequest(HttpExchange httpExchange) throws IOException {
        if (!httpExchange.getRequestHeaders().getFirst("Content-Type").equals("application/json"))
            sendErrorResponse(httpExchange, "Не поддерживаемый тип данных",
                    List.of("Проверьте Media Type"), 422);

        try (InputStream inputStream  = httpExchange.getRequestBody()) {
            String request = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            try {
                Movie movie = gson.fromJson(request, Movie.class);
                List<String> detailsError = validateMovie(movie);

                if (!detailsError.isEmpty()) {
                    sendErrorResponse(httpExchange, "Ошибка валидации", detailsError, 422);
                    return;
                }

                int key = store.addMovie(movie);

                String response = new Gson().toJson(Map.of("id", key, "movie", movie));
                sendResponse(httpExchange, response, 201);

            } catch (JsonSyntaxException e) {
                // Ошибка десериализации (некорректный JSON)
                sendErrorResponse(httpExchange, "Некорректный JSON", List.of(e.getMessage()), 400);
            }

        } catch (IOException e) {
            sendErrorResponse(httpExchange, "Ошибка чтения запроса", List.of(e.getMessage()), 500);
        }

    }

    private List<String> validateMovie(Movie movie) {
        List<String> errors = new ArrayList<>();
        String title = movie.getTitle();

        if (title == null) {
            errors.add("title не может быть null");
        } else {
            if (title.strip().isEmpty()) {
                errors.add("title не может быть пустой строкой");
            } else if (title.length() > 100) {
                errors.add("title не может превышать 100 символов");
            }
        }

        int currentYear = Year.now().getValue();
        int year = movie.getYear();
        if (year < 1888) {
            errors.add("year не может быть меньше 1888 (год самого раннего из сохранившихся фильмов)");
        } else if (year > currentYear + 1) {
            errors.add("year не может быть больше текущего года + 1");
        }

        return errors;
    }

}
