/*
 * Copyright 2023 HM Revenue & Customs
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

package repositories


import javax.inject.{Inject, Singleton}
import models.UserAnswers
import play.api.Configuration
import play.api.libs.json._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Indexes._
import org.mongodb.scala.model.{IndexModel, IndexOptions, ReplaceOptions, UpdateOptions}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import java.time.Instant
import scala.concurrent.duration.SECONDS
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionRepository @Inject()(config: Configuration, mongo: MongoComponent)(implicit executionContext: ExecutionContext)
  extends PlayMongoRepository[UserAnswers](
    collectionName = "user-answers",
    mongoComponent = mongo,
    domainFormat = UserAnswers.formats,
    indexes = Seq(
      IndexModel(ascending("lastUpdated"), IndexOptions()
          .name("user-answers-last-updated-index")
          .expireAfter(config.get[Int]("mongodb.timeToLiveInSeconds"), SECONDS))
    )
  ) {

  def get(id: String): Future[Option[UserAnswers]] =
    collection.find[UserAnswers](and(equal("_id", id))).headOption()

  def set(userAnswers: UserAnswers): Future[Boolean] = {
    collection.replaceOne(
      filter = equal("_id", userAnswers.id),
      replacement = userAnswers.copy(lastUpdated = Instant.now()),
      options = ReplaceOptions().upsert(true)
    ).toFuture().map(_.wasAcknowledged())
  }

  def remove(id: String): Future[Option[UserAnswers]] = {
    collection.findOneAndDelete(equal("_id", id))
      .headOption()
  }

  def updateTimeToLive(id: String): Future[Boolean] = {
    get(id).flatMap {
      case Some(ua) =>
        set(ua)
      case _ =>
        throw new Exception(s"UserAnswers not found")
    }
  }

}
