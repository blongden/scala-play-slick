package models

sealed trait UserCommand
final case class CreateUser(id: String, properties: CreateUserData) extends UserCommand
final case class EditUser(id: String, properties: EditUserData) extends UserCommand
final case class DeleteUser(id: String) extends UserCommand

case class CreateUserData(email: String, password: String, fullname: String, isAdmin: Boolean)
case class EditUserData(email: String, fullname: String, isAdmin: Boolean)
