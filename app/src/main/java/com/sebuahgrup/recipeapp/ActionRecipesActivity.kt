package com.sebuahgrup.recipeapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.firestore
import com.sebuahgrup.recipeapp.model.Recipes
import com.sebuahgrup.recipeapp.model.User
import java.io.ByteArrayOutputStream


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
    private lateinit var loadingProgressBar: FrameLayout
    private var imageBase64: String = ""
    private var getUserNameLog: String = ""
    private lateinit var auth : FirebaseAuth
    private var isBackToHome = false
    private var isUpdateMode: Boolean = false
    private var recipeIdToUpdate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_action_recipes)
        val yourConstraintLayout: View = findViewById(R.id.main_action_page)
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
        //initialize button
        loadingProgressBar = findViewById(R.id.action_loading_progress_bar)
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
        isUpdateMode = intent.getBooleanExtra("isUpdate", false)
        recipeIdToUpdate = intent.getStringExtra("recipeId")

        //spinner data type
        val recipeTypes = arrayOf("minuman", "makanan Ringan", "makanan Berat", "pembuka","Penutup")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, recipeTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeRecipesSpinner.adapter = adapter

        onBackPressedDispatcher.addCallback(this) {
           handlePress()
        }
        if (isUpdateMode) {
            setupUpdateMode()
        } else {
            addRecipesButton.text = "Add"
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
            if (isUpdateMode) {
                updateRecipeInDatabase()
            } else {
                addRecipesToDatabase()
            }
        }
        displayUser()
    }

    private fun updateRecipeInDatabase() {
        loadingProgressBar.visibility = View.VISIBLE
        val db = Firebase.firestore

        val updatedRecipe = mapOf(
            "recipesName" to nameRecipesText.text.toString(),
            "typeRecipes" to typeRecipesSpinner.selectedItem.toString(),
            "ingredientsRecipes" to ingredientsRecipesText.text.toString(),
            "stepRecipes" to stepRecipesText.text.toString(),
            "imageRecipes" to imageBase64
        )

        recipeIdToUpdate?.let { id ->
            db.collection("recipes").document(id).update(updatedRecipe)
                .addOnSuccessListener {
                    loadingProgressBar.visibility = View.GONE
                    Toast.makeText(this, "Update Resep Berhasil", Toast.LENGTH_SHORT).show()
                    finish() // Kembali ke halaman sebelumnya
                }
                .addOnFailureListener {
                    loadingProgressBar.visibility = View.GONE
                    Toast.makeText(this, "Gagal Mengupdate Resep", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupUpdateMode() {
        addRecipesButton.text = "Update Recipes"

        // Ambil data resep dari Firestore dan isi form
        val db = Firebase.firestore
        recipeIdToUpdate?.let { id ->
            db.collection("recipes").document(id).get()
                .addOnSuccessListener { document ->
                    val recipe = document.toObject(Recipes::class.java)
                    if (recipe != null) {

                        nameRecipesText.setText(recipe.recipesName)
                        ingredientsRecipesText.setText(recipe.ingredientsRecipes)
                        stepRecipesText.setText(recipe.stepRecipes)
                        val typePosition = (typeRecipesSpinner.adapter as ArrayAdapter<String>)
                            .getPosition(recipe.typeRecipes)
                        typeRecipesSpinner.setSelection(typePosition)

                        if (recipe.imageRecipes.isNotEmpty()) {
                            val decodedImage = Base64.decode(recipe.imageRecipes, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)
                            previewImageView.setImageBitmap(bitmap)
                            imageBase64 = recipe.imageRecipes
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal memuat data resep", Toast.LENGTH_SHORT).show()
                }
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
    private fun compressBitmap(bitmap: Bitmap): Bitmap {
        val maxWidth = 1024  // Tentukan lebar maksimal gambar
        val maxHeight = 1024 // Tentukan tinggi maksimal gambar

        val ratio: Float = Math.min(maxWidth.toFloat() / bitmap.width, maxHeight.toFloat() / bitmap.height)
        val width = (bitmap.width * ratio).toInt()
        val height = (bitmap.height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, width, height, true)
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
                val format = getImageFormat(imageUri)  // Mendapatkan format gambar
                if (checkImageSize(bitmap)) {
                    val compressedBitmap = compressBitmap(bitmap)
                    previewImageView.setImageBitmap(compressedBitmap)
                    imageBase64 = bitmapToBase64(compressedBitmap, format)  // Mengirimkan format yang benar
                } else {
                    Toast.makeText(this, "Ukuran gambar terlalu besar, silakan pilih gambar yang lebih kecil", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun getImageFormat(uri: Uri): Bitmap.CompressFormat {
        val mimeType = contentResolver.getType(uri)
        return when {
            mimeType?.contains("jpeg") == true || mimeType?.contains("jpg") == true -> Bitmap.CompressFormat.JPEG
            mimeType?.contains("png") == true -> Bitmap.CompressFormat.PNG
            mimeType?.contains("webp") == true -> Bitmap.CompressFormat.WEBP
            else -> Bitmap.CompressFormat.JPEG  // Default ke JPEG jika format tidak dikenal
        }
    }
    private fun checkImageSize(bitmap: Bitmap): Boolean {
        val maxSize = 1024 * 1024  // Maksimal ukuran gambar 1MB
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        return byteArray.size <= maxSize
    }
    private fun bitmapToBase64(bitmap: Bitmap, format: Bitmap.CompressFormat): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(format, 60, byteArrayOutputStream)  // Menggunakan format yang diteruskan
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
    private fun addRecipesToDatabase() {
        val recipeName = nameRecipesText.text.toString()
        val ingredients = ingredientsRecipesText.text.toString()
        val steps = stepRecipesText.text.toString()
        val image = imageBase64

        if (recipeName.isNullOrEmpty() || ingredients.isNullOrEmpty() || steps.isNullOrEmpty() || image.isNullOrEmpty()) {
            Toast.makeText(this, "Semua kolom harus diisi dan gambar harus ada!", Toast.LENGTH_SHORT).show()
            return
        }
        loadingProgressBar.visibility = View.VISIBLE
        val db = Firebase.firestore
        val newRecipe = Recipes(
            id = db.collection("recipes").document().id,
            creatorName = getUserNameLog,
            creatorUid = auth.currentUser?.uid.toString(),
            recipesName = nameRecipesText.text.toString(),
            typeRecipes = typeRecipesSpinner.selectedItem.toString(),
            ingredientsRecipes = ingredientsRecipesText.text.toString(),
            stepRecipes = stepRecipesText.text.toString(),
            imageRecipes = imageBase64
        )

        db.collection("recipes").document(newRecipe.id).set(newRecipe)
            .addOnSuccessListener {
                loadingProgressBar.visibility = View.GONE
                clearForm()
                Toast.makeText(this, "Tambah Resep Berhasil", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                loadingProgressBar.visibility = View.GONE
                Toast.makeText(this, "gagal Menambah resep", Toast.LENGTH_SHORT).show()
            }

    }

    private fun clearForm() {
        nameRecipesText.text.clear()
        ingredientsRecipesText.text.clear()
        stepRecipesText.text.clear()
    }

}