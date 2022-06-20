/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers

import base.SpecBase
import models.{NpsDataFormats, PSubsByYear}
import models.TaxYearSelection._
import navigation.{FakeNavigator, Navigator}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import pages._
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SubmissionService
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import utils.CheckYourAnswersHelper
import viewmodels.AnswerSection
import views.html.CheckYourAnswersView

class CheckYourAnswersControllerSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience with BeforeAndAfterEach {

  private val mockSubmissionService = mock[SubmissionService]
  private val mockAuditConnector = mock[AuditConnector]

  override def beforeEach(): Unit = {
    reset(mockSubmissionService)
    reset(mockAuditConnector)
  }

  val onwardRoute = Call("GET", "/foo")

  "Check Your Answers Controller" must {

    "return OK and the correct view for a GET" in {

      val ua = emptyUserAnswers
        .set(WhichSubscriptionPage(taxYear, index), psubWithoutEmployerContribution.nameOfProfessionalBody).success.value
        .set(SubscriptionAmountPage(taxYear, index), psubWithoutEmployerContribution.amount).success.value
        .set(EmployerContributionPage(taxYear, index), psubWithoutEmployerContribution.employerContributed).success.value
        .set(WhichSubscriptionPage(getTaxYear(CurrentYearMinus1).toString, index), psubWithoutEmployerContribution.nameOfProfessionalBody).success.value
        .set(SubscriptionAmountPage(getTaxYear(CurrentYearMinus1).toString, index), psubWithoutEmployerContribution.amount).success.value
        .set(EmployerContributionPage(getTaxYear(CurrentYearMinus1).toString, index), psubWithoutEmployerContribution.employerContributed).success.value
        .set(YourEmployerPage, true).success.value
        .set(YourAddressPage, true).success.value

      val CYAHelper = new CheckYourAnswersHelper(ua)

      val taxYearSelection = Seq(AnswerSection(
        headingKey = Some("checkYourAnswers.taxYearsClaiming"),
        headingClasses = Some("visually-hidden"),
        subheadingKey = None,
        rows = Seq(
          CYAHelper.taxYearSelection,
          CYAHelper.amountsAlreadyInCode,
          CYAHelper.reEnterAmounts
        ).flatten
      ))

      val subscriptions: Seq[AnswerSection] = {
        NpsDataFormats.sort(ua.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats).get).zipWithIndex.flatMap {
          case (psubsByYear, yearIndex) =>
            psubsByYear._2.zipWithIndex.map {
              case (psub, subIndex) =>
                val taxYear = psubsByYear._1
                AnswerSection(
                  headingKey = if (yearIndex == 0 && subIndex == 0) Some("checkYourAnswers.yourSubscriptions") else None,
                  headingClasses = None,
                  subheadingKey = if (subIndex == 0) Some(s"taxYearSelection.${getTaxYearPeriod(taxYear)}") else None,
                  rows = Seq(
                    CYAHelper.whichSubscription(taxYear.toString, subIndex, psub),
                    CYAHelper.subscriptionAmount(taxYear.toString, subIndex, psub),
                    CYAHelper.employerContribution(taxYear.toString, subIndex, psub),
                    CYAHelper.expensesEmployerPaid(taxYear.toString, subIndex, psub)
                  ).flatten,
                  messageArgs = Seq(taxYear.toString, (taxYear + 1).toString): _*
                )
            }
        }
      }

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[CheckYourAnswersView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(taxYearSelection ++ subscriptions)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to next page for a GET to acceptAndClaim" in {

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
        .build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.acceptAndClaim.url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }
  }

}
