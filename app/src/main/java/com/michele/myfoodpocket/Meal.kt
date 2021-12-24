package com.michele.myfoodpocket

class Meal {
    private var emailDate : String = "";
    private var category : String = "";
    private var description : String = "";
    private var calories : Int = 0; // Calorie provvisorie che verranno poi sostituite con quelle del pasto
    private var photoPath : String = "";

    constructor() {}

    constructor(emailDate : String, category : String, description: String, calories : Int, photoPath : String) {
        this.emailDate = emailDate;
        this.category = category;
        this.description = description;
        this.calories = calories;
        this.photoPath = photoPath;
    }

    override fun toString() : String {
        return this.emailDate + " , " + this.category + " , " + this.description + " , " + this.calories;
    }

    fun getEmailDate() : String {
        return this.emailDate;
    }

    fun setEmailDate() : Void? {
        this.emailDate = emailDate;
        return null;
    }

    fun getCategory() : String {
        return this.category;
    }

    fun setCategory(category: String) : Void? {
        this.category = category;
        return null;
    }

    fun getDescription() : String {
        return this.description;
    }

    fun setDescription(description : String) : Void? {
        this.description = description;
        return null;
    }

    fun getCalories() : Int {
        return this.calories;
    }

    fun setCalories(calories : Int) : Void? {
        this.calories = calories;
        return null;
    }

    fun getPhotoPath() : String {
        return this.photoPath;
    }

    fun setPhotoPath(description : String) : Void? {
        this.photoPath = photoPath;
        return null;
    }
}