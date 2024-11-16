package com.sebuahgrup.recipeapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.util.Base64
import android.graphics.BitmapFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.sebuahgrup.recipeapp.model.Recipes

class VIewRecipesActivity : AppCompatActivity() {
    private lateinit var homeButton : ImageButton
    private lateinit var listButton : ImageButton
    private lateinit var likedRecipes : ImageButton
    private lateinit var actionRecipesButton : ImageButton
    private lateinit var profileButton : ImageButton
    private lateinit var titleRecipes : TextView
    private lateinit var typeRecipes : TextView
    private lateinit var authorRecipes : TextView
    private lateinit var ingredientsRecipes : TextView
    private lateinit var stepsRecipes : TextView
    private lateinit var imageRecipes : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_recipes)

        //initialize button
        homeButton = findViewById(R.id.view_navigation_home_button)
        listButton = findViewById(R.id.view_navigation_list_recipes_button)
        likedRecipes = findViewById(R.id.view_navigation_liked_recipes_button)
        actionRecipesButton = findViewById(R.id.view_navigation_edit_recipes_button)
        profileButton = findViewById(R.id.view_navigation_account_button)
        titleRecipes = findViewById(R.id.view_recipes_name_label)
        typeRecipes = findViewById(R.id.view_recipes_type_label)
        authorRecipes = findViewById(R.id.view_recipes_user_text)
        ingredientsRecipes = findViewById(R.id.view_recipes_ingredients_recipes_text)
        stepsRecipes = findViewById(R.id.view_recipes_steps_recipes_text)
        imageRecipes = findViewById(R.id.view_recipes_image_preview)

        val recipeId = intent.getStringExtra("recipe_id")

        if (recipeId != null){
            getRecipeDetails(recipeId)
        }

        //action call Homepage
        homeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        //action call ListRecipesPage
        listButton.setOnClickListener {
            val intent = Intent(this, ListRecipesActivity::class.java)
            startActivity(intent)
        }
        //action call LikedRecipesPage
        likedRecipes.setOnClickListener {
            val intent = Intent(this, LikedRecipesActivity::class.java)
            startActivity(intent)
        }
        //action call ActionRecipesPage
        actionRecipesButton.setOnClickListener {
            val intent = Intent(this, ActionRecipesActivity::class.java)
            startActivity(intent)
        }
        //action call ProfilePage
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getRecipeDetails(recipeId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("recipes")
            .document(recipeId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val recipe = document.toObject(Recipes::class.java)
                    if (recipe != null) {
                        // Set the data to the TextViews
                        titleRecipes.text = recipe.recipesName
                        typeRecipes.text = recipe.typeRecipes
                        authorRecipes.text = recipe.creatorName
                        ingredientsRecipes.text = recipe.ingredientsRecipes
                        stepsRecipes.text = recipe.stepRecipes

                        // Decode image (if available) and set it
                        if (recipe.imageRecipes.isNotEmpty()) {
                            val decodedBytes = Base64.decode(recipe.imageRecipes, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            imageRecipes.setImageBitmap(bitmap)
                        }
                    }
                } else {
                    Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}