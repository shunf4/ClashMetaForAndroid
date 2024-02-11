package com.github.kr328.clash.core.bridge

import androidx.annotation.Keep

@Keep
interface ClashraySendReceiveCallbackInterface {
    fun received(jsonPayload: String)
}