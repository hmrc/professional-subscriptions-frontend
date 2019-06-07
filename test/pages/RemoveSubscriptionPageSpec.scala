package pages

import pages.behaviours.PageBehaviours

class RemoveSubscriptionPageSpec extends PageBehaviours {

  "RemoveSubscriptionPage" must {

    beRetrievable[Boolean](RemoveSubscriptionPage)

    beSettable[Boolean](RemoveSubscriptionPage)

    beRemovable[Boolean](RemoveSubscriptionPage)
  }
}
