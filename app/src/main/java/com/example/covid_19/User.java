package com.example.covid_19;

public class User {
    private String name;
    private String age;
    private String gender;
    private String email;
    private String password;
    private String country;
    private String profileImage;

    public User() {} // Required for Firebase

    public User(String name, String age, String gender, String email, String password, String country, String profileImage) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.email = email;
        this.password = password;
        this.country = country;
        this.profileImage = profileImage;
    }

    public String getName() {
        return name;
    }

    public String getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getCountry() {
        return country;
    }

    public String getProfileImage() {
        return profileImage;
    }

    // Optional: Add setters if you plan to update data via setters
    public void setName(String name) {
        this.name = name;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}