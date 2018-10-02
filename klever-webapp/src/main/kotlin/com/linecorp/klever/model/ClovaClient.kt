package com.linecorp.klever.model

data class ClovaClient(
    val clientId: Int = 0,
    val userId: Int = 0,
    val appId: String = "",
    val code: String = ""
) {
    companion object {
        const val NOT_EXIST = -1
    }
}
