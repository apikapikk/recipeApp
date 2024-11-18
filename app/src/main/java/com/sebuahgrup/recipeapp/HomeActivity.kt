package com.sebuahgrup.recipeapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.sebuahgrup.recipeapp.adapter.HomeRecipesAdapter
import com.sebuahgrup.recipeapp.model.Recipes
import com.sebuahgrup.recipeapp.model.User
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomeActivity : AppCompatActivity() {

    //make variable to initialize element
    private lateinit var listButton : Button
    private lateinit var homeButton: ImageButton
    private lateinit var likedButton : ImageButton
    private lateinit var listButtonNav : ImageButton
    private lateinit var actionButton : ImageButton
    private lateinit var profileButton : ImageButton
    private lateinit var userGreetings : TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var randomizeButton : Button
    private lateinit var auth : FirebaseAuth
    private lateinit var recipesList: MutableList<Recipes>
    private lateinit var homeRecipesAdapter: HomeRecipesAdapter
    private lateinit var searchRecipes : EditText
    private var isBackToHome = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        val yourConstraintLayout: View = findViewById(R.id.main_home_page)
        ViewCompat.setOnApplyWindowInsetsListener(yourConstraintLayout) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBarsInsets.left,
                systemBarsInsets.top,
                systemBarsInsets.right,
                systemBarsInsets.bottom
            )
            insets
        }
        //initialize element
        listButton = findViewById(R.id.home_see_all_recipes_button)
        homeButton = findViewById(R.id.home_navigation_home_button)
        likedButton = findViewById(R.id.profile_navigation_liked_recipes_button)
        listButtonNav = findViewById(R.id.profile_navigation_list_recipes_button)
        actionButton = findViewById(R.id.profile_navigation_edit_recipes_button)
        profileButton = findViewById(R.id.profile_navigation_account_button)
        userGreetings = findViewById(R.id.home_greetings_user_label)
        searchRecipes = findViewById(R.id.home_search_text)
        recipesList = mutableListOf()
        homeRecipesAdapter = HomeRecipesAdapter(recipesList) { recipe ->
            // Handle item click
            val intent = Intent(this, VIewRecipesActivity::class.java)
            intent.putExtra("recipe_id", recipe.id)  // Pass recipe ID
            intent.putExtra("author_uid", recipe.creatorUid)
            startActivity(intent)
        }
        recyclerView = findViewById(R.id.home_recycle_view_recipes)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        auth = FirebaseAuth.getInstance()
        recyclerView.adapter = homeRecipesAdapter
        randomizeButton = findViewById(R.id.home_randomize_recipes_button)

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
        randomizeButton.setOnClickListener {
            randomizeRecipes()
        }
        searchRecipes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                filterRecipes(query) // Panggil fungsi filter untuk memperbarui RecyclerView
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        //call function to display current user name in user greetings label
        displayUser()
        getRecipesFromFirestore()
        onBackPressedDispatcher.addCallback(this) {
            handlePress()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterRecipes(query: String) {
        val filteredList = recipesList.filter { recipe ->
            recipe.recipesName.contains(query, ignoreCase = true)
                    || recipe.typeRecipes.contains(query, ignoreCase = true)
                    || recipe.creatorName.contains(query, ignoreCase = true)
        }
        homeRecipesAdapter.updateList(filteredList) // Perbarui data di adapter
    }

    private fun handlePress() {
        if (isBackToHome) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            isBackToHome = true
        }
    }
   // @essLint("NotifyDataSetChanged")Suppr
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
                homeRecipesAdapter.notifyDataSetChanged() // Update RecyclerView
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting documents: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun randomizeRecipes() {
        val db = Firebase.firestore
        db.collection("recipes").get()
            .addOnSuccessListener { recipeSnapshot ->
                if (!recipeSnapshot.isEmpty) {
                    val recipeIds = recipeSnapshot.documents.map { it.id }
                    val randomId = recipeIds.random()
                    db.collection("recipes").document(randomId).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val randomRecipe = document.toObject(Recipes::class.java)
                                if (randomRecipe != null) {
                                    val intent = Intent(this, VIewRecipesActivity::class.java)
                                    intent.putExtra("recipe_id", randomId)
                                    intent.putExtra("author_uid", randomRecipe.creatorUid)
                                    startActivity(intent)
                                }
                            }
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengambil data resep: ${it.message}", Toast.LENGTH_SHORT).show()
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
    }
