package com.openclassrooms.hexagonal.games.screen.postdetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.data.model.CommentEntity
import java.text.DateFormat
import java.util.Date

class CommentsAdapter(
  options: FirestoreRecyclerOptions<CommentEntity>
) : FirestoreRecyclerAdapter<CommentEntity, CommentsAdapter.CommentViewHolder>(options) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.item_comment, parent, false)
    return CommentViewHolder(view)
  }

  override fun onBindViewHolder(holder: CommentViewHolder, position: Int, model: CommentEntity) {
    val author = model.author?.let { "${it.firstname} ${it.lastname}".trim() }
      ?.takeIf { it.isNotBlank() }
      ?: holder.itemView.context.getString(R.string.comment_author_fallback)
    holder.author.text = author
    holder.body.text = model.text
    holder.date.text = if (model.timestamp > 0) {
      DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
        .format(Date(model.timestamp))
    } else {
      ""
    }
  }

  class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val author: TextView = itemView.findViewById(R.id.commentAuthor)
    val body: TextView = itemView.findViewById(R.id.commentBody)
    val date: TextView = itemView.findViewById(R.id.commentDate)
  }
}
