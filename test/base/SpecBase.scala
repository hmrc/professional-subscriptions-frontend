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
import models.TaxYearSelection._
import models.{Address, Employment, EmploymentExpense, PSub, TaxYearSelection, UserAnswers}
import navigation.Navigator
import org.scalatest.TryValues
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice._
import pages.{EmployerContributionPage, ExpensesEmployerPaidPage, NpsData, SubscriptionAmountAndAnyDeductions, SubscriptionAmountPage, TaxYearSelectionPage, WhichSubscriptionPage, YourAddressPage, YourEmployerPage}
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
  val navigator = new Navigator

  lazy val fakeNino = "AB123456A"
  lazy val taxYear: String = TaxYearSelection.getTaxYear(CurrentYear).toString
  lazy val index = 0
  lazy val taxYearInt: Int = TaxYearSelection.getTaxYear(CurrentYear)
  lazy val psubWithEmployerContribution: PSub = PSub(name = "psub", amount = 10, employerContributed = true, employerContributionAmount = Some(5))
  lazy val psubWithoutEmployerContribution: PSub = PSub(name = "psub", amount = 10, employerContributed = false, employerContributionAmount = None)

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

  val validTaxCodeRecordJson: JsValue = Json.parse(
    """
      |{
      |  "data" : [ {
      |    "componentType" : "EmploymentIncome",
      |    "employmentId" : 1,
      |    "amount" : 1100,
      |    "description" : "EmploymentIncome",
      |    "taxCode" : "1150L",
      |    "name" : "Employer1",
      |    "basisOperation" : "Week1Month1BasisOperation",
      |    "status" : "Live",
      |    "inYearAdjustment" : 0
      |  }, {
      |    "componentType" : "EmploymentIncome",
      |    "employmentId" : 2,
      |    "amount" : 0,
      |    "description" : "EmploymentIncome",
      |    "taxCode" : "1100L",
      |    "name" : "Employer2",
      |    "basisOperation" : "OtherBasisOperation",
      |    "status" : "PotentiallyCeased",
      |    "inYearAdjustment" : 321.12
      |  }, {
      |    "componentType" : "EmploymentIncome",
      |    "employmentId" : 2,
      |    "amount" : 0,
      |    "description" : "EmploymentIncome",
      |    "taxCode" : "1100L",
      |    "name" : "Employer2",
      |    "basisOperation" : "OtherBasisOperation",
      |    "status" : "Ceased",
      |    "inYearAdjustment" : 321.12
      |  } ],
      |  "links" : [ ]
      |}
    """.stripMargin
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
    .set(WhichSubscriptionPage(taxYear, index), "Arable Research Institute Association").success.value
    .set(SubscriptionAmountPage(taxYear, index), 100000).success.value
    .set(ExpensesEmployerPaidPage(taxYear, index), 200).success.value
    .set(EmployerContributionPage(taxYear, index), true).success.value
    .set(SubscriptionAmountAndAnyDeductions, 100000).success.value
    .set(NpsData, Map(taxYear -> Seq(EmploymentExpense(300)))).success.value
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
