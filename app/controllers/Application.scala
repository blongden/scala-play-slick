package controllers

import javax.inject.Inject
import models.Users
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global

class Application @Inject() (userRep: Users) extends Controller {
  def index = Action.async { implicit request =>
    userRep.list().map(users => Ok(views.html.index(users)))
  }

  def addUser = Action.async {
    userRep.add(java.util.UUID.randomUUID.toString, "bob@example.com", "password", "Bob Example", 0).map { _ => Redirect(routes.Application.index()) }
  }
}
