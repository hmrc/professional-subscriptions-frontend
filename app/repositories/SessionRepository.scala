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
import models.{MongoDateTimeFormats, UserAnswers}
import org.joda.time.{DateTime, DateTimeZone}
import play.api.Configuration
import play.api.libs.json._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Indexes._
import org.mongodb.scala.model.Updates.{set => mongoSet}
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model.{IndexModel, IndexOptions, UpdateOptions}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.formats.MongoJodaFormats
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import uk.gov.hmrc.mongo.play.json.formats.MongoJodaFormats.Implicits._

import scala.concurrent.duration.SECONDS
import scala.concurrent.{ExecutionContext, Future}

case class DatedCacheMap(id: String,
                         data: Map[String, JsValue],
                         lastUpdated: DateTime = DateTime.now(DateTimeZone.UTC)
                        )

object DatedCacheMap extends MongoDateTimeFormats {
  implicit lazy val reads: Reads[DatedCacheMap] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "_id").read[String] and
        (__ \ "data").read[Map[String, JsValue]] and
        (__ \ "lastUpdated").read(MongoJodaFormats.dateTimeReads)
      ) (DatedCacheMap.apply _)
  }
  implicit lazy val writes: OWrites[DatedCacheMap] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "_id").write[String] and
        (__ \ "data").write[Map[String, JsValue]] and
        (__ \ "lastUpdated").write(MongoJodaFormats.dateTimeWrites)
      ) (unlift(DatedCacheMap.unapply))
  }
  val formats: OFormat[DatedCacheMap] = OFormat(reads, writes)
}

@Singleton
class SessionRepository @Inject()(config: Configuration, mongo: MongoComponent)(implicit executionContext: ExecutionContext)
  extends PlayMongoRepository[DatedCacheMap](
    collectionName = "user-answers",
    mongoComponent = mongo,
    domainFormat = DatedCacheMap.formats,
    indexes = Seq(
      IndexModel(ascending("lastUpdated"), IndexOptions()
          .name("user-answers-last-updated-index")
          .expireAfter(config.get[Int]("mongodb.timeToLiveInSeconds"), SECONDS))
    ),
    extraCodecs = Seq(Codecs.playFormatCodec(UserAnswers.formats))
  ) {

  def get(id: String): Future[Option[UserAnswers]] =
    collection.find[UserAnswers](and(equal("_id", id))).headOption()

  def set(userAnswers: UserAnswers): Future[Boolean] = {
    val dcm = DatedCacheMap(userAnswers.id, userAnswers.data.fields.toMap)
    collection.updateOne(
      filter = equal("_id", dcm.id),
      update = combine(
        mongoSet("data", Codecs.toBson(dcm.data)),
        mongoSet("lastUpdated", Codecs.toBson(dcm.lastUpdated))
      ),
      UpdateOptions().upsert(true)
    ).toFuture().map(_.wasAcknowledged())
  }


  def remove(id: String): Future[Option[UserAnswers]] = {
    collection.findOneAndDelete(equal("_id", id))
      .headOption()
      .map(maybeDatedCacheMap =>
        maybeDatedCacheMap.map(dcm =>
          UserAnswers(dcm.id, JsObject(dcm.data), dcm.lastUpdated)
        )
      )
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
