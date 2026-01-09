package com.openclassrooms.hexagonal.games.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme

@Composable
fun PasswordResetScreen(
  modifier: Modifier = Modifier,
  state: AuthUiState,
  onEmailChanged: (String) -> Unit,
  onSendReset: () -> Unit,
  onBack: () -> Unit,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 24.dp, vertical = 32.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = stringResource(id = R.string.reset_title),
      style = MaterialTheme.typography.headlineSmall
    )
    Spacer(modifier = Modifier.height(24.dp))
    OutlinedTextField(
      modifier = Modifier.fillMaxWidth(),
      value = state.email,
      onValueChange = onEmailChanged,
      label = { Text(text = stringResource(id = R.string.label_email)) },
      singleLine = true,
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Email,
        imeAction = ImeAction.Done
      )
    )
    if (state.errorMessage != null) {
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = state.errorMessage,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium
      )
    }
    if (state.infoMessage != null) {
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = state.infoMessage,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.bodyMedium
      )
    }
    Spacer(modifier = Modifier.height(24.dp))
    Button(
      modifier = Modifier.fillMaxWidth(),
      enabled = !state.isLoading,
      onClick = onSendReset
    ) {
      if (state.isLoading) {
        CircularProgressIndicator(
          modifier = Modifier
            .padding(vertical = 4.dp),
          strokeWidth = 3.dp
        )
      } else {
        Text(text = stringResource(id = R.string.action_reset_password))
      }
    }
    Spacer(modifier = Modifier.height(12.dp))
    OutlinedButton(
      modifier = Modifier.fillMaxWidth(),
      enabled = !state.isLoading,
      onClick = onBack
    ) {
      Text(text = stringResource(id = R.string.action_back_to_sign_in))
    }
  }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun PasswordResetScreenPreview() {
  HexagonalGamesTheme {
    PasswordResetScreen(
      state = AuthUiState(),
      onEmailChanged = {},
      onSendReset = {},
      onBack = {}
    )
  }
}
