/*
 * Copyright 2019 HM Revenue & Customs
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

package base

import com.github.tototoshi.play2.scalate.Scalate
import config.FrontendAppConfig
import controllers.actions._
import models.TaxYearSelection.CurrentYear
import models.{Address, Employment, UserAnswers}
import org.scalatest.TryValues
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice._
import pages.{AddAnotherSubscriptionPage, EmployerContributionPage, SubscriptionAmountPage, TaxYearSelectionPage, WhichSubscriptionPage, YourAddressPage, YourEmployerPage}
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{Injector, bind}
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.partials.FormPartialRetriever
import utils.{MockPsFormPartialRetriever, MockScalate}

trait SpecBase extends PlaySpec with GuiceOneAppPerSuite with TryValues {

  val userAnswersId = "id"

  lazy val fakeNino = "AB123456A"
  lazy val taxYear = "2016"
  lazy val taxYearInt = 2016

  lazy val validAddress = Address(
    Some("6 Howsell Road"),
    Some("Llanddew"),
    Some("Line 3"),
    Some("Line 4"),
    Some("Line 5"),
    Some("DN16 3FB"),
    Some("GREAT BRITAIN")
  )

  lazy val validAddressJson: JsValue = Json.parse(
    s"""
       |{
       |  "address":{
       |    "line1":"6 Howsell Road",
       |    "line2":"Llanddew",
       |    "line3":"Line 3",
       |    "line4":"Line 4",
       |    "line5":"Line 5",
       |    "postcode":"DN16 3FB",
       |    "country":"GREAT BRITAIN"
       |  }
       |}
     """.stripMargin
  )

  lazy val emptyAddressJson: JsValue = Json.parse(
    s"""
       |{
       |  "address":{
       |    "line1":"",
       |    "line2":"",
       |    "line3":"",
       |    "line4":"",
       |    "line5":"",
       |    "postcode":"",
       |    "country":""
       |  }
       |}
     """.stripMargin
  )

  val validEmploymentJson: JsValue = Json.parse(
    """{
      | "data" : {
      |   "employments" : [{
      |    "name": "HMRC Longbenton",
      |    "startDate": "2018-06-27"
      |    }]
      |  }
      |}""".stripMargin
  )

  lazy val etag: Int = 123

  lazy val validEtagJson: JsValue = Json.parse(
    s"""
       |{
       |   "etag":"$etag"
       |}
    """.stripMargin)

  def someUserAnswers: UserAnswers = emptyUserAnswers
    .set(TaxYearSelectionPage, Seq(CurrentYear)).success.value
    .set(WhichSubscriptionPage, "Arable Research Institute Association").success.value
    .set(SubscriptionAmountPage, 100000).success.value
    .set(EmployerContributionPage, false).success.value
    .set(AddAnotherSubscriptionPage, false).success.value
    .set(YourEmployerPage, true).success.value
    .set(YourAddressPage, true).success.value


  lazy val taiEmployment: Seq[Employment] = Seq(Employment("HMRC Longbenton"))

  def emptyUserAnswers = UserAnswers(userAnswersId, Json.obj())

  implicit val hc: HeaderCarrier = HeaderCarrier()

  def injector: Injector = app.injector

  def frontendAppConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  def fakeRequest = FakeRequest("", "")

  implicit def messages: Messages = messagesApi.preferred(fakeRequest)

  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
        bind[Scalate].to[MockScalate],
        bind[FormPartialRetriever].to[MockPsFormPartialRetriever]
      )
}
