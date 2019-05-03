#!/bin/bash

echo ""
echo "Applying migration AddAnotherSubscription"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /addAnotherSubscription                        controllers.AddAnotherSubscriptionController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /addAnotherSubscription                        controllers.AddAnotherSubscriptionController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAddAnotherSubscription                  controllers.AddAnotherSubscriptionController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAddAnotherSubscription                  controllers.AddAnotherSubscriptionController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "addAnotherSubscription.title = addAnotherSubscription" >> ../conf/messages.en
echo "addAnotherSubscription.heading = addAnotherSubscription" >> ../conf/messages.en
echo "addAnotherSubscription.checkYourAnswersLabel = addAnotherSubscription" >> ../conf/messages.en
echo "addAnotherSubscription.error.required = Select yes if addAnotherSubscription" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAddAnotherSubscriptionUserAnswersEntry: Arbitrary[(AddAnotherSubscriptionPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AddAnotherSubscriptionPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAddAnotherSubscriptionPage: Arbitrary[AddAnotherSubscriptionPage.type] =";\
    print "    Arbitrary(AddAnotherSubscriptionPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserDataGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AddAnotherSubscriptionPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserDataGenerator.scala > tmp && mv tmp ../test/generators/UserDataGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def addAnotherSubscription: Option[AnswerRow] = userAnswers.get(AddAnotherSubscriptionPage) map {";\
     print "    x => AnswerRow(\"addAnotherSubscription.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, routes.AddAnotherSubscriptionController.onPageLoad(CheckMode).url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration AddAnotherSubscription completed"
