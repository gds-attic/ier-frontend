package uk.gov.gds.ier.transaction.overseas.confirmation

import uk.gov.gds.ier.mustache.StepMustache

import uk.gov.gds.ier.validation.ErrorTransformForm
import uk.gov.gds.ier.transaction.overseas.confirmation.blocks.{ConfirmationQuestion, ConfirmationBlocks}
import uk.gov.gds.ier.transaction.overseas.InprogressOverseas

trait ConfirmationMustache {

  case class ErrorModel(
      startUrl: String
  )

  case class ConfirmationModel(
      applicantDetails: List[ConfirmationQuestion],
      parentDetails: List[ConfirmationQuestion],
      displayParentBlock: Boolean,
      postUrl: String
  )

  object Confirmation extends StepMustache {
    def confirmationPage(
        form: ErrorTransformForm[InprogressOverseas],
        postUrl: String) = {

      val confirmation = new ConfirmationBlocks(form)
      val parentData = confirmation.parentBlocks()
      val applicantData = confirmation.applicantBlocks()

      val data = ConfirmationModel(
        parentDetails = parentData,
        applicantDetails = applicantData,
        displayParentBlock = !parentData.isEmpty,
        postUrl = postUrl
      )

      val content = Mustache.render("overseas/confirmation", data)

      MainStepTemplate(
        content,
        "Confirm your details - Register to vote",
        contentClasses = Some("confirmation")
      )
    }
  }
}
