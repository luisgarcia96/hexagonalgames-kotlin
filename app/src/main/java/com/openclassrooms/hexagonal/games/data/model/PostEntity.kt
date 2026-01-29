package com.openclassrooms.hexagonal.games.data.model

import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User

data class PostEntity(
  val id: String = "",
  val title: String = "",
  val description: String? = null,
  val photoUrl: String? = null,
  val timestamp: Long = 0,
  val author: UserEntity? = null
)

data class UserEntity(
  val id: String = "",
  val firstname: String = "",
  val lastname: String = ""
)

fun PostEntity.toDomain(): Post {
  return Post(
    id = id,
    title = title,
    description = description,
    photoUrl = photoUrl,
    timestamp = timestamp,
    author = author?.toDomain()
  )
}

fun UserEntity.toDomain(): User {
  return User(
    id = id,
    firstname = firstname,
    lastname = lastname
  )
}

fun Post.toEntity(): PostEntity {
  return PostEntity(
    id = id,
    title = title,
    description = description,
    photoUrl = photoUrl,
    timestamp = timestamp,
    author = author?.toEntity()
  )
}

fun User.toEntity(): UserEntity {
  return UserEntity(
    id = id,
    firstname = firstname,
    lastname = lastname
  )
}
