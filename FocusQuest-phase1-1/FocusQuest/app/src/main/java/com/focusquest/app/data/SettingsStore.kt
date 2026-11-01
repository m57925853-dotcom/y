package com.focusquest.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.ds by preferencesDataStore("settings")

data class Settings(
    val name: String = "",
    val startMin: Int = 15 * 60,
    val endMin: Int = 22 * 60,
    val breakMin: Int = 120,
    val onboarded: Boolean = false
) {
    val totalMinutes: Int
        get() = ((endMin - startMin + 1440) % 1440).let { if (it == 0) 1440 else it }
    val netMinutes: Int get() = (totalMinutes - breakMin).coerceAtLeast(0)
}

class SettingsStore(private val context: Context) {
    private val kName = stringPreferencesKey("name")
    private val kStart = intPreferencesKey("start")
    private val kEnd = intPreferencesKey("end")
    private val kBreak = intPreferencesKey("break")
    private val kDone = booleanPreferencesKey("onboarded")

    val flow: Flow<Settings> = context.ds.data.map { p ->
        Settings(
            name = p[kName] ?: "",
            startMin = p[kStart] ?: (15 * 60),
            endMin = p[kEnd] ?: (22 * 60),
            breakMin = p[kBreak] ?: 120,
            onboarded = p[kDone] ?: false
        )
    }

    suspend fun save(s: Settings) {
        context.ds.edit { p ->
            p[kName] = s.name
            p[kStart] = s.startMin
            p[kEnd] = s.endMin
            p[kBreak] = s.breakMin
            p[kDone] = s.onboarded
        }
    }
}
