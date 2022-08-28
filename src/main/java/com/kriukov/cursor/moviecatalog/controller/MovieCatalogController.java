package com.kriukov.cursor.moviecatalog.controller;

import com.kriukov.cursor.moviecatalog.entity.Movie;
import com.kriukov.cursor.moviecatalog.entity.MovieInfo;
import com.kriukov.cursor.moviecatalog.entity.Rating;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/catalog")
public class MovieCatalogController {

    @Value("${application.rating.endpoint}")
    private String ratingUrl;

    @Value("${application.movieinfo.endpoint}")
    private String movieInfoUrl;


    private final RestTemplate restTemplate;
    private final WebClient webClient;

    @GetMapping("/{userId}")
    public ResponseEntity<List<Movie>> getCatalogForUser(@PathVariable String userId){

        Rating[] ratings =
                restTemplate.getForEntity(URI.create(ratingUrl + userId), Rating[].class).getBody();

        return ResponseEntity.ok(Arrays.stream(ratings)
                .map(rating -> {
                    MovieInfo movieInfo = webClient.get()
                            .uri(movieInfoUrl + rating.getMovieId())
                            .retrieve()
                            .bodyToMono(MovieInfo.class).block();
                    return new Movie(rating.getMovieId(), movieInfo.getName(), rating.getRating());
                })
                .collect(Collectors.toList()));
    }
}
