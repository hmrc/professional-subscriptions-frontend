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

package views

import models.{Address, EnglishRate, Rates, ScottishRate, TaxYearSelection}
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.twirl.api.Html
import services.ClaimAmountService
import views.behaviours.ViewBehaviours
import views.html.ConfirmationCurrentView

class ConfirmationCurrentViewSpec extends ViewBehaviours {

  val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

  "ConfirmationCurrentView" must {

    val view = application.injector.instanceOf[ConfirmationCurrentView]

    val claimAmountService = application.injector.instanceOf[ClaimAmountService]

    val claimAmount: Int = 100

    val claimAmountsRates = EnglishRate(
      basicRate = frontendAppConfig.englishBasicRate,
      higherRate = frontendAppConfig.englishHigherRate,
      calculatedBasicRate = claimAmountService.calculateTax(frontendAppConfig.englishBasicRate, claimAmount),
      calculatedHigherRate = claimAmountService.calculateTax(frontendAppConfig.englishHigherRate, claimAmount)
    )

    val scottishClaimAmountsRates = ScottishRate(
      starterRate = frontendAppConfig.scottishStarterRate,
      basicRate = frontendAppConfig.scottishBasicRate,
      intermediateRate = frontendAppConfig.scottishIntermediateRate,
      calculatedStarterRate = claimAmountService.calculateTax(frontendAppConfig.scottishStarterRate, claimAmount),
      calculatedBasicRate = claimAmountService.calculateTax(frontendAppConfig.scottishBasicRate, claimAmount),
      calculatedIntermediateRate = claimAmountService.calculateTax(frontendAppConfig.scottishIntermediateRate, claimAmount)
    )

    def applyView(claimAmountsAndRates: Seq[Rates] = Seq(claimAmountsRates, scottishClaimAmountsRates),
                  claimAmount: Int = claimAmount,
                  address: Address = validAddress,
                  updateEmployer: Boolean = false,
                  updateAddressUrl: String = "addressURL",
                  updateEmployerUrl: String = "employerURL"
                 )(fakeRequest: FakeRequest[AnyContent], messages: Messages): Html =
      view.apply(claimAmountsAndRates, claimAmount, Some(address), Some(updateEmployer), "addressURL", "employerURL")(fakeRequest, messages)

    val viewWithAnswers = applyView()(fakeRequest, messages)

    behave like normalPage(viewWithAnswers, "confirmation")

    "display correct static text" in {

      val doc = asDocument(viewWithAnswers)

      assertContainsMessages(doc,
        "confirmation.heading",
        "confirmation.actualAmount",
        "confirmation.whatHappensNext",
        "confirmation.taxCodeChanged.paragraph1",
        "confirmation.taxCodeChanged.paragraph2",
        "confirmation.continueToClaim.paragraph1",
        "confirmation.continueToClaim.paragraph2"
      )
    }

    "display correct dynamic text for tax rates" in {

      val doc = asDocument(viewWithAnswers)

      assertContainsText(doc, messages("confirmation.personalAllowanceIncrease", claimAmount))
      assertContainsText(doc, messages(
        "confirmation.basicRate",
        claimAmountsRates.calculatedBasicRate,
        claimAmount,
        claimAmountsRates.basicRate
      ))
      assertContainsText(doc, messages(
        "confirmation.higherRate",
        claimAmountsRates.calculatedHigherRate,
        claimAmount,
        claimAmountsRates.higherRate
      ))
      assertContainsText(doc, messages(
        "confirmation.intermediateRate",
        scottishClaimAmountsRates.calculatedIntermediateRate,
        claimAmount,
        scottishClaimAmountsRates.intermediateRate
      ))
      assertContainsText(doc, messages("confirmation.englandHeading"))
      assertContainsText(doc, messages("confirmation.scotlandHeading"))
    }

    "YourAddress" must {

      "display update address button and content when 'false'" in {

        val doc = asDocument(applyView()(fakeRequest, messages))

        assertContainsMessages(doc, "confirmation.updateAddressInfo", "confirmation.addressChange")
        doc.getElementById("updateAddressInfoBtn").text mustBe messages("confirmation.updateAddressInfoNow")
      }

      "not display update address button and content when 'true'" in {

        val doc = asDocument(applyView(address = validAddress)(fakeRequest, messages))

        assertNotRenderedById(doc, "updateAddressInfoBtn")
      }
    }

    "YourEmployer" must {

      "display update employer button and content when 'false'" in {

        val viewWithSpecificAnswers = applyView()(fakeRequest, messages)

        val doc = asDocument(viewWithSpecificAnswers)

        assertContainsMessages(doc, "confirmation.updateEmployerInfo", "confirmation.employerChange")
        doc.getElementById("updateEmployerInfoBtn").text mustBe messages("confirmation.updateEmployerInfoNow")
      }

      "not display update employer button and content when 'true'" in {

        val viewWithSpecificAnswers = applyView(updateEmployer = true)(fakeRequest, messages)

        val doc = asDocument(viewWithSpecificAnswers)

        assertNotRenderedById(doc, "updateEmployerInfoNow")
      }
    }

  }

  application.stop()
}
