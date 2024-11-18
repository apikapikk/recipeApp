package com.sebuahgrup.recipeapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.sebuahgrup.recipeapp.model.User

class ProfileActivity : AppCompatActivity() {
    private lateinit var homeButton : ImageButton
    private lateinit var listButton : ImageButton
    private lateinit var likedRecipesButton : ImageButton
    private lateinit var actionRecipesButton : ImageButton
    private lateinit var profileButton : ImageButton
    private lateinit var userGreetings : TextView
    private lateinit var userNameProfile : EditText
    private lateinit var usernameProfile : EditText
    private lateinit var passwordProfile : EditText
    private lateinit var actionButton: Button
    private lateinit var auth : FirebaseAuth
    private lateinit var loadingProgressBar: FrameLayout
    private lateinit var logoutButton: Button
    private var isBackToHome = false
    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        val yourConstraintLayout: View = findViewById(R.id.main_profile_page)
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
        loadingProgressBar = findViewById(R.id.profile_loading_progress_bar)
        homeButton = findViewById(R.id.profile_navigation_home_button)
        listButton = findViewById(R.id.profile_navigation_list_recipes_button)
        likedRecipesButton = findViewById(R.id.profile_navigation_liked_recipes_button)
        actionRecipesButton = findViewById(R.id.profile_navigation_edit_recipes_button)
        profileButton = findViewById(R.id.profile_navigation_account_button)
        userGreetings = findViewById(R.id.profile_greetings_user_label)
        userNameProfile = findViewById(R.id.profile_name_text)
        usernameProfile = findViewById(R.id.profile_username_text)
        passwordProfile = findViewById(R.id.profile_password_text)
        actionButton = findViewById(R.id.profile_button_action_edit)
        logoutButton = findViewById(R.id.profile_button_action_logout)
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
        actionButton.setOnClickListener {
            if (isEditing) {
                updateUserData()
            } else {
                enableEditing(true)
            }
        }
        logoutButton.setOnClickListener {
            auth.signOut() // Logout dari Firebase
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        displayUser()
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
                userNameProfile.text = Editable.Factory.getInstance().newEditable(user.name)
                usernameProfile.text = Editable.Factory.getInstance().newEditable(user.email)
                passwordProfile.text = Editable.Factory.getInstance().newEditable(user.password)

            } else {
                Toast.makeText(this, "Data pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal mengambil data pengguna: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun enableEditing(enable: Boolean) {
        isEditing = enable
        userNameProfile.isEnabled = enable
        usernameProfile.isEnabled = enable
        passwordProfile.isEnabled = enable
        actionButton.text = if (enable) "Update" else "Edit"
    }
    private fun updateUserData() {
        val uid = auth.currentUser?.uid
        val newPassword = passwordProfile.text.toString()
        val newEmail = usernameProfile.text.toString()
        val newName = userNameProfile.text.toString()

        if (uid != null) {
            val user = auth.currentUser
            user?.updateEmail(newEmail)?.addOnSuccessListener {
                user.updatePassword(newPassword).addOnSuccessListener {
                    val db = FirebaseDatabase.getInstance().getReference("users").child(uid)
                    val updatedUser = mapOf(
                        "name" to newName,
                        "email" to newEmail,
                        "password" to newPassword
                    )

                    db.updateChildren(updatedUser).addOnSuccessListener {
                        Toast.makeText(this, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        enableEditing(false) // Kembali ke mode non-editable
                    }.addOnFailureListener {
                        showError("Gagal memperbarui data: ${it.message}")
                    }
                }.addOnFailureListener {
                    showError("Gagal memperbarui password: ${it.message}")
                }
            }?.addOnFailureListener {
                showError("Gagal memperbarui email: ${it.message}")
            }
        } else {
            showError("User belum login.")
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}