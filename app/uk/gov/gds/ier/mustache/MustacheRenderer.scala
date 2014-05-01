package uk.gov.gds.ier.mustache

import uk.gov.gds.ier.validation.ErrorTransformForm
import play.api.templates.Html
import play.api.mvc.Call
import uk.gov.gds.ier.langs.Language

class MustacheRenderer[T](
    template: MustacheTemplate[T],
    form: ErrorTransformForm[T],
    postUrl: Call,
    application: T
) extends StepMustache {

  type Request[A] = play.api.mvc.Request[A]

  def html()(implicit request:Request[Any]):Html = {
    val lang = Language.getLang(request)
    val model = template.data(lang, form, postUrl, application)
    val content = Mustache.render(template.mustachePath, model)
    MainStepTemplate(content, model.question.title)
  }
}

