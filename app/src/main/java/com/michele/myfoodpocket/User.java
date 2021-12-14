package com.michele.myfoodpocket;

public class User {
    public String email, sex, height, weight, birthDate; // Necessario farle public altrimenti non
                                                         // posso scriverle su Firebase

    public User() {}

    public User(String email, String sex, String height, String weight, String birthDate) {
        this.email = email;
        this.sex = sex;
        this.height = height;
        this.weight = weight;
        this.birthDate = birthDate;
    }

    public String toString() {
        return email + ", " + sex + ", " + height + ", " + weight + ", " + birthDate;
    }

    public String getEmail() {
        return this.email;
    }

    public String getSex() {
        return this.sex;
    }

    public String getHeight() {
        return this.height;
    }

    public String getWeight() {
        return this.weight;
    }

    public String getBirthDate() {
        return this.birthDate;
    }
}
