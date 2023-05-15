package com.example.weatherappv2;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherAPI {
    @GET("weather?appid=7d6035a012bd4574b16d3e6e76d724e2&units=metric")
    Call<OpenWeatherMap> getWeatherWithLocation(
            @Query("lat")double lat,
            @Query("lon")double lon
    );
    @GET("weather?appid=7d6035a012bd4574b16d3e6e76d724e2&units=metric")
    Call<OpenWeatherMap> getWeatherWithName(@Query("q")String name);
}