package uk.gov.gds.ier.step

import uk.gov.gds.ier.serialiser.WithSerialiser
import uk.gov.gds.ier.model.InprogressOrdinary
import uk.gov.gds.ier.guice.{WithEncryption, WithConfig}
import controllers.step.ordinary.routes.ConfirmationController

trait OrdinaryStep
  extends StepController[InprogressOrdinary]
  with WithSerialiser
  with WithConfig
  with WithEncryption {
  def factoryOfT() = InprogressOrdinary()
  val confirmationRoute = ConfirmationController.get
}