package com.michele.myfoodpocket

class User {
    private var email : String = "";
    private var sex : String = "";
    private var height : String = "";
    private var weight : String = "";
    private var birthDate : String = "";

    constructor() {}

    constructor(email : String, sex : String, height : String, weight : String, birthDate : String) {
        this.email = email;
        this.sex = sex;
        this.height = height;
        this.weight = weight;
        this.birthDate = birthDate;
    }

    override fun toString() : String {
        return this.email + " , " + this.sex + " , " + this.height + " , " + this.weight + " , "
         this.birthDate;
    }

    fun getEmail() : String {
        return this.email;
    }

    fun setEmail(email : String) : Void? {
        this.email = email;
        return null;
    }

    fun getSex() : String {
        return this.sex;
    }

    fun setSex(sex : String) : Void? {
        this.sex = sex;
        return null;
    }

    fun getHeight() : String {
        return this.height;
    }

    fun setHeight(height : String) : Void? {
        this.height = height;
        return null;
    }

    fun getWeight() : String {
        return this.weight;
    }

    fun setWeight(weight : String) : Void? {
        this.weight = weight;
        return null;
    }

    fun getBirthDate() : String {
        return this.birthDate;
    }

    fun setBirthDate(birthDate : String) : Void? {
        this.birthDate = birthDate;
        return null;
    }
}