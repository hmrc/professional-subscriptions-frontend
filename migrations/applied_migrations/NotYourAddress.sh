#!/bin/bash

echo ""
echo "Applying migration NotYourAddress"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /notYourAddress                       controllers.NotYourAddressController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "notYourAddress.title = notYourAddress" >> ../conf/messages.en
echo "notYourAddress.heading = notYourAddress" >> ../conf/messages.en

echo "Migration NotYourAddress completed"
