package uk.gov.gds.ier.transaction.crown.declaration

import uk.gov.gds.ier.step.StepTemplate
import uk.gov.gds.ier.transaction.crown.InprogressCrown
import controllers.step.crown._
import uk.gov.gds.common.model.LocalAuthority
import uk.gov.gds.ier.serialiser.WithSerialiser
import uk.gov.gds.ier.service.WithDeclarationPdfDownloadService

trait DeclarationPdfMustache extends StepTemplate[InprogressCrown] {
  self: WithPlacesService with WithSerialiser with WithDeclarationPdfDownloadService =>

  case class DeclarationPdfModel(
    question: Question,
    declarationPdfUrl: String,
    showAuthorityUrl: Boolean,
    authorityName: String,
    pdfFileSize: String
  ) extends MustacheData

  val pageTitle = "Download your service declaration form"

  val mustache = MustacheTemplate("crown/declarationPdf") { (form, postUrl) =>

    //val postcode = application.address flatMap {_.address} map {_.postcode}
    val postcode = form(keys.address.address.postcode).value
    val authorityDetails : Option[LocalAuthority] = postcode match {
      case Some("") => None
      case Some(postCode) => placesService.lookupAuthority(postCode)
      case None => None
    }

    implicit val progressForm = form
    DeclarationPdfModel(
      question = Question(
        postUrl = postUrl.url,
        number = "7",
        title = pageTitle,
        errorMessages = form.globalErrors.map ( _.message )
      ),
      declarationPdfUrl = routes.DeclarationPdfDownloadController.download.url,
      showAuthorityUrl = false,
      authorityName = authorityDetails map {
        auth => auth.name + " electoral registration office"
      } getOrElse "your local electoral registration office",
      pdfFileSize = declarationPdfDownloadService.fileSizeWithUnit
    )
  }
}
