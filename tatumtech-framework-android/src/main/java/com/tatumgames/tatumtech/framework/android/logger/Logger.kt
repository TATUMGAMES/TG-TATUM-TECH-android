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
package com.tatumgames.tatumtech.framework.android.logger

import android.util.Log
import com.tatumgames.tatumtech.framework.android.constants.Constants

object Logger {

    /**
     * Helper method for logging e-verbose.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     * activity where the log call occurs.
     * @param message The message you would like log.
     */
    fun e(tag: String, message: String?) {
        if (Constants.DEBUG) {
            Log.e(tag, message.orEmpty())
        }
    }

    /**
     * Helper method for logging e-verbose.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     * activity where the log call occurs.
     * @param message The message you would like log.
     * @param exception An object that wraps an error event that occurred and contains information
     * about the error including its type.
     */
    fun e(tag: String, message: String?, exception: Exception) {
        if (Constants.DEBUG) {
            Log.e(tag, message.orEmpty(), exception)
        }
    }

    /**
     * Helper method for logging d-verbose.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     * activity where the log call occurs.
     * @param message The message you would like log.
     */
    fun d(tag: String, message: String?) {
        if (Constants.DEBUG) {
            Log.d(tag, message.orEmpty())
        }
    }

    /**
     * Helper method for logging i-verbose.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     * activity where the log call occurs.
     * @param message The message you would like log.
     */
    fun i(tag: String, message: String?) {
        if (Constants.DEBUG) {
            Log.i(tag, message.orEmpty())
        }
    }

    /**
     * Helper method for logging v-verbose.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     * activity where the log call occurs.
     * @param message The message you would like log.
     */
    fun v(tag: String, message: String?) {
        if (Constants.DEBUG) {
            Log.v(tag, message.orEmpty())
        }
    }

    /**
     * Helper method for logging w-verbose.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     * activity where the log call occurs.
     * @param message The message you would like log.
     */
    fun w(tag: String, message: String?) {
        if (Constants.DEBUG) {
            Log.w(tag, message.orEmpty())
        }
    }

    /**
     * Helper method to display data on Console.
     *
     * @param message The message to be displayed.
     */
    fun printOnConsole(message: String) {
        if (Constants.DEBUG) {
            println(message)
        }
    }
}
