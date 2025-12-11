package com.example.appstore.views;

public interface Searchable {
    void onSearch(String query);

    default void onFilter(String platform) {}
}
