package com.sebuahgrup.recipeapp.model

data class Recipes (
    val id : String = "",
    val creatorName : String = "",
    val creatorUid : String = "",
    val recipesName : String = "",
    val typeRecipes : String = "",
    val ingredientsRecipes : String = "",
    val stepRecipes : String = "",
    val imageRecipes : String = "",
    val likedBy: List<String> = emptyList()
)