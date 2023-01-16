package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.GetCurrentShare
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Result
import proton.pass.domain.Share
import javax.inject.Inject

class GetCurrentShareImpl @Inject constructor(
    private val sharesRepository: ShareRepository
) : GetCurrentShare {
    override suspend operator fun invoke(userId: UserId): Result<List<Share>> =
        sharesRepository.observeShares(userId)
            .filterNotNull()
            .firstOrNull()
            ?: Result.Error()
}
