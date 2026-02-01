package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.Friend
import com.google.android.material.button.MaterialButton

class FriendAdapter(
    private val friends: List<Friend>,
    private val onMessageClick: (Friend) -> Unit,
    private val onFriendClick: (Friend) -> Unit
) : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(friends[position], onMessageClick, onFriendClick)
    }

    override fun getItemCount(): Int = friends.size

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: ImageView = itemView.findViewById(R.id.image_friend_avatar)
        private val name: TextView = itemView.findViewById(R.id.text_friend_name)
        private val faculty: TextView = itemView.findViewById(R.id.text_friend_faculty)
        private val points: TextView = itemView.findViewById(R.id.text_friend_points)
        private val rank: TextView = itemView.findViewById(R.id.text_friend_rank)
        private val actionButton: MaterialButton = itemView.findViewById(R.id.button_friend_action)

        fun bind(friend: Friend, onMessageClick: (Friend) -> Unit, onFriendClick: (Friend) -> Unit) {
            name.text = friend.nickname
            faculty.text = friend.faculty ?: "Unknown Faculty"
            points.text = "${friend.points} points"
            rank.text = "· Rank #${friend.rank}"

            actionButton.setOnClickListener {
                onMessageClick(friend)
            }

            itemView.setOnClickListener {
                onFriendClick(friend)
            }
        }
    }
}
