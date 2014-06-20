package uk.gov.gds.ier.localAuthority

import com.google.inject.Inject
import play.api.mvc.Controller
import play.api.mvc.Action
import uk.gov.gds.ier.client.ApiResults
import uk.gov.gds.ier.serialiser.{ JsonSerialiser, WithSerialiser }
import uk.gov.gds.ier.validation.IerForms
import uk.gov.gds.ier.session.RequestHandling
import uk.gov.gds.ier.guice.WithEncryption
import uk.gov.gds.ier.security.EncryptionService
import uk.gov.gds.ier.service.LocateService
import uk.gov.gds.ier.service.apiservice.ConcreteIerApiService
import uk.gov.gds.ier.transaction.ordinary.InprogressOrdinary
import uk.gov.gds.ier.transaction.forces.InprogressForces
import uk.gov.gds.ier.transaction.crown.InprogressCrown
import uk.gov.gds.ier.transaction.overseas.InprogressOverseas
import uk.gov.gds.ier.exception.PostcodeLookupFailedException
import uk.gov.gds.ier.validation.FormKeys
import uk.gov.gds.ier.logging.Logging
import controllers.routes.LocalAuthorityController
import uk.gov.gds.ier.model.LocalAuthority
import uk.gov.gds.ier.service.AddressService
import uk.gov.gds.ier.guice.WithRemoteAssets
import uk.gov.gds.ier.guice.WithConfig
import uk.gov.gds.ier.config.Config
import uk.gov.gds.ier.assets.RemoteAssets
import uk.gov.gds.ier.langs.Messages

class LocalAuthorityController @Inject() (
    val locateService: LocateService,
    val ierApiService: ConcreteIerApiService,
    val addressService: AddressService,
    val serialiser: JsonSerialiser,
    val encryptionService: EncryptionService,
    val config: Config,
    val remoteAssets: RemoteAssets
) extends Controller
  with ApiResults
  with WithSerialiser
  with FormKeys
  with RequestHandling
  with WithEncryption
  with Logging
  with LocalAuthorityLookupForm
  with IerForms
  with LocalAuthorityMustache
  with WithRemoteAssets
  with WithConfig {


  def show = Action {
    implicit request =>

      val sourcePath = request.headers.get("referer").getOrElse("")

      val optGssCode =
        if (sourcePath.contains("overseas")) {
          request.getApplication[InprogressOverseas] flatMap (_.lastUkAddress flatMap (_.gssCode))
        }
        else if (sourcePath.contains("crown")) {
          request.getApplication[InprogressCrown] flatMap (_.address flatMap (_.address flatMap (_.gssCode)))
        }
        else if (sourcePath.contains("forces")) {
          request.getApplication[InprogressForces] flatMap (_.address flatMap (_.address flatMap (_.gssCode)))
        }
        else {
          request.getApplication[InprogressOrdinary] flatMap (_.address flatMap (_.gssCode))
        }

      optGssCode match {
        case Some(gssCode) => {
          val localAuthority = ierApiService.getLocalAuthorityByGssCode(gssCode)
          Ok(views.html.localAuthority(localAuthority, sourcePath))
        }
        case None => {
          Ok(views.html.localAuthorityLookup(sourcePath))
        }
      }
  }

  def ero(gssCode: String, sourcePath: Option[String]) = Action { request =>
    val localAuthority = ierApiService.getLocalAuthorityByGssCode(gssCode)
    Ok(LocalAuthorityPage(localAuthority, sourcePath))
  }

  def doLookup(sourcePath: Option[String]) = Action { implicit request =>
    localAuthorityLookupForm.bindFromRequest().fold(
      hasErrors => BadRequest(LocalAuthorityLookupPage(
        hasErrors,
        sourcePath,
        controllers.routes.LocalAuthorityController.showLookup(sourcePath).url
      )),
      success => {
        val gssCode = locateService.lookupGssCode(success.postcode)
        gssCode match {
          case Some(gss) => Redirect(
            controllers.routes.LocalAuthorityController.ero(gss, sourcePath)
          )
          case None => BadRequest(
            LocalAuthorityLookupPage(
              localAuthorityLookupForm.fill(success).withGlobalError(
                Messages("lookup_error_noneFound")
              ),
              sourcePath,
              controllers.routes.LocalAuthorityController.showLookup(sourcePath).url
            )
          )
        }
      }
    )
  }

  def showLookup(sourcePath: Option[String]) = Action { implicit request =>
    Ok(LocalAuthorityLookupPage(
      localAuthorityLookupForm,
      sourcePath,
      controllers.routes.LocalAuthorityController.showLookup(sourcePath).url
    ))
  }
}
