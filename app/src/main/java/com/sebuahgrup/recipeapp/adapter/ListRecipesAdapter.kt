package com.sebuahgrup.recipeapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sebuahgrup.recipeapp.R
import android.graphics.Bitmap
import android.util.Base64
import android.graphics.BitmapFactory
import android.widget.ImageButton
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.sebuahgrup.recipeapp.model.Recipes

class ListRecipesAdapter(
    private val recipesList: List<Recipes>,
    private val onItemClick: (Recipes) -> Unit,
    private val onLikeClick: (Recipes) -> Unit
) : RecyclerView.Adapter<ListRecipesAdapter.RecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_list_recipes_card, parent, false)
        return RecipeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipesList[position]
        holder.recipesName.text = recipe.recipesName
        holder.recipesType.text = recipe.typeRecipes
        holder.authorRecipes.text = recipe.creatorName
        // Decode the Base64 string and get Bitmap
        val imageBitmap = decodeBase64ToBitmap(recipe.imageRecipes)
        // Display image in ImageView using Glide (Glide handles Bitmap)
        Glide.with(holder.itemView.context)
            .load(imageBitmap)  // Glide can accept Bitmap
            .into(holder.recipeImage)  // Set the image into ImageView
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid != null && recipe.likedBy.contains(currentUserUid)) {
            holder.likeButton.setImageResource(R.drawable.liked_recipes_on) // Use your "liked" icon
        } else {
            holder.likeButton.setImageResource(R.drawable.liked_recipes_off) // Use your "not liked" icon
        }
        holder.itemView.setOnClickListener {
            onItemClick(recipe)  // Pass the clicked recipe to the listener
        }
        holder.likeButton.setOnClickListener {
            onLikeClick(recipe)
        }
    }

    override fun getItemCount(): Int {
        return recipesList.size
    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val likeButton : ImageButton = itemView.findViewById(R.id.list_liked_button_card)
        val recipesName: TextView = itemView.findViewById(R.id.list_recipe_name_card)
        val recipesType : TextView = itemView.findViewById(R.id.list_recipe_type_card)
        val authorRecipes: TextView = itemView.findViewById(R.id.list_recipes_creator_name)
        val recipeImage: ImageView = itemView.findViewById(R.id.list_recipes_image_card)
    }
}
