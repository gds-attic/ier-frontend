package uk.gov.gds.ier.client

import uk.gov.gds.ier.model.{Fail, ApiResponse, Success}
import play.api.libs.ws.WS
import scala.concurrent.duration._
import scala.concurrent.Await
import play.api.http._
import uk.gov.gds.ier.config.Config
import com.google.inject.Inject
import uk.gov.gds.ier.guice.WithConfig
import uk.gov.gds.ier.logging.Logging

trait ApiClient extends Logging {
  self:WithConfig =>

  def get(url: String) : ApiResponse = timeThis(s"apiClient.get url:$url") {
    try {
        val result = Await.result(
          WS.url(url).get(),
          config.apiTimeout seconds
        )
        result.status match {
          case Status.OK => {
            logger.info(s"apiClient.get url:$url result:200")
            Success(result.body)
          }
          case status => {
            logger.info(s"apiClient.get url:$url result:$status reason:${result.body}")
            Fail(result.body)
          }
        }
    } catch {
      case e:Exception => {
        logger.error(s"apiClient.get url:$url exception:${e.getStackTraceString}")
        Fail(e.getMessage)
      }
    }
  }

  def post(url:String, content:String, headers: (String, String)*) : ApiResponse = timeThis(s"apiClient.post url:$url") {
    try {
      val result = Await.result(
        WS.url(url)
          .withHeaders("Content-Type" -> MimeTypes.JSON)
          .withHeaders(headers:_*)
          .post(content),
        config.apiTimeout seconds
      )
      result.status match {
        case Status.OK => {
          logger.info(s"apiClient.post url:$url result:200")
          Success(result.body)
        }
        case Status.NO_CONTENT => {
          logger.info(s"apiClient.post url:$url result:204")
          Success("")
        }
        case status => {
          logger.info(s"apiClient.post url:$url result:$status")
          Fail(result.body)
        }
      }
    } catch {
      case e:Exception => {
        logger.error(s"apiClient.post url:$url exception:${e.getStackTraceString}")
        Fail(e.getMessage)
      }
    }
  }
}
