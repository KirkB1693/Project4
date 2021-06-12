package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.succeeded
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.isToast
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private lateinit var firebaseAuth: FirebaseAuth

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signInWithEmailAndPassword("kirkb1693@gmail.com", "dksfajkfui")
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun addReminder() {

        // Start up reminders list screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        val activity = getActivity(activityScenario)

        // Click on the add reminder fab
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Check save button is displayed and click it
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed())).perform(click())

        Thread.sleep(2000)
        // Check snackbar is displayed
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))

        Thread.sleep(2000)
        // Enter new reminder
        onView(withId(R.id.reminderTitle)).perform(replaceText("TITLE1"))
        onView(withText("TITLE1")).check(matches(isDisplayed()))

        onView(withId(R.id.reminderDescription)).perform(replaceText("DESCRIPTION1"))
        onView(withText("DESCRIPTION1")).check(matches(isDisplayed()))

        // Check save button is displayed and click it
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed())).perform(click())

        Thread.sleep(2000)
        // Check snackbar is displayed
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_select_location)))

        // Select location for reminder
        onView(withId(R.id.selectLocation)).perform(click())

        openActionBarOverflowOrOptionsMenu(appContext)
        onView(withText("Normal Map")).perform(click())

        openActionBarOverflowOrOptionsMenu(appContext)
        onView(withText("Satellite Map")).perform(click())

        openActionBarOverflowOrOptionsMenu(appContext)
        onView(withText("Terrain Map")).perform(click())

        openActionBarOverflowOrOptionsMenu(appContext)
        onView(withText("Hybrid Map")).perform(click())

        // Select current location on map
        onView(withId(R.id.map)).perform(longClick())

        // Check confirmation (save) button is displayed and click it
        onView(withId(R.id.confimation_button)).check(matches(isDisplayed())).perform(click())

        // When returning to reminder save screen check that Title and Description are still correct and that location is not blank
        onView(withId(R.id.reminderTitle)).check(matches(withText("TITLE1")))
        onView(withId(R.id.reminderDescription)).check(matches(withText("DESCRIPTION1")))
        onView(withId(R.id.selectedLocation)).check(matches(not(withText(""))))

        // Check for save button and click it
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed())).perform(click())

        runBlocking {
            val result = repository.getReminders()
            assert(result.succeeded)
            result as Result.Success
            val remindersList = result.data


            // Check that new reminder shows in recyclerview
            onView(withText("TITLE1")).check(matches(isDisplayed()))
            onView(withText("DESCRIPTION1")).check(matches(isDisplayed()))
            onView(withText(remindersList[0].location)).check(matches(isDisplayed()))
        }

        Thread.sleep(2000)
        onView(withText("Geofence for TITLE1 Added")).inRoot(isToast()).check(matches(isDisplayed()))
        Thread.sleep(3000)
        onView(withText(R.string.reminder_saved)).inRoot(isToast()).check(matches(isDisplayed()))

        // Make sure the activity is closed
        activityScenario.close()
    }

    // get activity context
    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }
}
