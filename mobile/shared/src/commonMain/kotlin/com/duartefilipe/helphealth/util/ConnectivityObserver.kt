package com.duartefilipe.helphealth.util

enum class NetworkStatus {
    Available,
    Unavailable
}

interface ConnectivityObserver {
    fun currentStatus(): NetworkStatus
}
