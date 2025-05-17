package com.example.np_team_k.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// Context Extension (DataStore 인스턴스)
val Context.userDataStore by preferencesDataStore(name = "user_prefs")

object UserPreferences {

    private val KAKAO_ID_KEY = stringPreferencesKey("kakao_id")

    @OptIn(DelicateCoroutinesApi::class) // 메모리 누수를 방지를 위해 선언
    fun saveKakaoId(context: Context, kakaoId: String) {
        GlobalScope.launch {
            context.userDataStore.edit { prefs ->
                prefs[KAKAO_ID_KEY] = kakaoId
            }
        }
    }

    fun getKakaoId(context: Context): Flow<String> {
        return context.userDataStore.data.map { prefs ->
            prefs[KAKAO_ID_KEY] ?: ""
        }
    }
}