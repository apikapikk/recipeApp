package com.sebuahgrup.recipeapp

import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.provider.MediaStore
import com.sebuahgrup.recipeapp.model.Recipes
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.ImageDecoder
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ArrayAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.sebuahgrup.recipeapp.model.User


class ActionRecipesActivity : AppCompatActivity() {

    //initialize element
    private lateinit var homeButton : ImageButton
    private lateinit var listButton : ImageButton
    private lateinit var likedRecipesButton : ImageButton
    private lateinit var actionRecipesButton : ImageButton
    private lateinit var profileButton : ImageButton
    private lateinit var nameRecipesText : EditText
    private lateinit var typeRecipesSpinner : Spinner
    private lateinit var ingredientsRecipesText : EditText
    private lateinit var stepRecipesText: EditText
    private lateinit var addRecipesButton : Button
    private lateinit var uploadImageButton: Button
    private lateinit var userNameLog: TextView
    private lateinit var previewImageView: ImageView
    private var imageBase64: String = ""
    private var getUserNameLog: String = ""
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_action_recipes)

        //initialize button
        homeButton = findViewById(R.id.action_navigation_home_button)
        listButton = findViewById(R.id.action_navigation_list_recipes_button)
        likedRecipesButton = findViewById(R.id.action_navigation_liked_recipes_button)
        actionRecipesButton = findViewById(R.id.action_navigation_edit_recipes_button)
        profileButton = findViewById(R.id.action_navigation_account_button)
        nameRecipesText = findViewById(R.id.action_add_name_recipes_text)
        typeRecipesSpinner = findViewById(R.id.action_add_type_spinner)
        ingredientsRecipesText = findViewById(R.id.action_add_ingredients_recipes_text)
        stepRecipesText = findViewById(R.id.action_add_steps_recipes_text)
        addRecipesButton = findViewById(R.id.action_add_recipes_button)
        uploadImageButton = findViewById(R.id.action_upload_image_button)
        userNameLog = findViewById(R.id.action_add_user_label)
        previewImageView = findViewById(R.id.action_image_preview)
        auth = FirebaseAuth.getInstance()

        //spinner data type
        val recipeTypes = arrayOf("minuman", "makanan Ringan", "makanan Berat", "pembuka","Penutup")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, recipeTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeRecipesSpinner.adapter = adapter

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
        //action to upload image
        uploadImageButton.setOnClickListener {
            uploadImageToDatabase()
        }
        //action to add recipes
        addRecipesButton.setOnClickListener {
            addRecipesToDatabase()
        }
        displayUser()
    }
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
                getUserNameLog = user.name
                userNameLog.text = user.name
            } else {
                Toast.makeText(this, "Data pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal mengambil data pengguna: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToDatabase() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getImage.launch(intent)
    }

    private val getImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri = result.data?.data
            if (imageUri != null) {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, imageUri))
                } else {
                    MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                }
                previewImageView.setImageBitmap(bitmap)
                imageBase64 = bitmapToBase64(bitmap)  // Store the Base64 string
            }
        }
    }
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
    private fun addRecipesToDatabase() {
        val db = Firebase.firestore
        val newRecipe = Recipes(
            id = db.collection("recipes").document().id,
            creatorName = getUserNameLog,
            recipesName = nameRecipesText.text.toString(),
            typeRecipes = typeRecipesSpinner.selectedItem.toString(),
            ingredientsRecipes = ingredientsRecipesText.text.toString(),
            stepRecipes = stepRecipesText.text.toString(),
            imageRecipes = imageBase64
        )

        db.collection("recipes").document(newRecipe.id).set(newRecipe)
            .addOnSuccessListener {
                Toast.makeText(this, "Tambah Resep Berhasil", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "gagal Menambah resep", Toast.LENGTH_SHORT).show()
            }

    }

}