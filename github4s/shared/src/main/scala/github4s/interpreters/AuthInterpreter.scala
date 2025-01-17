/*
 * Copyright 2016-2022 47 Degrees Open Source <https://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package github4s.interpreters

import cats.Applicative
import cats.implicits._
import github4s.algebras.Auth
import github4s.Decoders._
import github4s.domain._
import github4s.GHResponse
import github4s.http.HttpClient
import java.util.UUID

class AuthInterpreter[F[_]: Applicative](implicit
    client: HttpClient[F]
) extends Auth[F] {

  override def authorizeUrl(
      client_id: String,
      redirect_uri: String,
      scopes: List[String]
  ): F[GHResponse[Authorize]] = {
    val state = UUID.randomUUID().toString
    val result: GHResponse[Authorize] =
      GHResponse(
        result = Authorize(
          client.config.authorizeUrl.format(client_id, redirect_uri, scopes.mkString(","), state),
          state
        ).asRight,
        statusCode = 200,
        headers = Map.empty
      )
    result.pure[F]
  }

  override def getAccessToken(
      client_id: String,
      client_secret: String,
      code: String,
      redirect_uri: String,
      state: String,
      headers: Map[String, String]
  ): F[GHResponse[OAuthToken]] =
    client.postOAuth[OAuthToken](
      url = client.config.accessTokenUrl,
      headers = headers,
      Map(
        "client_id"     -> client_id,
        "client_secret" -> client_secret,
        "code"          -> code,
        "redirect_uri"  -> redirect_uri,
        "state"         -> state
      )
    )
}
