package io.kzonix.redprime.client

import akka.http.scaladsl.model.HttpMethods.POST
import com.google.inject.Inject
import io.kzonix.redprime.client.RedditClient.LoginQueryParams._
import io.kzonix.redprime.client.model.OAuthResponse.responseReads
import io.kzonix.redprime.client.model.OAuthResponse
import io.kzonix.redprime.client.model.PasswordGrantTypePayload
import play.api.Configuration
import play.api.Logger
import play.api.libs.ws.WSAuthScheme
import play.api.libs.ws.WSClient
import play.api.libs.ws.WSResponse

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class RedditClient @Inject() (
    ws: WSClient,
    config: Configuration
)(implicit
    val ec: ExecutionContext
) {

  private val logger: Logger = Logger(this.getClass)

  def login: Future[Option[OAuthResponse]] = {

    val cfg                                  = config.get[PasswordGrantTypePayload](path = "reddit.client")
    val eventualResponse: Future[WSResponse] = ws
      .url(cfg.authUri)
      .withAuth(
        cfg.clientId,
        cfg.clientSecret,
        WSAuthScheme.BASIC
      )
      .withQueryStringParameters(
        GrantType -> cfg.grantType,
        UserName  -> cfg.userName,
        Password  -> cfg.password
      )
      .execute(POST.value)

    eventualResponse.map { response =>
      logger.info(response.json.toString())
      response.json.validate[OAuthResponse].asOpt
    }
  }

}

object RedditClient {

  object LoginQueryParams {
    val GrantType = "grant_type"
    val UserName  = "username"
    val Password  = "password"
  }

}
