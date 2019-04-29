#!/bin/bash

echo ""
echo "Applying migration EmployerContribution"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /employerContribution                        controllers.EmployerContributionController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /employerContribution                        controllers.EmployerContributionController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeEmployerContribution                  controllers.EmployerContributionController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeEmployerContribution                  controllers.EmployerContributionController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "employerContribution.title = employerContribution" >> ../conf/messages.en
echo "employerContribution.heading = employerContribution" >> ../conf/messages.en
echo "employerContribution.checkYourAnswersLabel = employerContribution" >> ../conf/messages.en
echo "employerContribution.error.required = Select yes if employerContribution" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryEmployerContributionUserAnswersEntry: Arbitrary[(EmployerContributionPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[EmployerContributionPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryEmployerContributionPage: Arbitrary[EmployerContributionPage.type] =";\
    print "    Arbitrary(EmployerContributionPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserDataGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(EmployerContributionPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserDataGenerator.scala > tmp && mv tmp ../test/generators/UserDataGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def employerContribution: Option[AnswerRow] = userAnswers.get(EmployerContributionPage) map {";\
     print "    x => AnswerRow(\"employerContribution.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, routes.EmployerContributionController.onPageLoad(CheckMode).url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration EmployerContribution completed"
