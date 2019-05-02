#!/bin/bash

echo ""
echo "Applying migration UpdateYourEmployerInformation"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /updateYourEmployerInformation                       controllers.UpdateYourEmployerInformationController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "updateYourEmployerInformation.title = updateYourEmployerInformation" >> ../conf/messages.en
echo "updateYourEmployerInformation.heading = updateYourEmployerInformation" >> ../conf/messages.en

echo "Migration UpdateYourEmployerInformation completed"
