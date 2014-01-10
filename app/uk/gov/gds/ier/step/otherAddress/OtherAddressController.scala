package uk.gov.gds.ier.step.otherAddress

import controllers.step._
import com.google.inject.Inject
import uk.gov.gds.ier.serialiser.{WithSerialiser, JsonSerialiser}
import uk.gov.gds.ier.validation._
import uk.gov.gds.ier.controller.OrdinaryController
import play.api.mvc.{SimpleResult, Call}
import uk.gov.gds.ier.model.{InprogressOrdinary, InprogressApplication}
import play.api.templates.Html
import uk.gov.gds.ier.config.Config
import uk.gov.gds.ier.guice.{WithEncryption, WithConfig}
import uk.gov.gds.ier.security.{EncryptionKeys, EncryptionService}

class OtherAddressController @Inject ()(val serialiser: JsonSerialiser,
                                        val config: Config,
                                        val encryptionService : EncryptionService,
                                        val encryptionKeys : EncryptionKeys)
  extends OrdinaryController
  with OtherAddressForms {

  val validation = otherAddressForm
  val editPostRoute = routes.OtherAddressController.editPost
  val stepPostRoute = routes.OtherAddressController.post

  def template(form:InProgressForm[InprogressOrdinary], call:Call): Html = {
    views.html.steps.otherAddress(form, call)
  }
  def goToNext(currentState: InprogressOrdinary): SimpleResult = {
    Redirect(routes.OpenRegisterController.get)
  }
}

