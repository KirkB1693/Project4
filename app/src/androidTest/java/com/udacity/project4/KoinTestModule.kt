package com.udacity.project4

import com.udacity.project4.locationreminders.data.local.FakeAndroidTestRepository
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val testModule = module {
    //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
    viewModel {
        RemindersListViewModel(
            get(),
            get() as FakeAndroidTestRepository
        )
    }
    //Declare singleton definitions to be later injected using by inject()
    single {
        //This view model is declared singleton to be used across multiple fragments
        SaveReminderViewModel(
            get(),
            get() as FakeAndroidTestRepository
        )
    }
    single { FakeAndroidTestRepository() }
    single { LocalDB.createRemindersDao(androidContext()) }
}
