package com.michele.myfoodpocket

class User {
    private var email : String = "";
    private var sex : Int = 0; // Sesso provvisorio che verrà poi sostituito con quello dell'utente
    private var height : Int = 0; // Altezza provvisoria che verrà poi sostituita con l'altezza dell'utente
    private var weight : Float = 0.0f; // Peso provvisorio che verrà poi sostituito con il peso dell'utente
    private var birthDate : String = "";
    private var workHeaviness : Int = 0; // Pesantezza del lavoro provvisoria che poi verrà sostituita con quella dell'utente
    private var sportPracticed : Boolean = false; // Attività fisica sportiva provvisoria che poi verrà sostituita con quella dell'utente

    constructor() {}

    constructor(email : String, sex : Int, height : Int, weight : Float, birthDate : String, workHeaviness : Int, sportPracticed : Boolean) {
        this.email = email;
        this.sex = sex;
        this.height = height;
        this.weight = weight;
        this.birthDate = birthDate;
        this.workHeaviness = workHeaviness;
        this.sportPracticed = sportPracticed;
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

    fun getWorkHeaviness() : Int {
        return this.workHeaviness;
    }

    fun setWorkHeaviness(workHeaviness: Int) : Void? {
        this.workHeaviness = workHeaviness;
        return null;
    }

    fun getSportPracticed() : Boolean {
        return this.sportPracticed;
    }

    fun setSportPracticed(sportPracticed: Boolean) : Void? {
        this.sportPracticed = sportPracticed;
        return null;
    }
}