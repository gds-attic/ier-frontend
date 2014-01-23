package controllers.step.overseas

import uk.gov.gds.ier.stubs.StubController
import uk.gov.gds.ier.model.InprogressOverseas

object DateLeftUkController extends StubController[InprogressOverseas] {
  val confirmationStep = ConfirmationController.confirmationStep
  val thisStepUrl = "/register-to-vote/overseas/date-left-uk"

  def dateLeftUkStep = confirmationIf { application =>
    application.dateLeftUk.isDefined
  }
}