package com.example.core.ble

enum class BLELifecycleState {
    Disconnected,
    Scanning,
    Connecting,
    ConnectedDiscovering,
    ConnectedSubscribing,
    Connected
}
