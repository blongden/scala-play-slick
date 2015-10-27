package controllers

import javax.inject.Inject

import play.api._
import play.api.mvc._
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global

case class User(id: Int, email: String, password: String, fullname: String, isAdmin: Int)

class Application @Inject() (dbConfigProvider: db.slick.DatabaseConfigProvider) extends Controller {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.driver.api._

  private class UserTable(tag: Tag) extends Table[User](tag, "USER") {
    def id = column[Int]("id", O.PrimaryKey)
    def email = column[String]("email")
    def password = column[String]("password")
    def fullname = column[String]("fullname")
    def isAdmin = column[Int]("isAdmin")

    def * = (id, email, password, fullname, isAdmin) <> ((User.apply _).tupled, User.unapply)
  }

  private val users = TableQuery[UserTable]

  def index = Action.async { implicit request =>
    dbConfig.db.run(users.result).map(users => Ok(views.html.index(users)))
  }
}
