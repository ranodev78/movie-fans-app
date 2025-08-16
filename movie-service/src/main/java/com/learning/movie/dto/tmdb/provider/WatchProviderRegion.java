package com.learning.movie.dto.tmdb.provider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WatchProviderRegion {
    private String link;
    private List<WatchProvider> flatrate;
    private List<WatchProvider> rent;
    private List<WatchProvider> buy;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public List<WatchProvider> getFlatrate() {
        return flatrate;
    }

    public void setFlatrate(List<WatchProvider> flatrate) {
        this.flatrate = flatrate;
    }

    public List<WatchProvider> getRent() {
        return rent;
    }

    public void setRent(List<WatchProvider> rent) {
        this.rent = rent;
    }

    public List<WatchProvider> getBuy() {
        return buy;
    }

    public void setBuy(List<WatchProvider> buy) {
        this.buy = buy;
    }
}
