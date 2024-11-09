package com.sebuahgrup.recipeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    //initialize button from xml files
    private lateinit var listButton : Button
    private lateinit var homeButton: ImageButton
    private lateinit var likedButton : ImageButton
    private lateinit var listButtonNav : ImageButton
    private lateinit var actionButton: ImageButton
    private lateinit var profileButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        //initialize element
        listButton = findViewById(R.id.home_see_all_recipes_button)
        homeButton = findViewById(R.id.home_navigation_home_button)
        likedButton = findViewById(R.id.profile_navigation_liked_recipes_button)
        listButtonNav = findViewById(R.id.profile_navigation_list_recipes_button)
        actionButton = findViewById(R.id.profile_navigation_edit_recipes_button)
        profileButton = findViewById(R.id.profile_navigation_account_button)

        //action call page
        homeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        likedButton.setOnClickListener {
            val intent = Intent(this,LikedRecipesActivity::class.java)
            startActivity(intent)
        }
        listButton.setOnClickListener {
            val intent = Intent(this,ListRecipesActivity::class.java)
            startActivity(intent)
        }
        listButtonNav.setOnClickListener {
            val intent = Intent(this, ListRecipesActivity::class.java)
            startActivity(intent)
        }
        actionButton.setOnClickListener {
            val intent = Intent(this, ActionRecipesActivity::class.java)
            startActivity(intent)
        }
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}