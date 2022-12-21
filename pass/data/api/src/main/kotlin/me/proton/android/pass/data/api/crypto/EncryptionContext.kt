package me.proton.android.pass.data.api.crypto

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString

interface EncryptionContext {
    fun encrypt(content: String): EncryptedString
    fun encrypt(content: ByteArray): EncryptedByteArray

    fun decrypt(content: EncryptedString): String
    fun decrypt(content: EncryptedByteArray): ByteArray
}