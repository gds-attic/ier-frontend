package uk.gov.gds.ier.transaction.overseas.confirmation.blocks

import controllers.step.overseas.routes
import uk.gov.gds.ier.transaction.shared.{BlockContent, BlockError}

trait PassportBlocks {
  self: ConfirmationBlock =>

  private val hasPassport = Some("true")
  private val noPassport = Some("false")
  private val bornInUk = Some("true")
  private val notBornInUk = Some("false")
  private val notBornBefore1983 = Some(false)
  private val bornBefore1983 = Some(true)

  def passport = {
    val passport = form(keys.passport.hasPassport).value
    val birth = form(keys.passport.bornInsideUk).value

    (passport, birth, form.bornBefore1983) match {
      case (`hasPassport`, _, _) => passportDetails
      case (`noPassport`, `notBornInUk`, _) => citizenDetails
      case (`noPassport`, `bornInUk`, `notBornBefore1983`) => citizenDetails
      case (`noPassport`, `bornInUk`, `bornBefore1983`) => {
        ConfirmationQuestion(
          title = "British Passport Details",
          editLink = routes.PassportCheckController.editGet.url,
          changeName = "your passport details",
          content = BlockContent(List("I was born in the UK but I don't have a British passport"))
        )
      }
      case _ => ConfirmationQuestion(
        title = "British passport",
        editLink = routes.PassportCheckController.editGet.url,
        changeName = "your passport details",
        content = BlockContent(List(completeThisStepMessage))
      )
    }
  }

  def citizenDetails = {
    val howBecameCitizen = form(keys.passport.citizenDetails.howBecameCitizen).value
    val dateBecameCitizen = form.dateBecameCitizen.map { date =>
      s"${date.getDayOfMonth} ${date.getMonthOfYear} ${date.getYear}"
    }

    val citizenContent = for (
      how <- howBecameCitizen;
      date <- dateBecameCitizen
    ) yield {
      List(
        s"How you became a citizen: $how",
        s"Date you became a citizen: $date")
    }

    val route = if(form(keys.passport).hasErrors) {
      routes.PassportCheckController.editGet
    } else {
      routes.CitizenDetailsController.editGet
    }

    ConfirmationQuestion(
      title = "British citizenship",
      editLink = route.url,
      changeName = "your citizenship details",
      content = ifComplete(keys.passport) {
        List(citizenContent.getOrElse(completeThisStepMessage))
      }
    )
  }

  def passportDetails = {
    val passportNumber = form(keys.passport.passportDetails.passportNumber).value
    val authority = form(keys.passport.passportDetails.authority).value
    val issueDate = for(
      day <- form(keys.passport.passportDetails.issueDate.day).value;
      month <- form(keys.passport.passportDetails.issueDate.month).value;
      year <- form(keys.passport.passportDetails.issueDate.year).value
    ) yield s"$day $month $year"

    val passportContent = for(
      num <- passportNumber;
      auth <- authority;
      date <- issueDate
    ) yield {
      List(
        s"Passport Number: $num",
        s"Authority: $auth",
        s"Issue Date: $date")
    }

    val route = if(form(keys.passport).hasErrors) {
      routes.PassportCheckController.editGet
    } else {
      routes.PassportDetailsController.editGet
    }

    ConfirmationQuestion(
      title = "British Passport Details",
      editLink = route.url,
      changeName = "your passport details",
      content = {
        if (form(keys.passport).hasErrors || passportContent.isEmpty) {
          BlockError(completeThisStepMessage)
        } else {
          BlockContent(passportContent.get)
        }
      }
    )
  }
}
