package uk.gov.gds.ier.step.contact

import controllers._
import com.google.inject.Inject
import uk.gov.gds.ier.serialiser.{WithSerialiser, JsonSerialiser}
import uk.gov.gds.ier.validation._
import uk.gov.gds.ier.controller.StepController
import play.api.data.Form
import play.api.mvc.{SimpleResult, Call}
import uk.gov.gds.ier.model.InprogressApplication
import play.api.templates.Html

class ContactController @Inject ()(val serialiser: JsonSerialiser)
  extends StepController
  with WithSerialiser
  with ContactForms {

  val validation = contactForm
  val editPostRoute = step.routes.ContactController.editPost
  val stepPostRoute = step.routes.ContactController.post

  def template(form:InProgressForm, call:Call): Html = {
    views.html.steps.contact(form, call)
  }
  def goToNext(currentState: InprogressApplication): SimpleResult = {
    Redirect(routes.ConfirmationController.get)
  }
}

