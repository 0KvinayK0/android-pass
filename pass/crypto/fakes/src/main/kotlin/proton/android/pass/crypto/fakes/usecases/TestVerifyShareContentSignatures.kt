package proton.android.pass.crypto.fakes.usecases

import proton.android.pass.crypto.api.error.InvalidSignature
import proton.android.pass.crypto.api.usecases.EncryptedShareResponse
import proton.android.pass.crypto.api.usecases.VerifyShareContentSignatures
import me.proton.core.key.domain.entity.key.PublicKey
import proton.pass.domain.key.VaultKey

class TestVerifyShareContentSignatures(private val success: Boolean = true) :
    VerifyShareContentSignatures {
    override fun invoke(
        response: EncryptedShareResponse,
        contentSignatureKeys: List<PublicKey>,
        vaultKeys: List<VaultKey>
    ) {
        if (!success) throw InvalidSignature("TEST EXCEPTION")
    }
}