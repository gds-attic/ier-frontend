package uk.gov.gds.ier.transaction.ordinary.dateOfBirth

import uk.gov.gds.ier.validation.ErrorTransformForm
import play.api.mvc.Call
import play.api.templates.Html
import uk.gov.gds.ier.step.StepTemplate
import uk.gov.gds.ier.transaction.ordinary.InprogressOrdinary

trait DateOfBirthMustache extends StepTemplate[InprogressOrdinary] {

  case class DateOfBirthModel(
      question:Question,
      day: Field,
      month: Field,
      year: Field,
      noDobReason: Field,
      rangeFieldSet: FieldSet,
      rangeUnder18: Field,
      rangeOver70: Field,
      range18to70: Field,
      rangeDontKnow: Field,
      noDobReasonShowFlag: Text
  )

  val mustache = MustacheTemplate("ordinary/dateOfBirth") { (form, post, back) =>
    implicit val progressForm = form

    val title = "What is your date of birth?"

    val data = DateOfBirthModel(
      question = Question(
        postUrl = post.url,
        backUrl = back.map (_.url).getOrElse(""),
        errorMessages = form.globalErrors.map{ _.message },
        number = "3 of 11",
        title = title
      ),
      day = TextField(
        key = keys.dob.dob.day
      ),
      month = TextField(
        key = keys.dob.dob.month
      ),
      year = TextField(
        key = keys.dob.dob.year
      ),
      noDobReason = TextField(
        key = keys.dob.noDob.reason
      ),
      rangeFieldSet = FieldSet (
        classes = if (form(keys.dob.noDob.range).hasErrors) "invalid" else ""
      ),
      rangeUnder18 = RadioField(
        key = keys.dob.noDob.range,
        value = "under18"
      ),
      range18to70 = RadioField(
        key = keys.dob.noDob.range,
        value = "18to70"
      ),
      rangeOver70 = RadioField(
        key = keys.dob.noDob.range,
        value = "over70"
      ),
      rangeDontKnow = RadioField(
        key = keys.dob.noDob.range,
        value = "dontKnow"
      ),
      noDobReasonShowFlag = Text (
        value = progressForm(keys.dob.noDob.reason).value.map(noDobReason => "-open").getOrElse("")
      )
    )

    MustacheData(data, title)
  }
}

