package com.aqtanb.tronchecker.presentation.util

object ErrorHandler {
    fun buildUserFriendlyMessage(error: Throwable): String {
        return when {
            error.message?.contains("network", ignoreCase = true) == true ->
                "Network error. Please check your internet connection."
            error.message?.contains("timeout", ignoreCase = true) == true ->
                "Request timed out. Please try again."
            error.message?.contains("not found", ignoreCase = true) == true ->
                "No transactions found for this address."
            else ->
                "Failed to load transactions. Please try again."
        }
    }
}