package uk.gov.gds.ier.step.nino

import controllers.step._
import com.google.inject.Inject
import uk.gov.gds.ier.serialiser.{WithSerialiser, JsonSerialiser}
import uk.gov.gds.ier.validation._
import uk.gov.gds.ier.controller.StepController
import play.api.data.Form
import play.api.mvc.{SimpleResult, Call}
import uk.gov.gds.ier.model.InprogressApplication
import play.api.templates.Html

class NinoController @Inject ()(val serialiser: JsonSerialiser)
  extends StepController
  with WithSerialiser
  with NinoForms {

  val validation = ninoForm
  val editPostRoute = routes.NinoController.editPost
  val stepPostRoute = routes.NinoController.post

  def template(form:InProgressForm, call:Call): Html = {
    views.html.steps.nino(form, call)
  }
  def goToNext(currentState: InprogressApplication): SimpleResult = {
    Redirect(routes.AddressController.get)
  }
}

