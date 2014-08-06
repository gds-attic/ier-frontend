package uk.gov.gds.ier.localAuthority

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import uk.gov.gds.ier.validation.ErrorMessages
import uk.gov.gds.ier.validation.FormKeys
import uk.gov.gds.ier.test.TestHelpers
import uk.gov.gds.ier.test.WithMockConfig
import uk.gov.gds.ier.test.WithMockRemoteAssets
import uk.gov.gds.ier.serialiser.WithSerialiser
import org.mockito.Mockito._
import org.mockito.Matchers._
import play.api.mvc.Call
import uk.gov.gds.ier.model.LocalAuthorityContactDetails
import play.api.test.Helpers._
import org.jsoup.Jsoup

class LocalAuthorityMustacheTests
  extends FlatSpec
  with Matchers
  with LocalAuthorityLookupForm
  with LocalAuthorityMustache
  with ErrorMessages
  with FormKeys
  with TestHelpers
  with WithMockConfig
  with WithMockRemoteAssets
  with WithSerialiser{

  val serialiser = jsonSerialiser

  behavior of "LocalAutorityPage"
  it should "render Local Authority information" in runningApp {
    val authorityDetails = LocalAuthorityContactDetails(
      name = Some("authority name"),
      url = Some("http://localhost"),
      addressLine1 = Some("addressLine1"),
      addressLine2 = Some("addressLine2"),
      postcode = Some("ab123cd"),
      emailAddress = Some("test@test.com"),
      phoneNumber = Some("123456")
    )
    val authorityPage = LocalAuthorityShowPage(Some(authorityDetails), Some("/test"))
    when(remoteAssets.messages(any[String])).thenReturn(Call("GET", "/assests/messages"))

    val doc = Jsoup.parse(authorityPage.body)

    val authorityName = doc.select("header h1").first
    authorityName should not be (null)
    authorityName.text should include("Contact authority name")

    val urlText = doc.select("p#authority_url").first()
    urlText should not be (null)
    urlText.text should include("Visit the authority name website")

    val urlLink= doc.select("a#url").first()
    urlLink should not be (null)
    urlLink.toString should include("http://localhost")
    urlLink.text should include("authority name website")

    val email = doc.select("p#authority_email").first
    email should not be (null)
    email.text should include ("test@test.com")

    val phone = doc.select("p#authority_phone").first
    phone should not be (null)
    phone.text should include ("123456")

    val address = doc.select("p#authority_address").first
    address should not be (null)
    address.text should include("authority name")
    address.text should include ("addressLine1")
    address.text should include ("addressLine2")

    val postcodeText = doc.select("p#authority_postcode").first
    postcodeText should not be (null)
    postcodeText.text should include ("ab123cd")
  }

  it should "render Local authority information without URL pointing to the ERO" in runningApp {
    val authorityDetails = LocalAuthorityContactDetails(
      name = Some("authority name"),
      url = Some(""),
      addressLine1 = Some("addressLine1"),
      addressLine2 = Some("addressLine2"),
      postcode = Some("ab123cd"),
      emailAddress = Some("test@test.com"),
      phoneNumber = Some("123456")
    )
    val authorityPage = LocalAuthorityShowPage(Some(authorityDetails), Some("/test"))
    when(remoteAssets.messages(any[String])).thenReturn(Call("GET", "/assests/messages"))

    val doc = Jsoup.parse(authorityPage.body)

    doc.toString should not include "Visit the authority name website"
    doc.toString should not include "http://localhost"

    val urlText = doc.select("p#authority_url").first
    urlText should be (null)

    val urlLink= doc.select("a#url").first()
    urlLink should be (null)
  }

  behavior of "LocalAuthorityLookupPage"
  it should "render the lookup page" in runningApp {
    val lookupPage = LocalAuthorityPostcodePage(
      postcode = Field(id = "postcode_id", name = "postcode_name", classes = "postcode_classes",
        value = "postcode_value"),
      sourcePath = "/sourcePath",
      postUrl = "/postUrl"
    )
    when(remoteAssets.messages(any[String])).thenReturn(Call("GET", "/assests/messages"))
    val doc = Jsoup.parse(lookupPage.body)

    val form = doc.select("form").first()
    form should not be (null)
    form.attr("action") should be ("/postUrl")

    val postcodeField = doc.select("input").first
    postcodeField.attr("id") should be ("postcode_id")
    postcodeField.attr("name") should be ("postcode_name")
    postcodeField.attr("class") should include ("postcode_classes")
    postcodeField.attr("value") should be ("postcode_value")
  }

}
