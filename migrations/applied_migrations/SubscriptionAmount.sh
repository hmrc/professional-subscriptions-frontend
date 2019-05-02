#!/bin/bash

echo ""
echo "Applying migration SubscriptionAmount"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /subscriptionAmount                  controllers.SubscriptionAmountController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /subscriptionAmount                  controllers.SubscriptionAmountController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeSubscriptionAmount                        controllers.SubscriptionAmountController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeSubscriptionAmount                        controllers.SubscriptionAmountController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "subscriptionAmount.title = SubscriptionAmount" >> ../conf/messages.en
echo "subscriptionAmount.heading = SubscriptionAmount" >> ../conf/messages.en
echo "subscriptionAmount.checkYourAnswersLabel = SubscriptionAmount" >> ../conf/messages.en
echo "subscriptionAmount.error.nonNumeric = Enter your subscriptionAmount using numbers" >> ../conf/messages.en
echo "subscriptionAmount.error.required = Enter your subscriptionAmount" >> ../conf/messages.en
echo "subscriptionAmount.error.wholeNumber = Enter your subscriptionAmount using whole numbers" >> ../conf/messages.en
echo "subscriptionAmount.error.outOfRange = SubscriptionAmount must be between {0} and {1}" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrarySubscriptionAmountUserAnswersEntry: Arbitrary[(SubscriptionAmountPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[SubscriptionAmountPage.type]";\
    print "        value <- arbitrary[Int].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrarySubscriptionAmountPage: Arbitrary[SubscriptionAmountPage.type] =";\
    print "    Arbitrary(SubscriptionAmountPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserDataGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(SubscriptionAmountPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserDataGenerator.scala > tmp && mv tmp ../test/generators/UserDataGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def subscriptionAmount: Option[AnswerRow] = userAnswers.get(SubscriptionAmountPage) map {";\
     print "    x => AnswerRow(\"subscriptionAmount.checkYourAnswersLabel\", s\"$x\", false, routes.SubscriptionAmountController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration SubscriptionAmount completed"
