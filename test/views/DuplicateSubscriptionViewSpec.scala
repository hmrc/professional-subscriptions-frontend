package views

import views.behaviours.ViewBehaviours
import views.html.DuplicateSubscriptionView

class DuplicateSubscriptionViewSpec extends ViewBehaviours {

  "DuplicateSubscription view" must {

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    val view = application.injector.instanceOf[DuplicateSubscriptionView]

    val applyView = view.apply()(fakeRequest, messages)

    behave like normalPage(applyView, "duplicateSubscription")

    behave like pageWithBackLink(applyView)
  }
}
