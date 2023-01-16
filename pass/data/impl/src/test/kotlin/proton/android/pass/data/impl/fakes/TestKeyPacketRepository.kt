package proton.android.pass.data.impl.fakes

import proton.android.pass.data.api.repositories.KeyPacketRepository
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Result
import proton.pass.domain.ItemId
import proton.pass.domain.KeyPacket
import proton.pass.domain.ShareId

class TestKeyPacketRepository : KeyPacketRepository {

    private var result: Result<KeyPacket> = Result.Loading

    fun setResult(value: Result<KeyPacket>) {
        result = value
    }

    override suspend fun getLatestKeyPacketForItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Result<KeyPacket> = result
}