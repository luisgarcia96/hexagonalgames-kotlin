package com.openclassrooms.hexagonal.games.screen.postdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
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
  viewModel: PostDetailViewModel = hiltViewModel()
) {
  val post by viewModel.post.collectAsStateWithLifecycle()

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
        }
      )
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
        postId = viewModel.postId
      )
    }
  }
}

@Composable
private fun CommentsList(
  modifier: Modifier = Modifier,
  postId: String
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

  val adapter = rememberCommentsAdapter(options)

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
  options: FirestoreRecyclerOptions<CommentEntity>
): CommentsAdapter {
  return androidx.compose.runtime.remember(options) {
    CommentsAdapter(options)
  }
}

const val POST_ID_ARG = "postId"
