package assets.mustache.forces

import org.scalatest.{Matchers, FlatSpec}
import play.api.test._
import org.jsoup.Jsoup
import play.api.test.Helpers._
import uk.gov.gds.ier.transaction.forces.nino.NinoMustache

class NinoTemplateTest
  extends FlatSpec
  with NinoMustache
  with Matchers {

  it should "properly render all properties from the model" in {

    running(FakeApplication()) {
      val data = NinoModel(
        question = Question(postUrl = "/whatever-url",
        backUrl = "",
        number = "6",
        title = "nino title"
        ),
        nino = Field(
          id = "ninoId",
          name = "ninoName",
          value = "AB123456C"
        ),
        noNinoReason = Field(
          id = "noNinoReasonId",
          name = "noNinoReasonName",
          value = "noNinoReason"
        ),
        noNinoReasonShowFlag = Text (
          value = "noNinoReasonShowFlag"
        )
      )

      val html = Mustache.render("forces/nino", data)
      val doc = Jsoup.parse(html.toString)

      val ninoInput = doc.select("input[id=ninoId]").first()
      ninoInput.attr("id") should be("ninoId")
      ninoInput.attr("name") should be("ninoName")
      ninoInput.attr("value") should be("AB123456C")

      val noNinoReasonInput = doc.select("textarea[id=noNinoReasonId]").first()
      noNinoReasonInput.attr("id") should be("noNinoReasonId")
      noNinoReasonInput.attr("name") should be("noNinoReasonName")
      noNinoReasonInput.text() should be("noNinoReason")

    }
  }
}