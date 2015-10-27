package models

import javax.inject.Inject

import models.{User => UserModel}
import play.api.db
import slick.driver.JdbcProfile
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by ben on 27/10/15.
 */
class Users @Inject() (dbConfigProvider: db.slick.DatabaseConfigProvider) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.driver.api._

  private class UserTable(tag: Tag) extends Table[UserModel](tag, "USER") {
    def id = column[Int]("id", O.PrimaryKey)
    def email = column[String]("email")
    def password = column[String]("password")
    def fullname = column[String]("fullname")
    def isAdmin = column[Int]("isAdmin")

    def * = (id, email, password, fullname, isAdmin) <> ((UserModel.apply _).tupled, UserModel.unapply)
  }

  private val users = TableQuery[UserTable]

  def list(): Future[Seq[UserModel]] = dbConfig.db.run(users.result)
}
