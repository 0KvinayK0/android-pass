/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.data.fakes.usecases

import proton.android.pass.data.api.usecases.MigrateVault
import proton.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestMigrateVault @Inject constructor() : MigrateVault {

    private var result: Result<Unit> = Result.success(Unit)

    private val memory = mutableListOf<Memory>()

    fun memory(): List<Memory> = memory

    fun setResult(value: Result<Unit>) {
        result = value
    }

    override suspend fun invoke(origin: ShareId, dest: ShareId) {
        memory.add(Memory(origin, dest))
        result.getOrThrow()
    }

    data class Memory(
        val origin: ShareId,
        val destination: ShareId
    )
}
