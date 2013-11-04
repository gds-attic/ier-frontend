package uk.gov.gds.ier.config

import com.google.inject.Singleton
import uk.gov.gds.ier.logging.Logging
import scala.collection.JavaConversions._

@Singleton
class Config extends Logging {
  private lazy val configuration = play.Play.application().configuration()

  def apiTimeout = configuration.getInt("api.timeout", 10).toInt
  def placesUrl = configuration.getString("places.url")
  def fakeIer = configuration.getBoolean("ier.fake")
  def fakePlaces = configuration.getBoolean("places.fake")
  def ierApiUrl = configuration.getString("ier.api.url")
  def ierApiToken = configuration.getString("ier.api.token")
  def stripNino = configuration.getBoolean("ier.nino.strip", false)

  def buildDate = configuration.getString("gds.BuildTime", "unknown")
  def buildNumber = configuration.getString("gds.BuildNumber", "unknown")
  def revision = configuration.getString("gds.GitCommit", "unknown")
  def branch = configuration.getString("gds.GitBranch", "unknown")

  def logConfiguration() = {
    logger.info(s"apiTimeout:$apiTimeout")
    logger.info(s"placesUrl:$placesUrl")
    logger.info(s"fakeIer:$fakeIer")
    logger.info(s"fakePlaces:$fakePlaces")
    logger.info(s"ierApiUrl:$ierApiUrl")
    logger.info(s"stripNino:$stripNino")
    logger.info(s"buildDate:$buildDate")
    logger.info(s"buildNumber:$buildNumber")
    logger.info(s"revision:$revision")
    logger.info(s"branch:$branch")
  }
}
