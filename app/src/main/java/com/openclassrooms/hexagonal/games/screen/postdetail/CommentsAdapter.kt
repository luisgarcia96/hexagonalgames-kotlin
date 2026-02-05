package com.openclassrooms.hexagonal.games.screen.postdetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.data.model.CommentEntity
import java.text.DateFormat
import java.util.Date

class CommentsAdapter(
  options: FirestoreRecyclerOptions<CommentEntity>,
  private val currentUserId: String?,
  private val onDeleteComment: (commentId: String, commentAuthorId: String?) -> Unit
) : FirestoreRecyclerAdapter<CommentEntity, CommentsAdapter.CommentViewHolder>(options) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.item_comment, parent, false)
    return CommentViewHolder(view)
  }

  override fun onBindViewHolder(holder: CommentViewHolder, position: Int, model: CommentEntity) {
    val commentId = snapshots.getSnapshot(position).id
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

    val canDelete = currentUserId != null && model.author?.id == currentUserId
    holder.delete.visibility = if (canDelete) View.VISIBLE else View.GONE
    holder.delete.setOnClickListener(
      if (canDelete) {
        View.OnClickListener { onDeleteComment(commentId, model.author?.id) }
      } else {
        null
      }
    )
  }

  class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val author: TextView = itemView.findViewById(R.id.commentAuthor)
    val body: TextView = itemView.findViewById(R.id.commentBody)
    val date: TextView = itemView.findViewById(R.id.commentDate)
    val delete: ImageButton = itemView.findViewById(R.id.commentDelete)
  }
}
