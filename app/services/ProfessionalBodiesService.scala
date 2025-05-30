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

package services

import com.google.inject.Inject
import config.FrontendAppConfig
import models.ProfessionalBody
import models.ProfessionalBody._
import play.api.Environment
import play.api.libs.json.Json

import scala.io.Source

class ProfessionalBodiesService @Inject() (
    environment: Environment,
    frontendAppConfig: FrontendAppConfig
) {

  private val resourceLocation: String = "professional-bodies.json"

  val professionalBodies: List[ProfessionalBody] = {

    val jsonString = environment
      .resourceAsStream(resourceLocation)
      .fold(throw new Exception("professional-bodies.json"))(Source.fromInputStream)
      .mkString

    Json.parse(jsonString).as[List[ProfessionalBody]]
  }

  def validateYearInRange(psubNames: Seq[String], year: Int): Boolean =
    psubNames.forall {
      validateYearInRange(_, year)
    }

  def validateYearInRange(psubName: String, year: Int): Boolean =
    professionalBodies.find(_.name == psubName) match {
      case Some(psub @ ProfessionalBody(_, _, _)) => psub.validateStartYear(year)
      case _ => throw new Exception(s"Professional Subscription not found for $psubName")
    }

}
