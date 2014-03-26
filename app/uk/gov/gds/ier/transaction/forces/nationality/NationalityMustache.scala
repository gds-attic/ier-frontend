package uk.gov.gds.ier.transaction.forces.nationality

import uk.gov.gds.ier.validation.ErrorTransformForm
import uk.gov.gds.ier.model.InprogressForces
import play.api.mvc.Call
import play.api.templates.Html
import uk.gov.gds.ier.mustache.StepMustache
import uk.gov.gds.ier.validation.constants.NationalityConstants


trait NationalityMustache extends StepMustache {

  case class CountryItem (
      index:String = "",
      countryName:String = ""
  )

  case class NationalityModel(
      question:Question,
      britishOption: Field,
      irishOption: Field,
      hasOtherCountryOption: Field,
      otherCountriesHead: Field,
      otherCountriesTail: List[CountryItem] = List.empty,
      moreThanOneOtherCountry: Boolean,
      noNationalityReason: Field,
      noNationalityReasonShowFlag: Text
  )

  def transformFormStepToMustacheData(
      application:InprogressForces,
      form: ErrorTransformForm[InprogressForces],
      postEndpoint: Call,
      backEndpoint:Option[Call]) : NationalityModel = {

    implicit val progressForm = form

    val otherCountriesList = obtainOtherCountriesList(progressForm)

    NationalityModel(
      question = Question(
        postUrl = postEndpoint.url,
        backUrl = backEndpoint.map { call => call.url }.getOrElse(""),
        errorMessages = form.globalErrors.map{ _.message },
        number = "4",
        title = "What is your nationality?"
      ),
      britishOption = CheckboxField(
        key = keys.nationality.british,
        value = "true"
      ),
      irishOption = CheckboxField(
        key = keys.nationality.irish,
        value = "true"
      ),
      hasOtherCountryOption = CheckboxField(
        key = keys.nationality.hasOtherCountry,
        value = "true"
      ),
      otherCountriesHead =  Field(
        id = keys.nationality.otherCountries.asId() + "[0]",
        name = keys.nationality.otherCountries.key + "[0]",
        value = otherCountriesList match {
          case Nil => ""
          case headCountry :: tailCountries => headCountry
        },
        classes = if (progressForm(keys.nationality.otherCountries.key).hasErrors) "invalid" else ""
      ),

      otherCountriesTail =
        if (!otherCountriesList.isEmpty) createMustacheCountryList(otherCountriesList.tail)
        else List.empty,
      moreThanOneOtherCountry = otherCountriesList.size > 1,
      noNationalityReason= TextField(
        key = keys.nationality.noNationalityReason
      ),
      noNationalityReasonShowFlag = Text (
        value = progressForm(keys.nationality.noNationalityReason.key).value.map(noNationalityReason => "-open").getOrElse("")
      )
    )
  }

  def nationalityMustache(
      application:InprogressForces,
      form: ErrorTransformForm[InprogressForces],
      postEndpoint: Call,
      backEndpoint:Option[Call]) : Html = {
    val data = transformFormStepToMustacheData(application, form, postEndpoint, backEndpoint)
    val content = Mustache.render("forces/nationality", data)
    MainStepTemplate(content, data.question.title)
  }

  def createMustacheCountryList (otherCountriesTail:List[String]) : List[CountryItem] = {
    otherCountriesTail.zipWithIndex.map{case (item, i) => CountryItem((i+2).toString,item)}
  }

  def obtainOtherCountriesList(form: ErrorTransformForm[InprogressForces]):List[String] = {
    (
      for (i <- 0 until NationalityConstants.numberMaxOfOtherCountries
           if (form(otherCountriesKey(i)).value.isDefined)
             && !form(otherCountriesKey(i)).value.get.isEmpty)
      yield form(otherCountriesKey(i)).value.get
      ).toList
  }

  def otherCountriesKey(i:Int) = keys.nationality.otherCountries.key + "["+i+"]"
}
