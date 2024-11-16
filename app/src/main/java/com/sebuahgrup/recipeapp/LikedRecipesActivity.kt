package com.sebuahgrup.recipeapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.sebuahgrup.recipeapp.adapter.ListRecipesAdapter
import com.sebuahgrup.recipeapp.model.Recipes
import com.sebuahgrup.recipeapp.model.User

class LikedRecipesActivity : AppCompatActivity() {
    private lateinit var homeButton : ImageButton
    private lateinit var listButton : ImageButton
    private lateinit var likedRecipesButton : ImageButton
    private lateinit var actionRecipesButton : ImageButton
    private lateinit var profileButton : ImageButton
    private lateinit var userGreetings : TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var likedRecipesAdapter: ListRecipesAdapter
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private val likedRecipesList: MutableList<Recipes> = mutableListOf()
    private var isBackToHome = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_liked_recipes)

        //initialize button
        homeButton = findViewById(R.id.liked_navigation_home_button)
        listButton = findViewById(R.id.liked_navigation_list_recipes_button)
        likedRecipesButton = findViewById(R.id.liked_navigation_liked_recipes_button)
        actionRecipesButton = findViewById(R.id.liked_navigation_edit_recipes_button)
        profileButton = findViewById(R.id.liked_navigation_account_button)
        userGreetings = findViewById(R.id.liked_recipes_greetings_user_label)
        recyclerView = findViewById(R.id.liked_recycle_view_recipes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Adapter
        likedRecipesAdapter = ListRecipesAdapter(
            likedRecipesList,
            onItemClick = { recipe ->
                // Handle item click
                val intent = Intent(this, VIewRecipesActivity::class.java)
                intent.putExtra("recipe_id", recipe.id)
                startActivity(intent)
            },
            onLikeClick = { recipe ->
                handleLikeClick(recipe)
            }
        )
        recyclerView.adapter = likedRecipesAdapter
        recyclerView = findViewById(R.id.liked_recycle_view_recipes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        auth = FirebaseAuth.getInstance()

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
        displayUser()
        loadLikedRecipes()
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
    private fun handleLikeClick(recipe: Recipes) {
        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid != null) {
            val recipeRef = db.collection("recipes").document(recipe.id)
            if (recipe.likedBy.contains(currentUserUid)) {
                // Remove UID from likedBy
                recipeRef.update("likedBy", FieldValue.arrayRemove(currentUserUid))
                    .addOnSuccessListener {
                        Toast.makeText(this, "Unliked", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Add UID to likedBy
                recipeRef.update("likedBy", FieldValue.arrayUnion(currentUserUid))
                    .addOnSuccessListener {
                        Toast.makeText(this, "Liked", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
    //function to display current user name in user greetings label
    private fun displayUser() {
        val uid = auth.currentUser?.uid
        if (uid != null){
            getUserName(uid)
        }else{
            Toast.makeText(this, "User Belum Login", Toast.LENGTH_SHORT).show()
        }
    }
    //function to get current user login name
    private fun getUserName(uid: String) {
        val db = FirebaseDatabase.getInstance().getReference("users")
        db.child(uid).get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            if (user != null) {
                userGreetings.text = user.name
            } else {
                Toast.makeText(this, "Data pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal mengambil data pengguna: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun loadLikedRecipes() {
        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid == null) {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("recipes")
            .whereArrayContains("likedBy", currentUserUid)
            .get()
            .addOnSuccessListener { result ->
                likedRecipesList.clear()
                for (document in result) {
                    val recipe = document.toObject(Recipes::class.java)
                    likedRecipesList.add(recipe)
                }
                likedRecipesAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}