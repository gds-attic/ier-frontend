package uk.gov.gds.ier.transaction.overseas.applicationFormVote

import uk.gov.gds.ier.validation.{ErrorTransformForm, ErrorMessages, FormKeys}
import uk.gov.gds.ier.model.{
  PostalOrProxyVote,
  InprogressOverseas,
  PostalVoteDeliveryMethod,
  WaysToVoteType}
import play.api.data.Forms._
import uk.gov.gds.ier.validation.constraints.overseas.PostalOrProxyVoteConstraints

trait PostalOrProxyVoteForms extends PostalOrProxyVoteConstraints {
  self:  FormKeys
    with ErrorMessages =>

  lazy val voteDeliveryMethodMapping = mapping(
    keys.methodName.key -> optional(nonEmptyText),
    keys.emailAddress.key -> optional(nonEmptyText)
  )(
    PostalVoteDeliveryMethod.apply
  )(
    PostalVoteDeliveryMethod.unapply
  ) verifying (validDeliveryMethod)

  lazy val postalOrProxyVoteMapping = mapping(
    keys.voteType.key -> text.verifying("Unknown type", r => WaysToVoteType.isValid(r)),
    keys.optIn.key -> optional(boolean)
      .verifying("Please answer this question", postalVote => postalVote.isDefined),
    keys.deliveryMethod.key -> optional(voteDeliveryMethodMapping)
  ) (
    (voteType, postalVoteOption, deliveryMethod) => PostalOrProxyVote(
      WaysToVoteType.parse(voteType),
      postalVoteOption,
      deliveryMethod
    )
  ) (
    postalVote => Some(
      postalVote.typeVote.name,
      postalVote.postalVoteOption,
      postalVote.deliveryMethod
    )
  ) verifying (validVoteOption)

  val postalOrProxyVoteForm = ErrorTransformForm(
    mapping(
      keys.postalOrProxyVote.key -> optional(postalOrProxyVoteMapping)
    ) (
        postalVote => InprogressOverseas (postalOrProxyVote = postalVote)
    ) (
        inprogress => Some(inprogress.postalOrProxyVote)
    ) verifying questionIsRequired
  )
}

