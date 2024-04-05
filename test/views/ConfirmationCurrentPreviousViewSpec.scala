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

package views

import models.{Address, EnglishRate, Rates, ScottishRate}
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.twirl.api.Html
import services.ClaimAmountService
import views.behaviours.ConfirmationViewBehaviours
import views.html.ConfirmationCurrentPreviousView

class ConfirmationCurrentPreviousViewSpec extends ConfirmationViewBehaviours {

  val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

  "PreviousCurrentYearsConfirmationView" must {

    val view = application.injector.instanceOf[ConfirmationCurrentPreviousView]

    val claimAmountService = application.injector.instanceOf[ClaimAmountService]

    val claimAmount: Int = 100
    val npsAmount: Int = 10

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
      higherRate = frontendAppConfig.scottishHigherRate,
      advancedRate = frontendAppConfig.scottishAdvancedRate,
      topRate = frontendAppConfig.scottishTopRate,
      calculatedStarterRate = claimAmountService.calculateTax(frontendAppConfig.scottishStarterRate, claimAmount),
      calculatedBasicRate = claimAmountService.calculateTax(frontendAppConfig.scottishBasicRate, claimAmount),
      calculatedIntermediateRate = claimAmountService.calculateTax(frontendAppConfig.scottishIntermediateRate, claimAmount),
      calculatedHigherRate = claimAmountService.calculateTax(frontendAppConfig.scottishHigherRate, claimAmount),
      calculatedAdvancedRate = claimAmountService.calculateTax(frontendAppConfig.scottishAdvancedRate, claimAmount),
      calculatedTopRate = claimAmountService.calculateTax(frontendAppConfig.scottishTopRate, claimAmount),
    )

    def applyView(claimAmountsAndRates: Seq[Rates] = Seq(claimAmountsRates, scottishClaimAmountsRates),
                  claimAmount: Int = claimAmount,
                  npsAmount: Int = npsAmount,
                  currentYearMinus1: Boolean = true,
                  address: Option[Address] = Some(validAddress),
                  updateEmployer: Boolean = false,
                  hasClaimIncreased: Boolean = true
                 )(fakeRequest: FakeRequest[AnyContent], messages: Messages): Html =
      view.apply(claimAmountsAndRates, claimAmount, npsAmount, currentYearMinus1, address, Some(updateEmployer), hasClaimIncreased)(fakeRequest, messages)

    val viewWithAnswers = applyView()(fakeRequest, messages)

    behave like normalPage(viewWithAnswers, "confirmation")

    "display correct static text" in {

      val doc = asDocument(viewWithAnswers)

      assertContainsMessages(doc,
        "confirmation.heading",
        "confirmation.whatHappensNext",
        "confirmation.currentTaxYear",
        "confirmation.taxCodeChanged.paragraph1",
        "confirmation.previousTaxYears",
        "confirmation.additionalConfirmationLetter"
      )
    }

    "not display currentYearMinusOneDelay when currentYearMinus1 is false" in {

      val doc = asDocument(applyView(currentYearMinus1 = false)(fakeRequest, messages))

      assertDoesntContainText(doc,
        messages("confirmation.currentYearMinusOneDelay")
      )
    }

    "display correct dynamic text for tax rates" in {

      val doc = asDocument(viewWithAnswers)

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

    "display correct text based on claim amount increase" in {

      val viewWithSpecificAnswers = applyView(npsAmount = 50, claimAmount = 1000)(fakeRequest, messages)

      val doc = asDocument(viewWithSpecificAnswers)

      assertContainsMessages(doc, messages("confirmation.personalAllowanceIncrease", 50, 1000))

      assertDoesntContainMessages(doc,
        messages("confirmation.personalAllowanceDecrease", 1000, 50),
        messages("confirmation.newPersonalAllowance", 50)
      )
    }

    "display correct text based on claim amount decrease" in {

      val viewWithSpecificAnswers = applyView(npsAmount = 1000, claimAmount = 50, hasClaimIncreased = false)(fakeRequest, messages)

      val doc = asDocument(viewWithSpecificAnswers)

      assertContainsMessages(doc, messages("confirmation.personalAllowanceDecrease", 1000, 50))

      assertDoesntContainMessages(doc,
        messages("confirmation.personalAllowanceIncrease", 1000, 50),
        messages("confirmation.newPersonalAllowance", 50)
      )
    }

    "display the correct text when there is no Nps data for CurrentYear" in {

      val viewWithSpecificAnswers = applyView(
        npsAmount = 0,
        claimAmount = 50,
        hasClaimIncreased = false
      )(fakeRequest, messages)

      val doc = asDocument(viewWithSpecificAnswers)

      assertContainsMessages(doc, messages("confirmation.newPersonalAllowance", 50))

      assertDoesntContainMessages(doc,
        messages("confirmation.personalAllowanceIncrease", 50, 1000),
        messages("confirmation.personalAllowanceDecrease", 1000, 50)
      )
    }

    "display address" in {

      val doc = asDocument(viewWithAnswers)

      assertRenderedById(doc, "citizenDetailsAddress")
    }

    "display correct content when no address" in {

      val doc = asDocument(applyView(address = None)(fakeRequest, messages))

      assertNotRenderedById(doc, "citizenDetailsAddress")
      assertRenderedById(doc, "no-address")
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
