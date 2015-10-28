package controllers

import javax.inject.Inject
import models.Users
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc._
import play.api.i18n._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Application @Inject() (userRep: Users, val messagesApi: MessagesApi) extends Controller with I18nSupport {
  val userForm: Form[CreateUserForm] = Form {
    mapping(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText,
      "fullname" -> nonEmptyText,
      "isAdmin" -> boolean
    )(CreateUserForm.apply)(CreateUserForm.unapply)
  }

  def index = Action.async { implicit request =>
    userRep.list().map(users => Ok(views.html.index(userForm, users)))
  }

  def addUser = Action.async { implicit request =>
    userForm.bindFromRequest.fold(
      errorForm => {
        userRep.list().map(users => Ok(views.html.index(errorForm, users)))
      },

      user => {
        userRep.add(java.util.UUID.randomUUID.toString, user.email, user.password, user.fullname, if (user.isAdmin) 1 else 0).map { _ => Redirect(routes.Application.index()) }
      }
    )

  }
}

case class CreateUserForm(email: String, password: String, fullname: String, isAdmin: Boolean)