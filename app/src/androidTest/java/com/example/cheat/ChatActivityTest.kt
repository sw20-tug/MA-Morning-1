package com.example.cheat

import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class ChatActivityTest {
    private lateinit var stringsToBeTyped: Array<String>

    @get:Rule
    var activityRule: ActivityTestRule<ChatActivity>
            = ActivityTestRule(ChatActivity::class.java)

    @Before
    fun initValidString() {
        // Specify a valid string.
        stringsToBeTyped = arrayOf("first message", "other mesh", "123", "lkjasdhf2 1")
    }

    @Test
    fun sendTexts() {
        for (msg in stringsToBeTyped){
            // Type text and then press the button.
            onView(withId(R.id.text_entry))
                .perform(typeText(msg))
            onView(withId(R.id.button_send)).perform(click())
        }
        for ((i, msg) in stringsToBeTyped.withIndex()){
            // Check that the text was changed.
            onView(nthChildOf(withId(R.id.history_layout), i * 2) )
                .check(matches(withText(msg)))
        }
    }
}

fun nthChildOf(parentMatcher: Matcher<View?>, childPosition: Int): Matcher<View?>? {
    return object : TypeSafeMatcher<View?>() {
        override fun describeTo(description: Description) {
            description.appendText("with $childPosition child view of type parentMatcher")
        }

        override fun matchesSafely(view: View?): Boolean {
            if (view?.getParent() !is ViewGroup) {
                return parentMatcher.matches(view?.getParent())
            }
            val group = view.getParent() as ViewGroup
            return parentMatcher.matches(view.getParent()) && group.getChildAt(childPosition) == view
        }
    }
}