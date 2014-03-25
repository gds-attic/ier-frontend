package uk.gov.gds.ier.transaction.crown.waysToVote

import com.google.inject.Inject
import uk.gov.gds.ier.serialiser.JsonSerialiser
import uk.gov.gds.ier.config.Config
import uk.gov.gds.ier.security.EncryptionService
import uk.gov.gds.ier.step.CrownStep
import controllers.step.crown.routes.{WaysToVoteController, OpenRegisterController}
import controllers.step.crown.{ProxyVoteController, ContactController, PostalVoteController}
import uk.gov.gds.ier.step.Routes
import uk.gov.gds.ier.model.{WaysToVoteType, InprogressCrown}
import uk.gov.gds.ier.validation.InProgressForm
import play.api.mvc.Call
import play.api.templates.Html
import play.api.mvc.SimpleResult
import uk.gov.gds.ier.step.NextStep
import uk.gov.gds.ier.model.{WaysToVote,PostalOrProxyVote}


class WaysToVoteStep @Inject ()(
    val serialiser: JsonSerialiser,
    val config: Config,
    val encryptionService : EncryptionService)
  extends CrownStep
  with WaysToVoteForms
  with WaysToVoteMustache {

  val validation = waysToVoteForm

  val routes: Routes = Routes(
    get = WaysToVoteController.get,
    post = WaysToVoteController.post,
    editGet = WaysToVoteController.editGet,
    editPost = WaysToVoteController.editPost
  )
  val previousRoute = Some(OpenRegisterController.get)

  def nextStep(currentState: InprogressCrown) = {
    currentState.waysToVote.map(_.waysToVoteType) match {
      case Some(WaysToVoteType.InPerson) => ContactController.contactStep
      case Some(WaysToVoteType.ByPost) => PostalVoteController.postalVoteStep
      case Some(WaysToVoteType.ByProxy) => ProxyVoteController.proxyVoteStep
      case _ => throw new IllegalArgumentException("unknown next step")
    }
  }
  
  override def postSuccess(currentState: InprogressCrown):InprogressCrown = {
    if (currentState.waysToVote == Some(WaysToVote(WaysToVoteType.InPerson))) 
        currentState.copy(postalOrProxyVote = None)
    else currentState.copy(postalOrProxyVote = currentState.postalOrProxyVote.map(_.copy(forceRedirectToPostal = true)))
  }
  
  def template(form:InProgressForm[InprogressCrown], call:Call, backUrl: Option[Call]): Html = {
    waysToVoteMustache(form.form, call, backUrl.map(_.url))
  }
}