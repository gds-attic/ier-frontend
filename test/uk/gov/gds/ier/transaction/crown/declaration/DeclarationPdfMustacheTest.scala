package uk.gov.gds.ier.transaction.crown.declaration

import org.scalatest.{GivenWhenThen, Matchers, FlatSpec}
import uk.gov.gds.ier.test.{WithMockRemoteAssets, TestHelpers}
import uk.gov.gds.ier.transaction.crown.InprogressCrown
import controllers.step.crown.routes._
import uk.gov.gds.ier.service.{DeclarationPdfDownloadService, WithDeclarationPdfDownloadService}
import org.specs2.mock.Mockito
import uk.gov.gds.ier.serialiser.{JsonSerialiser, WithSerialiser}
import uk.gov.gds.ier.model.{HasAddressOption, LastAddress, PartialAddress}

class DeclarationPdfMustacheTest
  extends FlatSpec
  with Matchers
  with GivenWhenThen
  with Mockito
  with DeclarationPdfForms
  with TestHelpers
  with DeclarationPdfMustache
  with WithSerialiser
  with WithDeclarationPdfDownloadService
  with WithMockRemoteAssets {

  val declarationPdfDownloadService = mock[DeclarationPdfDownloadService]
  val serialiser = mock[JsonSerialiser]

  it should "construct model for declaration step with election authority details from mocked service" in {
    val emptyApplication = InprogressCrown()
    val model: DeclarationPdfModel = mustache.data(
      declarationPdfForm.fill(inprogressApplicationWithPostcode("WR26NJ")),
      DeclarationPdfController.post,
      emptyApplication
    ).asInstanceOf[DeclarationPdfModel]

    model.question.title should be("Download your service declaration form")
    model.question.postUrl should be("/register-to-vote/crown/declaration-pdf")
  }

  private def inprogressApplicationWithPostcode(postcode: String) = {
    InprogressCrown().copy(
        address = Some(LastAddress(
          hasAddress = Some(HasAddressOption.YesAndLivingThere),
          address = Some(PartialAddress(
            addressLine = None,
            uprn = None,
            manualAddress = None,
            postcode = postcode)))))
  }
}
