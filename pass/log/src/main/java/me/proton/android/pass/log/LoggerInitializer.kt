/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.android.pass.log

import android.content.Context
import androidx.startup.Initializer
import me.proton.core.util.kotlin.CoreLogger
import timber.log.Timber

class LoggerInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // Timber.plant(SentryTree())
        }
        PassLogger.deviceInfo()

        // Forward Core Logs to Timber, using AppLogger.
        CoreLogger.set(PassLogger)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return if (BuildConfig.DEBUG) {
            emptyList()
        } else {
            emptyList()
            // listOf(SentryInitializer::class.java)
        }
    }
}