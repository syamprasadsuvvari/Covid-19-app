package com.example.covid_19;

import com.google.gson.annotations.SerializedName;

public class CovidData {
    private long cases;
    private long deaths;
    private long population;
    private long tests;
    private String country;

    @SerializedName("countryInfo")
    private CountryInfo countryInfo;

    public long getCases() {
        return cases;
    }

    public long getDeaths() {
        return deaths;
    }

    public long getPopulation() {
        return population;
    }

    public long getTests() {
        return tests;
    }

    public String getCountry() {
        return country;
    }

    public CountryInfo getCountryInfo() {
        return countryInfo;
    }

    public class CountryInfo {
        private double lat;

        @SerializedName("long")
        private double longitude;

        private String flag;

        public double getLat() {
            return lat;
        }

        public double getLong() {
            return longitude;
        }

        public String getFlag() {
            return flag;
        }
    }
}
