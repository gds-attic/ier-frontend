package uk.gov.gds.ier.transaction.ordinary.nino

import org.scalatest.{Matchers, FlatSpec}
import uk.gov.gds.ier.transaction.ordinary.InprogressOrdinary
import uk.gov.gds.ier.validation.{FormKeys, ErrorMessages}
import uk.gov.gds.ier.serialiser.WithSerialiser
import play.api.libs.json.Json
import uk.gov.gds.ier.test.TestHelpers
import controllers.step.ordinary.routes._

class NinoMustacheTests
  extends FlatSpec
  with Matchers
  with NinoForms
  with NinoMustache
  with WithSerialiser
  with ErrorMessages
  with FormKeys
  with TestHelpers {

  val serialiser = jsonSerialiser

  it should "successfully render to a valid nino" in {
    val js = Json.toJson(
      Map(
        "NINO.NINO" -> "AB 12 34 56 D"
      )
    )
    val newNinoForm = ninoForm.bind(js)
    val model = mustache.data(
      newNinoForm,
      NinoController.post,
      Some(NameController.get),
      InprogressOrdinary()
    ).data.asInstanceOf[NinoModel]
    model.nino.value should be("AB 12 34 56 D")
  }

  it should "successfully render to a valid 'no nino reason'" in {
    val js = Json.toJson(
      Map(
        "NINO.NoNinoReason" -> "Don't have any NINO"
      )
    )
    val newNinoForm = ninoForm.bind(js)
    val model = mustache.data(
        newNinoForm,
        NinoController.post,
        Some(NameController.get),
        InprogressOrdinary()
    ).data.asInstanceOf[NinoModel]
    model.noNinoReason.value should be("Don't have any NINO")
  }
}
