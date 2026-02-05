package com.openclassrooms.hexagonal.games.data.model

data class CommentEntity(
  val id: String = "",
  val text: String = "",
  val timestamp: Long = 0,
  val author: UserEntity? = null
)
