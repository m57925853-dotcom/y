package com.focusquest.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusquest.app.data.AppDatabase
import com.focusquest.app.data.Settings
import com.focusquest.app.data.SettingsStore
import com.focusquest.app.data.TaskEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val store = SettingsStore(app)
    private val dao = AppDatabase.get(app).taskDao()

    /** null = ما زالت البيانات تُحمَّل */
    val settings: StateFlow<Settings?> =
        store.flow.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val tasks: StateFlow<List<TaskEntity>> =
        dao.observeAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun finishOnboarding(s: Settings, newTasks: List<TaskEntity>) {
        viewModelScope.launch {
            dao.insertAll(newTasks)
            store.save(s.copy(onboarded = true))
        }
    }
}
