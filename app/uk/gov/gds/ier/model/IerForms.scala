package uk.gov.gds.ier.model

import play.api.data.Form
import play.api.data.Forms._

trait IerForms {
  lazy val postcodeRegex = "(?i)((GIR 0AA)|((([A-Z-[QVX]][0-9][0-9]?)|(([A-Z-[QVX]][A-Z-[IJZ]][0-9][0-9]?)|(([A-Z-[QVX]][0-9][A-HJKSTUW])|([A-Z-[QVX]][A-Z-[IJZ]][0-9][ABEHMNPRVWXY]))))\\s?[0-9][A-Z-[CIKMOV]]{2}))"
  val dobFormat = "yyyy-MM-dd"
  val timeFormat = "yyyy-MM-dd HH:mm:ss"
  val webApplicationForm = Form(
    mapping(
      "firstName" -> nonEmptyText,
      "middleName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "previousLastName" -> nonEmptyText,
      "nino" -> nonEmptyText,
      "dob" -> jodaLocalDate(dobFormat)
      )(WebApplication.apply)(WebApplication.unapply)
  )
  val apiApplicationForm = Form(
    mapping(
      "fn" -> nonEmptyText,
      "mn" -> nonEmptyText,
      "ln" -> nonEmptyText,
      "pln" -> nonEmptyText,
      "gssCode" -> nonEmptyText,
      "nino" -> text,
      "dob" -> jodaLocalDate(dobFormat)
    )(ApiApplication.apply)(ApiApplication.unapply)
  )
  val apiApplicationResponseForm = Form(
    mapping(
      "detail" -> apiApplicationForm.mapping,
      "ierId" -> nonEmptyText,
      "createdAt" -> nonEmptyText,
      "status" -> nonEmptyText,
      "source" -> nonEmptyText
    )(ApiApplicationResponse.apply)(ApiApplicationResponse.unapply)
  )
  val postcodeForm = Form(
    single(
      "postcode" -> nonEmptyText.verifying(_.matches(postcodeRegex))
    )
  )

  class BetterForm[A](form: Form[A]) {
    def errorsAsMap = {
      form.errors.groupBy(_.key).mapValues {
        errors =>
          errors.map(e => play.api.i18n.Messages(e.message, e.args: _*))
      }
    }
    def simpleErrors: Map[String, String] = {
      form.errors.foldLeft(Map.empty[String, String]){
        (map, error) => map ++ Map(error.key -> play.api.i18n.Messages(error.message, error.args: _*))
      }
    }
  }
  implicit def form2BetterForm[A](form: Form[A]): BetterForm[A] = new BetterForm[A](form)
}
