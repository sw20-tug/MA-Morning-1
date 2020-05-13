package com.example.cheat

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4

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
            onView(withId(i))
                .check(matches(withText(msg)))
        }
    }
}
