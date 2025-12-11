package com.example.appstore.model;

import java.time.ZonedDateTime;

/**
 * App model matching the backend API response.
 */
public class App {
    private String id;
    private String name;
    private String owner_login;
    private String category;
    private String description;
    private String created_at;
    private String updated_at;
    
    public App() {}
    
    public App(String id, String name, String ownerLogin, String category, String description) {
        this.id = id;
        this.name = name;
        this.owner_login = ownerLogin;
        this.category = category;
        this.description = description;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getOwnerLogin() { return owner_login; }
    public void setOwnerLogin(String ownerLogin) { this.owner_login = ownerLogin; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCreatedAt() { return created_at; }
    public void setCreatedAt(String createdAt) { this.created_at = createdAt; }
    
    public String getUpdatedAt() { return updated_at; }
    public void setUpdatedAt(String updatedAt) { this.updated_at = updatedAt; }
    
    @Override
    public String toString() {
        return "App{id='" + id + "', name='" + name + "', owner='" + owner_login + "'}";
    }
}
