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
  val createForm: Form[CreateUser] = Form {
    mapping(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText,
      "fullname" -> nonEmptyText,
      "isAdmin" -> boolean
    )(CreateUser.apply)(CreateUser.unapply)
  }

  val editForm: Form[EditUser] = Form {
    mapping(
      "email" -> nonEmptyText,
      "fullname" -> nonEmptyText,
      "isAdmin" -> boolean
    )(EditUser.apply)(EditUser.unapply)
  }

  def index = Action.async { userRep.list().map(users => Ok(views.html.index(createForm, users))) }

  def addUser = Action.async { implicit request =>
    createForm.bindFromRequest.fold(
      errorForm => userRep.list().map(users => Ok(views.html.index(errorForm, users))),
      formData => {
        userRep.add(
          User(
            java.util.UUID.randomUUID.toString,
            formData.email,
            formData.password,
            formData.fullname,
            if (formData.isAdmin) 1 else 0
        )).map(redirectToIndex)
      }
    )
  }

  def editUser(id: String) = Action.async {
    userRep.findById(id).map {
      case Some(u) => Ok(views.html.edit(id, editForm.fill(EditUser(u.email, u.fullname, u.isAdmin == 1))))
      case None => userNotFound(id)
    }
  }

  def saveUser(id: String) = Action.async { implicit request =>
    editForm.bindFromRequest.fold(
      errorForm => Future.successful(Ok(views.html.edit(id, errorForm))),
      formData => {
        userRep.findById(id).flatMap {
          case None => Future.successful(userNotFound(id))
          case Some(existingUser) => userRep.update(User(id, formData.email, existingUser.password, formData.fullname, if (formData.isAdmin) 1 else 0)).map(redirectToIndex)
          }
        }
    )
  }

  def deleteUser(id: String) = Action.async {userRep.delete(id) map redirectToIndex}

  private def userNotFound(id: String) = NotFound(s"No user was found for id: $id")

  private def redirectToIndex(a: Any) = Redirect(routes.Application.index())
}

case class CreateUser(email: String, password: String, fullname: String, isAdmin: Boolean)
case class EditUser(email: String, fullname: String, isAdmin: Boolean)