package com.example.np_team_k.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
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
    private val NICKNAME_KEY = stringPreferencesKey("nickname")
    private val IS_MEMBER_KEY = booleanPreferencesKey("is_member")

    @OptIn(DelicateCoroutinesApi::class)
    fun saveUser(context: Context, kakaoId: String, nickname: String, isMember: Boolean) {
        GlobalScope.launch {
            context.userDataStore.edit { prefs ->
                prefs[KAKAO_ID_KEY] = kakaoId
                prefs[NICKNAME_KEY] = nickname
                prefs[IS_MEMBER_KEY] = isMember
            }
        }
    }

    fun getKakaoId(context: Context): Flow<String> {
        return context.userDataStore.data.map { prefs ->
            prefs[KAKAO_ID_KEY] ?: ""
        }
    }

    fun getNickname(context: Context): Flow<String> {
        return context.userDataStore.data.map { prefs ->
            prefs[NICKNAME_KEY] ?: ""
        }
    }

    fun getIsMember(context: Context): Flow<Boolean> {
        return context.userDataStore.data.map { prefs ->
            prefs[IS_MEMBER_KEY] ?: false
        }
    }
}