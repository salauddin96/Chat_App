package org.techtales.pakhi.UsersAdapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.techtales.pakhi.ChatActivity
import org.techtales.pakhi.R
import org.techtales.pakhi.databinding.ProfileItemsBinding
import org.techtales.pakhi.model.User

class UserAdapter (var context: Context, var userList: ArrayList<User>): RecyclerView.Adapter<UserAdapter.UserViewHolder>(){

    inner class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val binding: ProfileItemsBinding = ProfileItemsBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.profile_items, parent, false)
        return UserViewHolder(view)
    }



    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
       val user = userList[position]
        holder.binding.profileName.text = user.name
        Glide.with(context).load(user.profileImage).placeholder(R.drawable.baseline_person_24)
            .into(holder.binding.profileImg)
        holder.itemView.setOnClickListener {

            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", user.name)
            intent.putExtra("image", user.profileImage)
            intent.putExtra("uid", user.uid)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

}