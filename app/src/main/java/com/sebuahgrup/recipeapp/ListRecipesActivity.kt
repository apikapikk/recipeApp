package com.sebuahgrup.recipeapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.sebuahgrup.recipeapp.adapter.HomeRecipesAdapter
import com.sebuahgrup.recipeapp.adapter.ListRecipesAdapter
import com.sebuahgrup.recipeapp.model.Recipes

class ListRecipesActivity : AppCompatActivity() {
    //initialize button from xml
    private lateinit var homeButton : ImageButton
    private lateinit var listButton : ImageButton
    private lateinit var likedRecipesButton : ImageButton
    private lateinit var actionRecipesButton : ImageButton
    private lateinit var profileButton : ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var auth : FirebaseAuth
    private lateinit var recipesList: MutableList<Recipes>
    private lateinit var listRecipesAdapter: ListRecipesAdapter
    private var isBackToHome = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_list_recipes)

        //initialize button
        homeButton = findViewById(R.id.list_navigation_home_button)
        listButton = findViewById(R.id.list_navigation_list_recipes_button)
        likedRecipesButton = findViewById(R.id.list_navigation_liked_recipes_button)
        actionRecipesButton = findViewById(R.id.list_navigation_edit_recipes_button)
        profileButton = findViewById(R.id.list_navigation_account_button)
        recipesList = mutableListOf()
        listRecipesAdapter = ListRecipesAdapter(
            recipesList,
            onItemClick = { recipe ->
                // Handle item click
                val intent = Intent(this, VIewRecipesActivity::class.java)
                intent.putExtra("recipe_id", recipe.id)  // Pass recipe ID
                intent.putExtra("author_uid", recipe.creatorUid)
                startActivity(intent)
            },
            onLikeClick = { recipe ->
                // Handle like button click
                val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserUid != null) {
                    val recipeRef = FirebaseFirestore.getInstance()
                        .collection("recipes")
                        .document(recipe.id)

                    if (recipe.likedBy.contains(currentUserUid)) {
                        // Remove UID from likedBy
                        recipeRef.update("likedBy", FieldValue.arrayRemove(currentUserUid))
                    } else {
                        // Add UID to likedBy
                        recipeRef.update("likedBy", FieldValue.arrayUnion(currentUserUid))
                    }
                }
            }
        )
        recyclerView = findViewById(R.id.list_recycle_view_recipes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        auth = FirebaseAuth.getInstance()
        recyclerView.adapter = listRecipesAdapter

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
        likedRecipesButton.setOnClickListener {
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
        getRecipesFromFirestore()
        onBackPressedDispatcher.addCallback(this) {
            handlePress()
        }
    }
    private fun handlePress() {
        if (isBackToHome) {
            finishAffinity()
        } else {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            isBackToHome = true
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun getRecipesFromFirestore() {
        val db = FirebaseFirestore.getInstance()

        db.collection("recipes")
            .get()
            .addOnSuccessListener { result ->
                recipesList.clear() // Clear list before adding new data
                for (document in result) {
                    val recipe = document.toObject(Recipes::class.java)
                    recipesList.add(recipe)
                }
                listRecipesAdapter.notifyDataSetChanged() // Update RecyclerView
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting documents: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

}