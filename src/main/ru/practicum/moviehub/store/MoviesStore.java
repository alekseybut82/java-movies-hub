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

    public void add(Movie movie) {
        moviesMap.put(++moviesCounter, movie);
    }


    public Map<Integer, Movie> getMoviesMap() {
        return moviesMap;
    }
}