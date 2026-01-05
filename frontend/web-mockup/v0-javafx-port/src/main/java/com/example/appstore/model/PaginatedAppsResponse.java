package com.example.appstore.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Paginated response from the apps API endpoint.
 */
public class PaginatedAppsResponse {
    @SerializedName("apps")
    private List<App> apps;

    @SerializedName("total")
    private int total;

    @SerializedName("page")
    private int page;

    @SerializedName("page_size")
    private int pageSize;

    @SerializedName("total_pages")
    private int totalPages;

    @SerializedName("has_next")
    private boolean hasNext;

    @SerializedName("has_previous")
    private boolean hasPrevious;

    public PaginatedAppsResponse() {}

    public List<App> getApps() {
        return apps;
    }

    public void setApps(List<App> apps) {
        this.apps = apps;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
}
