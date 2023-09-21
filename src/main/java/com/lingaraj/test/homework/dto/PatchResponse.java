package com.lingaraj.test.homework.dto;

public class PatchResponse {
    String message;
    String cause;
    UserDto recipe;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public UserDto getRecipe() {
        return recipe;
    }

    public void setRecipe(UserDto recipe) {
        this.recipe = recipe;
    }

}
