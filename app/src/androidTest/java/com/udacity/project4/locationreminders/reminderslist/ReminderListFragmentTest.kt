package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.GrantPermissionRule
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.succeeded
import com.udacity.project4.locationreminders.data.local.FakeAndroidTestRepository
import com.udacity.project4.testModule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest: KoinTest {

//    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    val repository: FakeAndroidTestRepository by inject()

    private lateinit var firebaseAuth: FirebaseAuth

    @Before
    fun setup() = runBlockingTest{
        stopKoin()
        startKoin {
            androidContext(getApplicationContext())
            loadKoinModules(testModule)
        }
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signInWithEmailAndPassword("kirkb1693@gmail.com", "dksfajkfui")
    }

    @After
    fun cleanup() = runBlockingTest {
        repository.deleteAllReminders()
        stopKoin()
    }

    @Test
    fun setNoData_noDataTextViewShouldBeDisplayed() = runBlockingTest{
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun setTwoReminders_remindersDisplayInRecyclerView() = runBlockingTest {
        // save two reminders
        val reminder1 = ReminderDTO("Google HQ", "Check it out", "Googleplex", 37.4221, -122.0841)
        repository.saveReminder(reminder1)
        val reminder2 = ReminderDTO("Coffee", "Get a latte", "Starbucks", 37.4158, -122.0776)
        repository.saveReminder(reminder2)

        // verify reminders are available and fetch is successful
        val reminders = repository.getReminders()
        assert(reminders.succeeded)

        // start the fragment
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // verify that reminders are displayed in recyclerview
        onView(withText(reminder1.title)).check(matches(isDisplayed()))
        onView(withText(reminder2.title)).check(matches(isDisplayed()))
        onView(withText(reminder1.location)).check(matches(isDisplayed()))
        onView(withText(reminder2.location)).check(matches(isDisplayed()))

    }

    @Test
    fun clickAddReminderFAB_navigateToSaveReminderFragment() = runBlockingTest{
        // launch the fragment
        val fragment = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // get a mock nav controller and set it on the fragment
        val navController = mock(NavController::class.java)
        fragment.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // click on the addReminderFAB and then verify navigation
        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

}