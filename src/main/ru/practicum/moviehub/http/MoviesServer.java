package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.logging.Handler;

public class MoviesServer {

    private final HttpServer server;

    public MoviesServer(MoviesStore store, int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port),0);
            server.createContext("/movies", new MoviesHandler(store));
            server.createContext("/movies/{id}", new SingleMovieHandler(store));
        } catch (Exception e) {
            throw new RuntimeException("Не удалось поднять http-сервер");
        }
    }

    public void start(){
        server.start();
        System.out.println("HTTP-сервер запущен!");
    }

    public void stop(){
        server.stop(0);
        System.out.println("HTTP-сервер остановлен!");
    }

}