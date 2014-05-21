package uk.gov.gds.ier.transaction.ordinary.postalVote

import uk.gov.gds.ier.validation.{EmailValidator, ErrorTransformForm, ErrorMessages, FormKeys}
import uk.gov.gds.ier.model.{PostalVote, PostalVoteDeliveryMethod, Contact}
import play.api.data.Forms._
import uk.gov.gds.ier.validation.constraints.PostalVoteConstraints
import uk.gov.gds.ier.transaction.ordinary.InprogressOrdinary
import play.api.data.validation.{Valid, Invalid, Constraint}

trait PostalVoteForms {
  self:  FormKeys
    with ErrorMessages =>

  val postalVoteForm = ErrorTransformForm(
    mapping(
      keys.postalVote.key -> optional(PostalVote.mapping),
      keys.contact.key -> optional(Contact.mapping)
    ) (
      (postalVote, contact) => InprogressOrdinary(
        postalVote = postalVote,
        contact = contact
      )
    ) (
      inprogress => Some(
        inprogress.postalVote,
        inprogress.contact
      )
    ) verifying (
      validPostVoteOption,
      validEmailAddressIfProvided,
      questionIsAnswered,
      emailProvidedIfEmailAnswered
    )
  )

  lazy val validPostVoteOption = Constraint[InprogressOrdinary](
    keys.postalVote.deliveryMethod.key
  ) { application =>
    application.postalVote match {
      case Some(PostalVote(Some(true), None)) => Invalid(
        "ordinary_postalVote_error_pleaseAnswer",
        keys.postalVote.deliveryMethod.methodName
      )
      case _ => Valid
    }
  }

  lazy val validEmailAddressIfProvided = Constraint[InprogressOrdinary](
    keys.postalVote.deliveryMethod.emailAddress.key
  ) { application =>
    val deliveryMethod = application.postalVote.flatMap(_.deliveryMethod)
    val emailAddress = deliveryMethod.flatMap(_.emailAddress)
    emailAddress match {
      case Some(email) if !EmailValidator.isValid(emailAddress) => Invalid(
        "ordinary_postalVote_error_pleaseEnterValidEmail",
        keys.postalVote.deliveryMethod.emailAddress
      )
      case _ => Valid
    }
  }

  lazy val questionIsAnswered = Constraint[InprogressOrdinary](
    keys.postalVote.optIn.key
  ) { application =>
    application.postalVote.flatMap(_.postalVoteOption) match {
      case Some(_) => Valid
      case None => Invalid(
        "ordinary_postalVote_error_pleaseAnswer",
        keys.postalVote.optIn
      )
    }
  }

  lazy val emailProvidedIfEmailAnswered = Constraint[InprogressOrdinary](
    keys.postalVote.deliveryMethod.emailAddress.key
  ) { application =>
    val deliveryMethod = application.postalVote.flatMap(_.deliveryMethod)
    val methodName = deliveryMethod.flatMap(_.deliveryMethod)
    val emailAddress = deliveryMethod.flatMap(_.emailAddress)

    (methodName, emailAddress) match {
      case (Some("email"), None) => Invalid(
        "ordinary_postalVote_error_enterYourEmail",
        keys.postalVote.deliveryMethod.emailAddress
      )
      case _ => Valid
    }
  }


}

