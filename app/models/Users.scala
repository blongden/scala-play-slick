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

  private class UserTable(tag: Tag) extends Table[UserModel](tag, "USER") {
    def id = column[String]("id", O.PrimaryKey)
    def email = column[String]("email")
    def password = column[String]("password")
    def fullname = column[String]("fullname")
    def isAdmin = column[Int]("isAdmin")

    def * = (id, email, password, fullname, isAdmin) <> ((UserModel.apply _).tupled, UserModel.unapply)
  }

  private val users = TableQuery[UserTable]

  def list(): Future[Seq[UserModel]] = dbConfig.db.run(users.result)

  def add(id: String, email: String, password: String, fullname: String, isAdmin: Int): Future[Int] = add(UserModel(id, email, password, fullname, isAdmin))

  def add(user: UserModel): Future[Int] = dbConfig.db.run { users += user }

  def findById(id: String): Future[UserModel] = dbConfig.db.run {
    users.filter(_.id === id).result.head
  }

  def update(user: UserModel): Future[Int] = dbConfig.db.run {
    users.filter(_.id === user.id).map(u => (u.email, u.password, u.fullname, u.isAdmin)).update((user.email, user.password, user.fullname, user.isAdmin))
  }

  def delete(id: String): Future[Int] = dbConfig.db.run {
    users.filter(_.id === id).delete
  }
}
