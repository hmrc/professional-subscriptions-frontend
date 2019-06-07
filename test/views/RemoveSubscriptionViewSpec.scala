package views

import controllers.routes
import forms.RemoveSubscriptionFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.RemoveSubscriptionView

class RemoveSubscriptionViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "removeSubscription"

  val form = new RemoveSubscriptionFormProvider()()

  "RemoveSubscription view" must {

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    val view = application.injector.instanceOf[RemoveSubscriptionView]

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode)(fakeRequest, messages)

    application.stop()

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    behave like yesNoPage(form, applyView, messageKeyPrefix, routes.RemoveSubscriptionController.onSubmit(NormalMode).url)
  }
}
