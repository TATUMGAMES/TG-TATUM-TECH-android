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
import com.tatumgames.tatumtech.android.database.dao.CodingChallengeDao
import com.tatumgames.tatumtech.android.database.dao.CodingQuestionDao
import com.tatumgames.tatumtech.android.database.dao.ConnectionDao
import com.tatumgames.tatumtech.android.database.dao.ContactCardDao
import com.tatumgames.tatumtech.android.database.dao.DemographicDataDao
import com.tatumgames.tatumtech.android.database.dao.EngagementCounterDao
import com.tatumgames.tatumtech.android.database.dao.EventRegistrationDao
import com.tatumgames.tatumtech.android.database.dao.QuizAnswerEventDao
import com.tatumgames.tatumtech.android.database.dao.QuizProgressDao
import com.tatumgames.tatumtech.android.database.dao.TimelineDao
import com.tatumgames.tatumtech.android.database.dao.UserDao
import com.tatumgames.tatumtech.android.database.entity.AttendeeEntity
import com.tatumgames.tatumtech.android.database.entity.CodingChallengeEntity
import com.tatumgames.tatumtech.android.database.entity.CodingQuestionEntity
import com.tatumgames.tatumtech.android.database.entity.ConnectionEntity
import com.tatumgames.tatumtech.android.database.entity.ContactCardEntity
import com.tatumgames.tatumtech.android.database.entity.DemographicDataEntity
import com.tatumgames.tatumtech.android.database.entity.EngagementCounterEntity
import com.tatumgames.tatumtech.android.database.entity.EventRegistrationEntity
import com.tatumgames.tatumtech.android.database.entity.QuizAnswerEventEntity
import com.tatumgames.tatumtech.android.database.entity.QuizProgressEntity
import com.tatumgames.tatumtech.android.database.entity.TimelineEntity
import com.tatumgames.tatumtech.android.database.entity.UserEntity

@Database(
    entities = [
        EventRegistrationEntity::class,
        AttendeeEntity::class,
        TimelineEntity::class,
        CodingChallengeEntity::class,
        CodingQuestionEntity::class,
        QuizProgressEntity::class,
        QuizAnswerEventEntity::class,
        UserEntity::class,
        DemographicDataEntity::class,
        ContactCardEntity::class,
        ConnectionEntity::class,
        EngagementCounterEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventRegistrationDao(): EventRegistrationDao
    abstract fun attendeeDao(): AttendeeDao
    abstract fun timelineDao(): TimelineDao
    abstract fun codingChallengeDao(): CodingChallengeDao
    abstract fun codingQuestionDao(): CodingQuestionDao
    abstract fun quizProgressDao(): QuizProgressDao
    abstract fun quizAnswerEventDao(): QuizAnswerEventDao
    abstract fun userDao(): UserDao
    abstract fun demographicDataDao(): DemographicDataDao
    abstract fun contactCardDao(): ContactCardDao
    abstract fun connectionDao(): ConnectionDao
    abstract fun engagementCounterDao(): EngagementCounterDao

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
