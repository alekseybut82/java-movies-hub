package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.HashMap;
import java.util.Map;

public class MoviesStore {

    private int moviesCounter;
    private Map<Integer, Movie> moviesMap;

    public MoviesStore() {
        this.moviesCounter = 0;
        this.moviesMap = new HashMap<>();
    }

    public int addMovie(Movie movie) {
        int key = ++moviesCounter;
        moviesMap.put(key, movie);
        return key;
    }

    public Movie getMovie(Integer key) {
        return moviesMap.get(key);
    }

    public Map<Integer, Movie> getMoviesMap() {
        return moviesMap;
    }

    public void clearMoviesMap() {
        moviesMap.clear();
        moviesCounter = 0;
    }
}