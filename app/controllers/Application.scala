package controllers

import javax.inject.Inject

import play.api._
import play.api.mvc._
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global

class Application @Inject() (dbConfigProvider: db.slick.DatabaseConfigProvider) extends Controller {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.driver.api._

  private class UserTable(tag: Tag) extends Table[(Int, String, String, String, Int)](tag, "USER") {
    def id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    def email: Rep[String] = column[String]("email")
    def password: Rep[String] = column[String]("password")
    def fullname: Rep[String] = column[String]("fullname")
    def isAdmin: Rep[Int] = column[Int]("isAdmin")

    def * = (id, email, password, fullname, isAdmin)
  }

  private val users = TableQuery[UserTable]

  def index = Action.async { implicit request =>
    dbConfig.db.run(users.result).map(users => Ok(views.html.index(users)))
  }
}
