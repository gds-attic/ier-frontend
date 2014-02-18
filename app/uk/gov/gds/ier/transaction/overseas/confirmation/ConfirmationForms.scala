package uk.gov.gds.ier.transaction.overseas.confirmation

import org.joda.time.{YearMonth, Years}
import play.api.data.Forms._
import play.api.data.validation.{Invalid, Valid, Constraint}
import uk.gov.gds.ier.model._
import uk.gov.gds.ier.form.OverseasFormImplicits
import uk.gov.gds.ier.serialiser.WithSerialiser
import uk.gov.gds.ier.validation.{ErrorTransformForm, FormKeys, Key, ErrorMessages}
import uk.gov.gds.ier.validation.constraints.CommonConstraints
import uk.gov.gds.ier.transaction.overseas.lastUkAddress.LastUkAddressForms
import uk.gov.gds.ier.transaction.overseas.previouslyRegistered.PreviouslyRegisteredForms
import uk.gov.gds.ier.transaction.overseas.dateLeftSpecial.DateLeftSpecialForms
import uk.gov.gds.ier.transaction.overseas.dateLeftUk.DateLeftUkForms
import uk.gov.gds.ier.transaction.overseas.dateOfBirth.DateOfBirthForms
import uk.gov.gds.ier.transaction.overseas.lastRegisteredToVote.LastRegisteredToVoteForms
import uk.gov.gds.ier.transaction.overseas.nino.NinoForms
import uk.gov.gds.ier.transaction.overseas.name.NameForms
import uk.gov.gds.ier.transaction.overseas.openRegister.OpenRegisterForms
import uk.gov.gds.ier.transaction.overseas.contact.ContactForms
import uk.gov.gds.ier.transaction.overseas.passport.PassportForms
import uk.gov.gds.ier.transaction.overseas.address.AddressForms
import uk.gov.gds.ier.transaction.overseas.waysToVote.WaysToVoteForms
import uk.gov.gds.ier.transaction.overseas.applicationFormVote.PostalOrProxyVoteForms

trait ConfirmationForms
  extends FormKeys
  with ErrorMessages
  with WithSerialiser
  with PreviouslyRegisteredForms
  with DateLeftSpecialForms
  with DateLeftUkForms
  with DateOfBirthForms
  with LastRegisteredToVoteForms
  with NinoForms
  with AddressForms
  with LastUkAddressForms
  with OpenRegisterForms
  with NameForms
  with PassportForms
  with WaysToVoteForms
  with PostalOrProxyVoteForms
  with ContactForms
  with OverseasFormImplicits
  with CommonConstraints {

  val stubMapping = mapping(
    "foo" -> text
  ) (foo => Stub()) (stub => Some("foo"))

  val optInMapping = single(
    keys.optIn.key -> boolean
  )

  val confirmationForm = ErrorTransformForm(
    mapping(
      keys.name.key -> optional(nameMapping),
      keys.previousName.key -> optional(previousNameMapping),
      keys.previouslyRegistered.key -> optional(previouslyRegisteredMapping),
      keys.dateLeftSpecial.key -> optional(dateLeftSpecialMapping),
      keys.dateLeftUk.key -> optional(dateLeftUkMapping),
      keys.lastRegisteredToVote.key -> optional(lastRegisteredToVoteMapping),
      keys.dob.key -> optional(dobMapping),
      keys.nino.key -> optional(ninoMapping),
      keys.lastUkAddress.key -> optional(partialAddressMapping),
      keys.overseasAddress.key -> optional(addressMapping),
      keys.openRegister.key -> optional(optInMapping),
      keys.waysToVote.key -> optional(waysToVoteMapping),
      keys.postalOrProxyVote.key -> optional(postalOrProxyVoteMapping),
      keys.contact.key -> optional(contactMapping),
      keys.passport.key -> optional(passportMapping),
      "parentsName" -> optional(stubMapping),
      "parentsPreviousName" -> optional(stubMapping),
      "parentsAddress" -> optional(stubMapping),
      keys.possibleAddresses.key -> optional(possibleAddressesMapping)
    )
    (InprogressOverseas.apply)
    (InprogressOverseas.unapply)
    verifying (validateOverseas)
  )

  lazy val validateOverseas = Constraint[InprogressOverseas]("validateOverseas") { application =>
    import uk.gov.gds.ier.model.ApplicationType._

    application.identifyApplication match {
      case YoungVoter => validateYoungVoter(application)
      case NewVoter => validateNewVoter(application)
      case RenewerVoter => validateRenewerVoter(application)
      case SpecialVoter => validateSpecialVoter(application)
      case DontKnow => validateBaseSetRequired(application)
    }
  }

  lazy val validateBaseSetRequired = Constraint[InprogressOverseas]("validateBaseSet") {
    application => Invalid("Base set criteria not met")
  }

  lazy val validateYoungVoter = Constraint[InprogressOverseas]("validateYoungVoter") { app =>
    val errorKeys = List(
      if (app.name.isDefined) None else Some(keys.name),
      if (app.previousName.isDefined) None else Some(keys.previousName),
      if (app.previouslyRegistered.isDefined) None else Some(keys.previouslyRegistered),
      if (app.dateLeftUk.isDefined) None else Some(keys.dateLeftUk),
      if (app.dob.isDefined) None else Some(keys.dob),
      if (app.nino.isDefined) None else Some(keys.nino),
      if (app.address.isDefined) None else Some(keys.overseasAddress),
      if (app.openRegisterOptin.isDefined) None else Some(keys.openRegister),
      if (app.waysToVote.isDefined) None else Some(keys.waysToVote),
      if (app.postalOrProxyVote.isDefined) None else Some(keys.postalOrProxyVote),
      if (app.contact.isDefined) None else Some(keys.contact),
      if (app.passport.isDefined) None else Some(keys.passport),
      if (app.parentsName.isDefined) None else Some(Key("parentsName")),
      if (app.parentsPreviousName.isDefined) None else Some(Key("parentsPreviousName")),
      if (app.parentsAddress.isDefined) None else Some(Key("parentsAddress"))

    ).flatten
    if (errorKeys.size == 0) {
      Valid
    } else {
      Invalid ("Please complete this step", errorKeys:_*)
    }
  }

  lazy val validateSpecialVoter = Constraint[InprogressOverseas]("validateSpecialVoter") { app =>
    val errorKeys = List(
      if (app.name.isDefined) None else Some(keys.name),
      if (app.previousName.isDefined) None else Some(keys.previousName),
      if (app.previouslyRegistered.isDefined) None else Some(keys.previouslyRegistered),
      if (app.dateLeftSpecial.isDefined) None else Some(keys.dateLeftSpecial),
      if (app.dob.isDefined) None else Some(keys.dob),
      if (app.nino.isDefined) None else Some(keys.nino),
      if (app.address.isDefined) None else Some(keys.overseasAddress),
      if (app.openRegisterOptin.isDefined) None else Some(keys.openRegister),
      if (app.waysToVote.isDefined) None else Some(keys.waysToVote),
      if (app.postalOrProxyVote.isDefined) None else Some(keys.postalOrProxyVote),
      if (app.contact.isDefined) None else Some(keys.contact),
      if (app.passport.isDefined) None else Some(keys.passport)
    ).flatten
    if (errorKeys.size == 0) {
      Valid
    } else {
      Invalid ("Please complete this step", errorKeys:_*)
    }
  }

  lazy val validateNewVoter = Constraint[InprogressOverseas]("validateNewVoter") { app =>
    val errorKeys = List(
      if (app.name.isDefined) None else Some(keys.name),
      if (app.previousName.isDefined) None else Some(keys.previousName),
      if (app.previouslyRegistered.isDefined) None else Some(keys.previouslyRegistered),
      if (app.dateLeftUk.isDefined) None else Some(keys.dateLeftUk),
      if (app.dob.isDefined) None else Some(keys.dob),
      if (app.nino.isDefined) None else Some(keys.nino),
      if (app.address.isDefined) None else Some(keys.overseasAddress),
      if (app.openRegisterOptin.isDefined) None else Some(keys.openRegister),
      if (app.waysToVote.isDefined) None else Some(keys.waysToVote),
      if (app.postalOrProxyVote.isDefined) None else Some(keys.postalOrProxyVote),
      if (app.contact.isDefined) None else Some(keys.contact),
      if (app.passport.isDefined) None else Some(keys.passport)
    ).flatten
    if (errorKeys.size == 0) {
      Valid
    } else {
      Invalid ("Please complete this step", errorKeys:_*)
    }
  }

  lazy val validateRenewerVoter = Constraint[InprogressOverseas]("validateRenewerVoter") { app =>
    val validationErrors = Seq (
      if (app.dob.isDefined) None else Some(keys.dob),
      if (!app.previouslyRegistered.exists(_.hasPreviouslyRegistered == true))
        Some(keys.previouslyRegistered) else None,
      if (app.dateLeftUk.isDefined) None else Some(keys.dateLeftUk),
      if (app.lastUkAddress.isDefined) None else Some(keys.lastUkAddress),
      if (app.name.isDefined) None else Some(keys.name),
      if (app.previousName.isDefined) None else Some(keys.previousName),
      if (app.nino.isDefined) None else Some(keys.nino),
      if (app.address.isDefined) None else Some(keys.overseasAddress),
      if (app.openRegisterOptin.isDefined) None else Some(keys.openRegister),
      if (app.waysToVote.isDefined) None else Some(keys.waysToVote),
      if (app.postalOrProxyVote.isDefined) None else Some(keys.postalOrProxyVote),
      if (app.contact.isDefined) None else Some(keys.contact)
    ).flatten

    if (validationErrors.size == 0)
      Valid
    else
      Invalid ("Please complete this step", validationErrors:_*)
  }
}
