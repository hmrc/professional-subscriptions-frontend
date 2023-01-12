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

import base.SpecBase
import play.api.Configuration
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

class SessionRepositorySpec extends SpecBase
  with FutureAwaits
  with DefaultAwaitTimeout
  with DefaultPlayMongoRepositorySupport[DatedCacheMap] {

  lazy val repository: SessionRepository = new SessionRepository(
    config = app.injector.instanceOf[Configuration],
    mongo = mongoComponent
  )
  val anotherUserId = "another-user-id"
  val anotherUsersAnswers = userAnswersCurrent.copy(id = anotherUserId)
  "SessionRepository" must {
    "return None for a missing user session" when {
      "the repository is empty" in {
        await(repository.get(anotherUserId)) mustBe None
      }
      "the repository is populated" in {
        await(repository.set(userAnswersCurrent)) mustBe true

        await(repository.get(anotherUserId)) mustBe None
      }
    }
    "return the user's answers" when {
      "the repository is populated" in {
        await(repository.set(userAnswersCurrent)) mustBe true
        await(repository.set(anotherUsersAnswers)) mustBe true

        await(repository.get(anotherUserId)).nonEmpty mustBe true
      }
    }
    "remove the user's answers" when {
      "the repository is empty" in {
        await(repository.remove(anotherUserId)).isEmpty mustBe true
      }
      "they're in the repository" in {
        await(repository.set(anotherUsersAnswers)) mustBe true

        await(repository.get(anotherUserId)).nonEmpty mustBe true

        await(repository.remove(anotherUserId)).nonEmpty mustBe true

        await(repository.get(anotherUserId)) mustBe None
      }
    }
    "update the UserAnswers' lastUpdated field" when {
      "the repository is empty" in {
        intercept[Exception]{
          await(repository.updateTimeToLive(anotherUserId))
        }
      }
        "they're in the repository" in {
        await(repository.set(anotherUsersAnswers)) mustBe true

        val before = await(repository.get(anotherUserId)).get.lastUpdated

        await(repository.updateTimeToLive(anotherUserId)) mustBe true

        val after = await(repository.get(anotherUserId)).get.lastUpdated

        before.isBefore(after) mustBe true
      }
    }
  }
}
