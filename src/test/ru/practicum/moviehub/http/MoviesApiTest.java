package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesApiTest {

    private static final String LOCALHOST_8080_MOVIES = "http://localhost:8080/movies";

    private static MoviesStore store;
    private static MoviesServer server;

    public static HttpResponse<String> sendGetRequest(String requestURL) throws Exception  {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestURL))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private HttpResponse<String> sendPostRequest(String requestURL, String request) throws Exception {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(requestURL))
                .headers("Content-Type","application/json")
                .POST(HttpRequest.BodyPublishers.ofString(request))
                .build();

        return client.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    public static HttpResponse<String> sendDeleteRequest(String requestURL) throws Exception  {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestURL))
                .DELETE()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    public boolean isEqualJson(String str1, String str2) {
        JsonParser parser = new JsonParser();
        JsonElement t1 = parser.parse(str1);
        JsonElement t2 = parser.parse(str2);
        return t1.equals(t2);
    }

    @BeforeAll
    public static void beforeAll() {
        store = new MoviesStore();
        server = new MoviesServer(store, 8080);
        server.start();
    }

    @BeforeEach
    void beforeEach() {
        store.clearMoviesMap();
    }

    @Test
    public void getMovies_whenEmpty_returnsEmptyArray() throws Exception {

        HttpResponse<String> response = sendGetRequest(LOCALHOST_8080_MOVIES);

        assertEquals(200, response.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
        "Content-Type должен содержать формат данных и кодировку");

        String body = response.body().trim();
        assertTrue(body.equals("[]"),
                "Ожидается пустой JSON-массив");
    }

    @Test
    public void getMovies_whenNotEmpty_returnsCorrectArray() throws Exception {
        store.addMovie(new Movie("A",2001));
        store.addMovie(new Movie("B",2002));
        store.addMovie(new Movie("C",2003));

        HttpResponse<String> response = sendGetRequest(LOCALHOST_8080_MOVIES);

        assertEquals(200, response.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = response.body().trim();
        assertTrue(isEqualJson(body,
                        "[{\"id\":1,\"movie\":{\"title\":\"A\",\"year\":2001}},{\"id\":2,\"movie\":{\"title\":\"B\",\"year\":2002}},{\"id\":3,\"movie\":{\"title\":\"C\",\"year\":2003}}]"),
                "Ожидается JSON-массив из 3-ех элементов");
    }

    @Test
    public void getMovieByIdInPath_whenIdIsPresent_returnMovies() throws Exception {
        store.addMovie(new Movie("A",2001));
        store.addMovie(new Movie("B",2002));
        store.addMovie(new Movie("C",2003));

        HttpResponse<String> response = sendGetRequest(LOCALHOST_8080_MOVIES + "/3");

        assertEquals(200, response.statusCode(), "GET /movies/3 должен вернуть 200");

        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = response.body().trim();
        assertTrue(isEqualJson(body, "{\"id\":3,\"movie\":{\"title\":\"C\",\"year\":2003}}"),
                "Ожидается 1 элемент JSON-массива");
    }

    @Test
    public void getMovieByIdInPath_whenIdIsNotPresent_return404() throws Exception {
        store.addMovie(new Movie("A",2001));
        store.addMovie(new Movie("B",2002));
        store.addMovie(new Movie("C",2003));

        HttpResponse<String> response = sendGetRequest(LOCALHOST_8080_MOVIES + "/100");

        assertEquals(404, response.statusCode(), "GET /movies/100 должен вернуть 404");

        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = response.body().trim();
        assertTrue(body.contains("Фильм не найден"), "Фильма с таким идентификатором нет");

    }

    @Test
    public void getMovieByIdInPath_whenIdIsNotInteger_return400() throws Exception {
        HttpResponse<String> response = sendGetRequest(LOCALHOST_8080_MOVIES + "/abracadabra");

        assertEquals(400, response.statusCode(), "GET /movies/abracadabra должен вернуть 400");

        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = response.body().trim();
        assertTrue(body.contains("Некорректный идентификатор кино"), "Фильма с таким идентификатором нет");
    }

    @Test
    public void deleteMovieByIdInPath_whenIdIsNotPresent_return404() throws Exception {
        store.addMovie(new Movie("A",2001));
        store.addMovie(new Movie("B",2002));
        store.addMovie(new Movie("C",2003));

        HttpResponse<String> response = sendDeleteRequest(LOCALHOST_8080_MOVIES + "/100");

        assertEquals(404, response.statusCode(), "DELETE /movies/100 должен вернуть 404");

        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = response.body().trim();
        assertTrue(body.contains("Фильм не найден"), "Фильма с таким идентификатором нет");
    }

    @Test
    public void deleteMovieByIdInPath_whenIdIsPresent_return204() throws Exception {
        store.addMovie(new Movie("A",2001));
        store.addMovie(new Movie("B",2002));
        store.addMovie(new Movie("C",2003));

        HttpResponse<String> response = sendDeleteRequest(LOCALHOST_8080_MOVIES + "/3");

        assertEquals(204, response.statusCode(), "DELETE /movies/3 должен вернуть 204");

        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = response.body().trim();
        assertTrue(store.getMoviesMap().size() == 2, "Количество фильмов уменьшилось");
        assertTrue(store.getMoviesMap().get(3) == null, "Удаленного фильма нет хранилище");
    }

    @Test
    public void deleteMovieByIdInPath_whenIdIsNotInteger_return400() throws Exception {
        HttpResponse<String> response = sendDeleteRequest(LOCALHOST_8080_MOVIES + "/abracadabra");

        assertEquals(400, response.statusCode(), "DELETE /movies/abracadabra должен вернуть 400");

        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = response.body().trim();
        assertTrue(body.contains("Некорректный идентификатор кино"), "Фильма с таким идентификатором нет");
    }

    @Test
    public void filterMovieByYearInQueryParams_whenYearIsPresent_return200() throws Exception {
        store.addMovie(new Movie("A",2001));
        store.addMovie(new Movie("B",2002));
        store.addMovie(new Movie("C",2002));

        HttpResponse<String> response = sendGetRequest(LOCALHOST_8080_MOVIES + "?year=2002");

        assertEquals(200, response.statusCode(), "GET /movies... должен вернуть 200");

        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = response.body().trim();
        assertTrue(isEqualJson(body,
                        "[{\"id\":2,\"movie\":{\"title\":\"B\",\"year\":2002}},{\"id\":3,\"movie\":{\"title\":\"C\",\"year\":2002}}]"),
                "Ожидается JSON-массив из 2-ух элементов");
    }

    @Test
    public void filterMovieByYearInQueryParams_whenIdIsNotPresent_return400() throws Exception {

        HttpResponse<String> response = sendGetRequest(LOCALHOST_8080_MOVIES + "?year=YUln");

        assertEquals(400, response.statusCode(), "GET /movies... должен вернуть 400");

        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = response.body().trim();
        assertTrue(body.contains("Некорректный параметр запроса — year"),
                "Год не является числом от 0 до 2027");
    }

    @Test
    public void addMovie_whenSuccesses_return201() throws Exception {
        Movie movie = new Movie("B", 2002);

        HttpResponse<String> response = sendPostRequest(LOCALHOST_8080_MOVIES,
                new Gson().toJson(movie));

        assertEquals(201, response.statusCode(), "POST /movies... должен вернуть 201");

        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = response.body().trim();
        assertTrue(store.getMovie(1).equals(movie));
        assertTrue(isEqualJson(body,
                        "{\"id\":1,\"movie\":{\"title\":\"B\",\"year\":2002}}"),
                "Ожидается JSON-массив из 2-ух элементов");
    }

    @Test
    public void addMovie_whenVadationCheckFail_return422() throws Exception {

        HttpResponse<String> response = sendPostRequest(LOCALHOST_8080_MOVIES,
                "{\"title\":\"  \",\"year\":168}");

        assertEquals(422, response.statusCode(), "POST /movies... должен вернуть 422");

        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = response.body().trim();
        assertTrue(body.contains("title не может быть пустой строкой") ||
                body.contains("year не может быть меньше 1888"));
    }

    @Test
    public void addMovie_whenJsonIncorrect_return400() throws Exception {

        HttpResponse<String> response = sendPostRequest(LOCALHOST_8080_MOVIES,
                "{\"title\":\"  \",\"year\":}");

        assertEquals(400, response.statusCode(), "POST /movies... должен вернуть 400");

        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = response.body().trim();
        assertTrue(body.contains("Некорректный JSON"));
    }

    @Test
    public void used_notAllowHttpMethod_ForMoviesCollectionEndpoint_return405() throws Exception {

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LOCALHOST_8080_MOVIES))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(405, response.statusCode(), "PUT /movies... должен вернуть 405");

        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = response.body().trim();
        assertTrue(body.contains("Метод запроса не поддерживается"), "Поддерживается только GET/POST");
    }

    @Test
    public void used_notAllowHttpMethod_ForMoviesResourceEndpoint_return405() throws Exception {

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LOCALHOST_8080_MOVIES + "/"))
                .PUT(HttpRequest.BodyPublishers.ofString("не важно что"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(405, response.statusCode(), "PUT /movies... должен вернуть 405");

        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = response.body().trim();
        assertTrue(body.contains("Метод запроса не поддерживается"), "Поддерживается только GET/POST");
    }

    @AfterAll
    public static void afterAll() {
        if (server != null) server.stop();
    }
}

