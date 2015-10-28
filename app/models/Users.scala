package models

import javax.inject.Inject

import models.{User => UserModel}
import play.api.db
import slick.driver.JdbcProfile
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class Users @Inject() (dbConfigProvider: db.slick.DatabaseConfigProvider) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.driver.api._
  import dbConfig.db

  private class UserTable(tag: Tag) extends Table[UserModel](tag, "USER") {
    def id = column[String]("id", O.PrimaryKey)
    def email = column[String]("email")
    def password = column[String]("password")
    def fullname = column[String]("fullname")
    def isAdmin = column[Int]("isAdmin")

    def * = (id, email, password, fullname, isAdmin) <> ((UserModel.apply _).tupled, UserModel.unapply)
  }

  private val users = TableQuery[UserTable]

  def list(): Future[Seq[UserModel]] = db.run(users.result)

  def add(user: UserModel): Future[Int] = db.run { users += user }

  private def filterById(id: String) = users.filter(_.id === id)

  def findById(id: String): Future[UserModel] = db.run { filterById(id).result.head }

  def update(user: UserModel): Future[Int] = db.run { filterById(user.id).update(user) }

  def delete(id: String): Future[Int] = db.run { filterById(id).delete }
}
