package com.michele.myfoodpocket

class User {
    private var email : String = "";
    private var sex : Int = 0; // Sesso provvisorio che verrà poi sostituito con quello dell'utente
    private var height : Int = 0; // Altezza provvisoria che verrà poi sostituita con l'altezza dell'utente
    private var weight : Float = 0.0f; // peso provvisorio che verrà poi sostituito con il peso dell'utente
    private var birthDate : String = "";
    private var sportFrequency : Int = 0; // Frequenza sportiva provvisoria che poi verrà sostituita con quella dell'utente

    constructor() {}

    constructor(email : String, sex : Int, height : Int, weight : Float, birthDate : String, sportFrequency : Int) {
        this.email = email;
        this.sex = sex;
        this.height = height;
        this.weight = weight;
        this.birthDate = birthDate;
        this.sportFrequency = sportFrequency;
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

    fun getSex() : Int {
        return this.sex;
    }

    fun setSex(sex : Int) : Void? {
        this.sex = sex;
        return null;
    }

    fun getHeight() : Int {
        return this.height;
    }

    fun setHeight(height : Int) : Void? {
        this.height = height;
        return null;
    }

    fun getWeight() : Float {
        return this.weight;
    }

    fun setWeight(weight : Float) : Void? {
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

    fun getSportFrequency() : Int {
        return this.sportFrequency;
    }

    fun setSportFrequency(sportFrequency: Int) : Void? {
        this.sportFrequency = sportFrequency;
        return null;
    }
}