package com.example.covid_19;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @GET("v3/covid-19/countries/{country}")
    Call<CovidData> getCountryData(@Path("country") String country);

    @GET("v3/covid-19/countries")
    Call<List<CovidData>> getAllCountries();
}
