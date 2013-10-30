package uk.gov.gds.ier.controller

import play.api.mvc._
import uk.gov.gds.ier.model.{InProgressSession, Steps}
import com.google.inject.Inject
import uk.gov.gds.ier.service.{PlacesService, IerApiService}
import views._
import controllers._
import uk.gov.gds.ier.serialiser.{WithSerialiser, JsonSerialiser}
import uk.gov.gds.ier.validation.{InProgressForm, IerForms}
import scala.Some
import uk.gov.gds.common.model.{Ero, LocalAuthority}
import org.slf4j.LoggerFactory

class RegisterToVoteController @Inject() (ierApi:IerApiService, serialiser: JsonSerialiser, placesService:PlacesService)
    extends Controller
    with IerForms
    with WithSerialiser
    with Steps
    with InProgressSession {

  def logger = LoggerFactory.getLogger(this.getClass)

  def toJson(obj: AnyRef): String = serialiser.toJson(obj)

  def fromJson[T](json: String)(implicit m: Manifest[T]): T = serialiser.fromJson[T](json)

  def index = Action { implicit request =>
    Ok(html.start()).withFreshSession()
  }

  def registerToVote = ValidSession requiredFor {
    request => application =>
      Redirect(controllers.routes.RegisterToVoteController.registerStep(firstStep()))
  }

  def registerStep(step:String) = ValidSession requiredFor {
    request => application =>
      Step(step) { stepDetail =>
        Ok(stepDetail.page(InProgressForm(application)))
      }
  }

  def validateStep(step:String) = ValidSession withParser(BodyParsers.parse.urlFormEncoded) requiredFor {
    implicit request => application =>
      Step(step) { stepDetail =>
        stepDetail.validation.bindFromRequest().fold(
          errors => Ok(stepDetail.page(InProgressForm(errors))),
          form => Redirect(routes.RegisterToVoteController.registerStep(stepDetail.next)).mergeWithSession(form)
        )
      }
  }

  def edit(step:String) = ValidSession requiredFor {
    request => application =>
      Step(step) { stepDetail =>
        Ok(stepDetail.editPage(InProgressForm(application)))
      }
  }

  def validateEdit(step:String) = ValidSession withParser(BodyParsers.parse.urlFormEncoded) requiredFor {
    implicit request => application =>
      Step(step) { stepDetail =>
        stepDetail.validation.bindFromRequest()(request).fold(
          errors => Ok(stepDetail.page(InProgressForm(errors))),
          form => Redirect(routes.RegisterToVoteController.confirmApplication()).mergeWithSession(form)
        )
      }
  }

  def errorRedirect(error:String) = Action {
    Redirect(routes.RegisterToVoteController.error()).flashing("error-type" -> error)
  }

  def error = Action {
    implicit request =>
      flash.get("error-type") match {
        case Some("exit-unknown-dob") => Ok(html.errors.exitUnknownDob())
        case Some("exit-nationality") => Ok(html.errors.exitNationality())
        case Some("exit-dob") => Ok(html.errors.exitDob())
        case Some("exit-error") => Ok(html.errors.exitError())
      }
  }

  def complete = Action {
    implicit request =>
      val authority = request.flash.get("postcode") match {
        case Some("") => None
        case Some(postCode) => placesService.lookupAuthority(postCode)
        case None => None
      }
      val refNum = request.flash.get("refNum")

      Ok(html.complete(authority, refNum)).withNewSession
  }

  def fakeComplete = Action {
    val authority = Some(LocalAuthority("Tower Hamlets Borough Council", Ero(), "00BG", "E09000030"))
    Ok(html.complete(authority, Some("123456")))
  }

  def confirmApplication = ValidSession requiredFor {
    request => application =>
      Step("confirmation") { stepDetail =>
        Ok(stepDetail.page(InProgressForm(stepDetail.validation.fillAndValidate(application))))
      }
  }

  def submitApplication = ValidSession requiredFor {
    request => application =>
      inprogressForm.fillAndValidate(application).fold(
        errors => {
          Ok(Step.getStep("confirmation").page(InProgressForm(errors)))
        },
        validApplication => {
          val refNum = ierApi.generateReferenceNumber(validApplication)
          val remoteClientIP = request.headers.get("X-Real-IP")
          logger.debug("X-Real-IP header: "+remoteClientIP)
          ierApi.submitApplication(remoteClientIP, validApplication, Some(refNum))
          Redirect(controllers.routes.RegisterToVoteController.complete()).flashing(
            "refNum" -> refNum,
            "postcode" -> validApplication.address.map(_.postcode).getOrElse("")
          )
        }
      )
  }
}
