package uk.gov.gds.ier.step.postalVote

import controllers.step._
import com.google.inject.Inject
import uk.gov.gds.ier.serialiser.{WithSerialiser, JsonSerialiser}
import uk.gov.gds.ier.validation._
import uk.gov.gds.ier.controller.StepController
import play.api.data.Form
import play.api.mvc.{SimpleResult, Call}
import uk.gov.gds.ier.model.InprogressApplication
import play.api.templates.Html

class PostalVoteController @Inject ()(val serialiser: JsonSerialiser,
                                      val errorTransformer: ErrorTransformer) 
  extends StepController
  with WithSerialiser
  with WithErrorTransformer
  with PostalVoteForms {

  val validation: Form[InprogressApplication] = postalVoteForm
  val editPostRoute: Call = routes.PostalVoteController.editPost
  val stepPostRoute: Call = routes.PostalVoteController.post

  def template(form:InProgressForm, call:Call): Html = {
    views.html.steps.postalVote(form, call)
  }
  def goToNext(currentState: InprogressApplication): SimpleResult = {
    Redirect(routes.ContactController.get)
  }
}
