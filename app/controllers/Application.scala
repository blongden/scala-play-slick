package controllers

import java.util.UUID.randomUUID
import javax.inject.Inject
import models._
import forms.UserForms._
import play.api.data.Form
import play.api.mvc._
import play.api.i18n._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Application @Inject() (userRep: Users, val messagesApi: MessagesApi, handle: UserHandler) extends Controller with I18nSupport {

  def index = ActionWithUsers { implicit context => renderIndex(createForm) }

  def addUser = ActionWithUsers { implicit context =>
    createForm.bindFromRequest.fold(
      renderIndex,
      formData => handle(CreateUser(randomUUID.toString, formData)) map redirectToIndex
    )
  }

  def editUser(id: String) = ActionWithUsers { implicit context =>
    userRep.findById(id).map {
      case Some(u) => Ok(views.html.edit(id, editForm.fill(EditUserData(u.email, u.fullname, u.isAdmin))))
      case None => userNotFound(id)
    }
  }

  def saveUser(id: String) = ActionWithUsers { implicit context =>
    editForm.bindFromRequest.fold(
      errorForm => Future.successful(Ok(views.html.edit(id, errorForm))),
      formData => { handle(EditUser(id, formData)).map {
        case None => userNotFound(id)
        case _    => redirectToIndex()
      }}
    )
  }

  def deleteUser(id: String) = Action.async {handle(DeleteUser(id)) map redirectToIndex}

  def ActionWithUsers(f: ContextWithUsers => Future[Result]) = {
    Action.async { request =>
      userRep.list().flatMap(users => f(ContextWithUsers(users, request)))
    }
  }

  private def renderIndex(form: Form[CreateUserData])(implicit context: ContextWithUsers) = {
    Future(Ok(views.html.index(form)))
  }

  private def userNotFound(id: String) = NotFound(s"No user was found for id: $id")

  private def redirectToIndex(): Result = Redirect(routes.Application.index())

  private def redirectToIndex(a: Any): Result = redirectToIndex()
}

case class ContextWithUsers(users: Seq[User], request: Request[AnyContent]) extends WrappedRequest(request)