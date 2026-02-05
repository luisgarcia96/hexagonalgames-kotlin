package com.openclassrooms.hexagonal.games.screen.postdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import android.widget.Toast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.compose.AsyncImage
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.data.model.CommentEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
  modifier: Modifier = Modifier,
  onBackClick: () -> Unit,
  onPostDeleted: () -> Unit = onBackClick,
  onAddCommentClick: () -> Unit = {},
  viewModel: PostDetailViewModel = hiltViewModel()
) {
  val post by viewModel.post.collectAsStateWithLifecycle()
  val context = LocalContext.current
  var showDeletePostDialog by rememberSaveable { mutableStateOf(false) }
  var commentPendingDelete by remember { mutableStateOf<CommentPendingDelete?>(null) }

  LaunchedEffect(viewModel.events) {
    viewModel.events.collect { event ->
      when (event) {
        is PostDetailEvent.Error -> {
          Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
        }
        PostDetailEvent.PostDeleted -> {
          Toast.makeText(
            context,
            context.getString(R.string.post_deleted_message),
            Toast.LENGTH_SHORT
          ).show()
          onPostDeleted()
        }
      }
    }
  }

  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(id = R.string.post_detail_title)) },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(id = R.string.contentDescription_go_back)
            )
          }
        },
        actions = {
          if (viewModel.canDeletePost(post)) {
            IconButton(
              onClick = { showDeletePostDialog = true }
            ) {
              Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(id = R.string.action_delete_post)
              )
            }
          }
        }
      )
    },
    floatingActionButton = {
      if (viewModel.currentUserId != null) {
        FloatingActionButton(onClick = onAddCommentClick) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(id = R.string.action_add_comment)
          )
        }
      }
    }
  ) { contentPadding ->
    Column(
      modifier = Modifier
        .padding(contentPadding)
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      if (post != null) {
        Text(
          text = stringResource(
            id = R.string.by,
            post?.author?.firstname ?: "",
            post?.author?.lastname ?: ""
          ),
          style = MaterialTheme.typography.titleSmall
        )
        Text(
          modifier = Modifier.padding(top = 4.dp),
          text = post?.title.orEmpty(),
          style = MaterialTheme.typography.titleLarge
        )
        if (post?.photoUrl.isNullOrEmpty() == false) {
          AsyncImage(
            modifier = Modifier
              .padding(top = 12.dp)
              .fillMaxWidth()
              .heightIn(max = 240.dp),
            model = post?.photoUrl,
            contentDescription = stringResource(id = R.string.contentDescription_post_image),
            contentScale = ContentScale.Crop
          )
        }
        if (post?.description.isNullOrEmpty() == false) {
          Text(
            modifier = Modifier.padding(top = 12.dp),
            text = post?.description.orEmpty(),
            style = MaterialTheme.typography.bodyMedium
          )
        }
      }

      Text(
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        text = stringResource(id = R.string.comments_title),
        style = MaterialTheme.typography.titleMedium
      )

      CommentsList(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        postId = viewModel.postId,
        currentUserId = viewModel.currentUserId,
        onDeleteComment = { commentId, commentAuthorId ->
          commentPendingDelete = CommentPendingDelete(commentId, commentAuthorId)
        }
      )
    }
  }

  if (showDeletePostDialog) {
    AlertDialog(
      onDismissRequest = { showDeletePostDialog = false },
      title = { Text(text = stringResource(id = R.string.delete_post_title)) },
      text = { Text(text = stringResource(id = R.string.delete_post_message)) },
      confirmButton = {
        TextButton(
          onClick = {
            showDeletePostDialog = false
            viewModel.deletePost()
          }
        ) {
          Text(text = stringResource(id = R.string.action_delete_post))
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeletePostDialog = false }) {
          Text(text = stringResource(id = R.string.action_cancel))
        }
      }
    )
  }

  if (commentPendingDelete != null) {
    AlertDialog(
      onDismissRequest = { commentPendingDelete = null },
      title = { Text(text = stringResource(id = R.string.delete_comment_title)) },
      text = { Text(text = stringResource(id = R.string.delete_comment_message)) },
      confirmButton = {
        TextButton(
          onClick = {
            val pendingDelete = commentPendingDelete
            commentPendingDelete = null
            if (pendingDelete != null) {
              viewModel.deleteComment(pendingDelete.commentId, pendingDelete.authorId)
            }
          }
        ) {
          Text(text = stringResource(id = R.string.action_delete_comment))
        }
      },
      dismissButton = {
        TextButton(onClick = { commentPendingDelete = null }) {
          Text(text = stringResource(id = R.string.action_cancel))
        }
      }
    )
  }
}

@Composable
private fun CommentsList(
  modifier: Modifier = Modifier,
  postId: String,
  currentUserId: String?,
  onDeleteComment: (commentId: String, commentAuthorId: String?) -> Unit
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  val query = androidx.compose.runtime.remember(postId) {
    FirebaseFirestore.getInstance()
      .collection("posts")
      .document(postId)
      .collection("comments")
      .orderBy("timestamp", Query.Direction.ASCENDING)
  }

  val options = androidx.compose.runtime.remember(postId, lifecycleOwner) {
    FirestoreRecyclerOptions.Builder<CommentEntity>()
      .setQuery(query, CommentEntity::class.java)
      .setLifecycleOwner(lifecycleOwner)
      .build()
  }

  val adapter = rememberCommentsAdapter(
    options = options,
    currentUserId = currentUserId,
    onDeleteComment = onDeleteComment
  )

  AndroidView(
    modifier = modifier,
    factory = {
      RecyclerView(context).apply {
        layoutManager = LinearLayoutManager(context)
        addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        this.adapter = adapter
      }
    },
    update = { recyclerView ->
      recyclerView.adapter = adapter
    }
  )
}

@Composable
private fun rememberCommentsAdapter(
  options: FirestoreRecyclerOptions<CommentEntity>,
  currentUserId: String?,
  onDeleteComment: (commentId: String, commentAuthorId: String?) -> Unit
): CommentsAdapter {
  return androidx.compose.runtime.remember(options, currentUserId, onDeleteComment) {
    CommentsAdapter(options, currentUserId, onDeleteComment)
  }
}

private data class CommentPendingDelete(
  val commentId: String,
  val authorId: String?
)

const val POST_ID_ARG = "postId"
