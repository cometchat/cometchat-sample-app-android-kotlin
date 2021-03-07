package com.cometchat.pro.androiduikit


import android.view.View
import android.view.ViewGroup
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun mainActivityTest_superhero1_click() {
        val materialCardView = onView(allOf(withId(R.id.superhero1), childAtPosition(allOf(withId(R.id.grid),
               childAtPosition(withClassName(`is`("android.widget.RelativeLayout")), 4)), 0)))
        materialCardView.perform(scrollTo(), click())
//        onView((withId(R.id.directLaunch))).check(matches(withText("Launch")))

    }

    @Test
    fun mainActivityTest_superhero2_click() {
        val materialCardView = onView(allOf(withId(R.id.superhero2), childAtPosition(allOf(withId(R.id.grid),
               childAtPosition(withClassName(`is`("android.widget.RelativeLayout")),4)), 1)))
        materialCardView.perform(scrollTo(), click())
//        onView((withId(R.id.directLaunch))).check(matches(withText("Launch")))
    }

    @Test
    fun mainActivityTest_superhero3_click() {
        val materialCardView = onView(allOf(withId(R.id.superhero3), childAtPosition(allOf(withId(R.id.grid),
                childAtPosition(withClassName(`is`("android.widget.RelativeLayout")),4)), 2)))
        materialCardView.perform(scrollTo(), click())
//        onView((withId(R.id.directLaunch))).check(matches(withText("Launch")))
    }
    @Test
    fun mainActivityTest_superhero4_click() {
        val materialCardView = onView(allOf(withId(R.id.superhero4), childAtPosition(allOf(withId(R.id.grid),
                childAtPosition(withClassName(`is`("android.widget.RelativeLayout")), 4)), 3)))
        materialCardView.perform(scrollTo(), click())
//        onView((withId(R.id.directLaunch))).check(matches(withText("Launch")))
    }

    @Test
    fun mainActivityTest_login_using_uid_btn_click() {
//        onView((withId(R.id.login))).perform(click())
        val materialButton = onView(allOf(withId(R.id.login), withText("Login using uid"), childAtPosition(
                childAtPosition(withClassName(`is`("android.widget.LinearLayout")), 0), 6)))
        materialButton.perform(scrollTo(), click())
        ActivityScenario.launch(LoginActivity::class.java).use{ scenario ->
            onView(withId(R.id.etUID)).perform(typeText("user1"))
            closeSoftKeyboard()
            val checkableImageButton = onView(allOf(withId(R.id.text_input_end_icon), childAtPosition(
                    childAtPosition(withClassName(`is`("android.widget.LinearLayout")), 1), 0),
                            isDisplayed()))
            checkableImageButton.perform(click())

//            onView(withText("Create Now")).perform(click())
        }
    }

    @Test
    fun launchLoginActivityForCreateUser() {
//        val materialButton = onView(
//                allOf(withId(R.id.login), withText("Login using uid"),
//                        childAtPosition(
//                                childAtPosition(
//                                        withClassName(`is`("android.widget.LinearLayout")),
//                                        0),
//                                6)))
//        materialButton.perform(scrollTo(), click())

        onView((withId(R.id.login))).check(matches(withText("Login using uid"))).perform(click())
        ActivityScenario.launch(LoginActivity::class.java).use {
            onView(withText("Create Now")).perform(click())
            ActivityScenario.launch(CreateUserActivity::class.java).use {
                onView((withId(R.id.etUID))).perform(typeText("user2"))
                onView(withId(R.id.etName)).perform(typeText("user2"))
                closeSoftKeyboard()
                onView(withId(R.id.create_user_btn)).perform(click())
            }

        }
    }
    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
