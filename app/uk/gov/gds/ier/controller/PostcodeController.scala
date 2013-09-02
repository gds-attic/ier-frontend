package uk.gov.gds.ier.controller

import com.google.inject.Inject
import uk.gov.gds.ier.service.PostcodeAnywhereService
import play.api.mvc.Controller
import play.api.mvc.Action
import uk.gov.gds.ier.client.ApiResults
import uk.gov.gds.ier.serialiser.{JsonSerialiser, WithSerialiser}
import uk.gov.gds.common.http.ApiResponseException
import uk.gov.gds.ier.exception.PostcodeLookupFailedException
import uk.gov.gds.ier.model.IerForms

class PostcodeController @Inject()(postcodeAnywhere: PostcodeAnywhereService, serialiser: JsonSerialiser)
  extends Controller with ApiResults with WithSerialiser with IerForms {

  def toJson(obj: AnyRef): String = serialiser.toJson(obj)

  def fromJson[T](json: String)(implicit m: Manifest[T]): T = serialiser.fromJson[T](json)

  def lookup(postcode: String) = Action {
    implicit request =>
      postcodeForm.bind(Map("postcode" -> postcode)).fold(
        errors => badResult("errors" -> errors.errorsAsMap),
        postcode =>
          try {
            okResult("addresses" -> postcodeAnywhere.lookup(postcode))
          } catch {
            case e:PostcodeLookupFailedException => serverErrorResult("error" -> e.getMessage)
          }
      )
  }
}
