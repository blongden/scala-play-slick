package controllers

import javax.inject.Inject
import models.{User, Users}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc._
import play.api.i18n._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Application @Inject() (userRep: Users, val messagesApi: MessagesApi) extends Controller with I18nSupport {
  val createForm: Form[CreateUserData] = Form {
    mapping(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText,
      "fullname" -> nonEmptyText,
      "isAdmin" -> boolean
    )(CreateUserData.apply)(CreateUserData.unapply)
  }

  val editForm: Form[EditUserData] = Form {
    mapping(
      "email" -> nonEmptyText,
      "fullname" -> nonEmptyText,
      "isAdmin" -> boolean
    )(EditUserData.apply)(EditUserData.unapply)
  }

  def index = Action.async { userRep.list().map(users => Ok(views.html.index(createForm, users))) }

  def addUser = Action.async { implicit request =>
    createForm.bindFromRequest.fold(
      errorForm => userRep.list().map(users => Ok(views.html.index(errorForm, users))),
      formData => handle(CreateUser(java.util.UUID.randomUUID.toString, formData)).map(redirectToIndex)
    )
  }

  def editUser(id: String) = Action.async {
    userRep.findById(id).map {
      case Some(u) => Ok(views.html.edit(id, editForm.fill(EditUserData(u.email, u.fullname, u.isAdmin))))
      case None => userNotFound(id)
    }
  }

  def saveUser(id: String) = Action.async { implicit request =>
    editForm.bindFromRequest.fold(
      errorForm => Future.successful(Ok(views.html.edit(id, errorForm))),
      formData => { handle(EditUser(id, formData)).map {
        case None => userNotFound(id)
        case _    => redirectToIndex()
      }}
    )
  }

  def deleteUser(id: String) = Action.async {handle(DeleteUser(id)) map redirectToIndex}

  private def userNotFound(id: String) = NotFound(s"No user was found for id: $id")

  private def redirectToIndex(): Result = Redirect(routes.Application.index())

  private def redirectToIndex(a: Any): Result = redirectToIndex()

  private def handle(command: UserCommand) = command match {
    case CreateUser(id, CreateUserData(e, p, fn, a)) => userRep.add(User(id, e, p, fn, a))
    case EditUser(id, EditUserData(e, fn, a)) => userRep.findById(id).flatMap {
      case None => Future(None)
      case Some(existingUser) => userRep.update(User(id, e, existingUser.password, fn, a))
    }
    case DeleteUser(id) => userRep.delete(id)
  }
}

sealed trait UserCommand
final case class CreateUser(id: String, properties: CreateUserData) extends UserCommand
final case class EditUser(id: String, properties: EditUserData) extends UserCommand
final case class DeleteUser(id: String) extends UserCommand

case class CreateUserData(email: String, password: String, fullname: String, isAdmin: Boolean)
case class EditUserData(email: String, fullname: String, isAdmin: Boolean)