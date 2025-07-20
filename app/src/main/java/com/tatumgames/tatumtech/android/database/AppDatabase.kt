/**
 * Copyright 2013-present Tatum Games, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tatumgames.tatumtech.android.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tatumgames.tatumtech.android.database.dao.AttendeeDao
import com.tatumgames.tatumtech.android.database.dao.ChallengeAnswerDao
import com.tatumgames.tatumtech.android.database.dao.EventRegistrationDao
import com.tatumgames.tatumtech.android.database.dao.TimelineDao
import com.tatumgames.tatumtech.android.database.entity.AttendeeEntity
import com.tatumgames.tatumtech.android.database.entity.ChallengeAnswerEntity
import com.tatumgames.tatumtech.android.database.entity.EventRegistrationEntity
import com.tatumgames.tatumtech.android.database.entity.TimelineEntity

@Database(
    entities = [
        EventRegistrationEntity::class,
        AttendeeEntity::class,
        TimelineEntity::class,
        ChallengeAnswerEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventRegistrationDao(): EventRegistrationDao
    abstract fun attendeeDao(): AttendeeDao
    abstract fun timelineDao(): TimelineDao
    abstract fun challengeAnswerDao(): ChallengeAnswerDao

    companion object {
        private const val DB_NAME = "tatum_tech.db"
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
        }
    }
}
