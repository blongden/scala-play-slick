package models

import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class UserHandler @Inject() (userRep: Users) {
  def apply(command: UserCommand) = command match {
    case CreateUser(id, CreateUserData(e, p, fn, a)) => userRep.add(User(id, e, p, fn, a))
    case EditUser(id, EditUserData(e, fn, a)) => handlerEditUser(id, e, fn, a)
    case DeleteUser(id) => userRep.delete(id)
  }

  private def handlerEditUser(id: String, email: String, fullname: String, isAdmin: Boolean) = {
    userRep.findById(id).flatMap {
      case None => Future(None)
      case Some(existingUser) => userRep.update(User(id, email, existingUser.password, fullname, isAdmin))
    }
  }
}
