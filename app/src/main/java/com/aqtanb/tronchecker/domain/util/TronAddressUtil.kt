package com.aqtanb.tronchecker.domain.util

import org.bitcoinj.core.Base58
import java.security.MessageDigest

object TronAddressUtil {
    fun convertHexToTronAddress(hexAddress: String): String {
        return try {
            when {
                hexAddress.isBlank() -> "Unknown"
                hexAddress.startsWith("T") -> hexAddress
                hexAddress.length == 42 && hexAddress.matches(Regex("[0-9a-fA-F]+")) -> {
                    val hexBytes = hexAddress.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
                    val hash1 = MessageDigest.getInstance("SHA-256").digest(hexBytes)
                    val hash2 = MessageDigest.getInstance("SHA-256").digest(hash1)
                    val checksum = hash2.take(4).toByteArray()
                    val addressWithChecksum = hexBytes + checksum
                    Base58.encode(addressWithChecksum)
                }
                else -> hexAddress
            }
        } catch (_: Exception) {
            "T${hexAddress.take(6)}...${hexAddress.takeLast(6)}"
        }
    }
}