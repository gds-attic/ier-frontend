package uk.gov.gds.ier

import play.api.{Application, GlobalSettings}
import java.lang.Class
import uk.gov.gds.ier.client.ApiClient
import uk.gov.gds.guice.GuiceContainer
import com.google.inject.{Binder, AbstractModule}
import uk.gov.gds.ier.logging.Logging
import uk.gov.gds.ier.config.Config
import play.api.mvc.{SimpleResult, Handler, RequestHeader}
import scala.concurrent.Future

trait DynamicGlobal extends GlobalSettings with Logging {

  def bindings: Binder => Unit = { binder => }

  override def onStart(app: Application) {
    super.onStart(app)
    new Config().logConfiguration()
    GuiceContainer.initialize(List(new AbstractModule() {
      @Override
      protected def configure() {
        bindings(binder())
      }
    }))
  }

  override def onRouteRequest(request: RequestHeader): Option[Handler] = {
    logger.info(s"routing request ${request.method} ${request.path}")
    super.onRouteRequest(request)
  }

  override def onError(request: RequestHeader, ex: Throwable): Future[SimpleResult] = {
    logger.error(s"uncaught exception when routing request ${request.method} ${request.path} exception: ${ex.getStackTraceString}")
    super.onError(request, ex)
  }
}