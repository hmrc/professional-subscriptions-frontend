#!/bin/bash

echo ""
echo "Applying migration SameAmountAllYears"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /sameAmountAllYears                        controllers.SameAmountAllYearsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /sameAmountAllYears                        controllers.SameAmountAllYearsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeSameAmountAllYears                  controllers.SameAmountAllYearsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeSameAmountAllYears                  controllers.SameAmountAllYearsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "sameAmountAllYears.title = sameAmountAllYears" >> ../conf/messages.en
echo "sameAmountAllYears.heading = sameAmountAllYears" >> ../conf/messages.en
echo "sameAmountAllYears.checkYourAnswersLabel = sameAmountAllYears" >> ../conf/messages.en
echo "sameAmountAllYears.error.required = Select yes if sameAmountAllYears" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrarySameAmountAllYearsUserAnswersEntry: Arbitrary[(SameAmountAllYearsPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[SameAmountAllYearsPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrarySameAmountAllYearsPage: Arbitrary[SameAmountAllYearsPage.type] =";\
    print "    Arbitrary(SameAmountAllYearsPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(SameAmountAllYearsPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def sameAmountAllYears: Option[AnswerRow] = userAnswers.get(SameAmountAllYearsPage) map {";\
     print "    x => AnswerRow(\"sameAmountAllYears.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, routes.SameAmountAllYearsController.onPageLoad(CheckMode).url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration SameAmountAllYears completed"
