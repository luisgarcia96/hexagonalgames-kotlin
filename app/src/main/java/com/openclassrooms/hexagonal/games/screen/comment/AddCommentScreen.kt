package com.openclassrooms.hexagonal.games.screen.comment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openclassrooms.hexagonal.games.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCommentScreen(
  modifier: Modifier = Modifier,
  viewModel: AddCommentViewModel = hiltViewModel(),
  onBackClick: () -> Unit,
  onSaveClick: () -> Unit
) {
  val comment by viewModel.comment.collectAsStateWithLifecycle()
  val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
  val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

  LaunchedEffect(Unit) {
    viewModel.saveCompleted.collect {
      onSaveClick()
    }
  }

  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(id = R.string.add_comment_title)) },
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
        .padding(16.dp)
    ) {
      OutlinedTextField(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        value = comment,
        onValueChange = viewModel::onCommentChanged,
        label = { Text(text = stringResource(id = R.string.hint_comment)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
      )

      if (errorMessage != null) {
        Text(
          modifier = Modifier.padding(top = 8.dp),
          text = errorMessage.orEmpty(),
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodyMedium
        )
      }

      Button(
        modifier = Modifier
          .padding(top = 16.dp)
          .fillMaxWidth(),
        onClick = { viewModel.addComment() },
        enabled = !isSaving
      ) {
        Text(text = stringResource(id = R.string.action_save_comment))
      }
    }
  }
}
