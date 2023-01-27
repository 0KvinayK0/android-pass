package proton.android.pass.totp.fakes

import proton.android.pass.common.api.Result
import proton.android.pass.totp.api.TotpManager
import proton.android.pass.totp.api.TotpSpec
import javax.inject.Inject

class TestTotpManager @Inject constructor() : TotpManager {

    override fun generateUri(spec: TotpSpec): String = ""

    override fun calculateCode(spec: TotpSpec): String = ""
    override fun parse(uri: String): Result<TotpSpec> = Result.Loading
}