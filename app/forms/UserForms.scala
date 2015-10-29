package forms

import models.{EditUserData, CreateUserData}
import play.api.data.Form
import play.api.data.Forms._

object UserForms {
  val createForm: Form[CreateUserData] = Form {
    mapping(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText,
      "fullname" -> nonEmptyText,
      "isAdmin" -> boolean
    )(CreateUserData.apply)(CreateUserData.unapply)
  }

  val editForm: Form[EditUserData] = Form {
    mapping(
      "email" -> nonEmptyText,
      "fullname" -> nonEmptyText,
      "isAdmin" -> boolean
    )(EditUserData.apply)(EditUserData.unapply)
  }
}
