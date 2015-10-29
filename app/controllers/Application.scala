package controllers

import javax.inject.Inject
import models._
import forms.UserForms._
import play.api.mvc._
import play.api.i18n._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Application @Inject() (userRep: Users, val messagesApi: MessagesApi, handle: UserHandler) extends Controller with I18nSupport {

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
}