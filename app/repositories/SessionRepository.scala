/*
 * Copyright 2021 HM Revenue & Customs
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
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.mongo.ReactiveRepository
import reactivemongo.api.WriteConcern

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class DatedCacheMap(id: String,
                         data: Map[String, JsValue],
                         lastUpdated: DateTime = DateTime.now(DateTimeZone.UTC)
                        )

object DatedCacheMap extends MongoDateTimeFormats {

  implicit val formats = Json.format[DatedCacheMap]

  def apply(cacheMap: CacheMap): DatedCacheMap = DatedCacheMap(cacheMap.id, cacheMap.data)
}

@Singleton
class SessionRepository @Inject()(config: Configuration, mongo: ReactiveMongoComponent)
  extends ReactiveRepository[DatedCacheMap, BSONObjectID]("user-answers", mongo.mongoConnector.db, DatedCacheMap.formats) {

  private val cacheTtl = config.get[Int]("mongodb.timeToLiveInSeconds")

  private val lastUpdatedIndex = Index(
    key     = Seq("lastUpdated" -> IndexType.Ascending),
    name    = Some("user-answers-last-updated-index"),
    options = BSONDocument("expireAfterSeconds" -> cacheTtl)
  )

  val started: Future[Unit] =
    collection.indexesManager.ensure(lastUpdatedIndex).map(_ => ())

  def get(id: String): Future[Option[UserAnswers]] =
    collection.find(Json.obj("_id" -> id), None).one[UserAnswers]

  def set(userAnswers: UserAnswers): Future[Boolean] = {

    val selector = Json.obj(
      "_id" -> userAnswers.id
    )

    val modifier = Json.obj(
      "$set" -> (userAnswers copy (lastUpdated = DateTime.now))
    )

    collection.update(ordered = false).one(selector, modifier, upsert = true).map {
      lastError =>
        lastError.ok
    }
  }


  def remove(id: String): Future[Option[UserAnswers]] =
    collection.findAndRemove(
      selector = Json.obj("_id" -> id),
      sort = None,
      fields = None,
      writeConcern = WriteConcern.Default,
      maxTime = None,
      collation = None,
      arrayFilters = Seq.empty
    ).map(_.result[UserAnswers])

  def updateTimeToLive(id: String): Future[Boolean] = {
    get(id).flatMap {
      case Some(ua) =>
        set(ua)
      case _ =>
        throw new Exception(s"UserAnswers not found")
    }
  }

}
