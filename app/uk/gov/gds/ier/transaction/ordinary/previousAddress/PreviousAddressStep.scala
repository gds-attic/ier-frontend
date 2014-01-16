package uk.gov.gds.ier.transaction.ordinary.previousAddress

import controllers.step.ordinary.OtherAddressController
import controllers.step.ordinary.routes.PreviousAddressController
import com.google.inject.Inject
import uk.gov.gds.ier.model.{InprogressOrdinary, Addresses, PossibleAddress}
import uk.gov.gds.ier.transaction.ordinary.address.AddressForms
import uk.gov.gds.ier.serialiser.JsonSerialiser
import uk.gov.gds.ier.validation._
import play.api.mvc.Call
import play.api.templates.Html
import uk.gov.gds.ier.config.Config
import uk.gov.gds.ier.security.{EncryptionKeys, EncryptionService}
import uk.gov.gds.ier.service.AddressService

import uk.gov.gds.ier.step.{Routes, OrdinaryStep}


class PreviousAddressStep @Inject ()(val serialiser: JsonSerialiser,
                                           val config: Config,
                                           val encryptionService : EncryptionService,
                                           val encryptionKeys : EncryptionKeys,
                                           val addressService: AddressService)
  extends OrdinaryStep
  with AddressForms
  with PreviousAddressForms {

  val validation = previousAddressForm

  val routes = Routes(
    get = PreviousAddressController.get,
    post = PreviousAddressController.post
  )


  override def post(implicit manifest: Manifest[InprogressOrdinary]) = ValidSession requiredFor {
    implicit request => application =>
      logger.debug(s"POST request for ${request.path}")
      validation.bindFromRequest().fold(
        hasErrors => {
          logger.debug(s"Form binding error: ${hasErrors.prettyPrint.mkString(", ")}")
          Ok(stepPage(InProgressForm(hasErrors))) storeInSession application
        },
        success => {
          logger.debug(s"Form binding successful")
          if (success.previousAddress.get.findAddress) {
            (Ok(stepPage(lookupAddress(success)))) storeInSession application
          }
          else {
            val mergedApplication = success.merge(application)
            goToNext(mergedApplication) storeInSession mergedApplication
          }
        }
      )
  }

  def template(form:InProgressForm[InprogressOrdinary], call:Call): Html = {
    val possibleAddresses = form(keys.possibleAddresses.jsonList).value match {
      case Some(possibleAddressJS) if !possibleAddressJS.isEmpty => {
        serialiser.fromJson[Addresses](possibleAddressJS)
      }
      case _ => Addresses(List.empty)
    }
    val possiblePostcode = form(keys.possibleAddresses.postcode).value

    val possible = possiblePostcode.map(PossibleAddress(possibleAddresses, _))
    views.html.steps.previousAddress(form, call, possible)
  }
  def nextStep(currentState: InprogressOrdinary) = {
    OtherAddressController.otherAddressStep
  }

  def lookupAddress(success: InprogressOrdinary): InProgressForm[InprogressOrdinary] = {
    val postcode = success.previousAddress.get.previousAddress.get.postcode
    val addressesList = addressService.lookupPartialAddress(postcode)
    val inProgressForm = InProgressForm(
      validation.fill(
        success.copy(
          possibleAddresses = Some(PossibleAddress(Addresses(addressesList), postcode))
        )
      )
    )
    inProgressForm
  }
}

