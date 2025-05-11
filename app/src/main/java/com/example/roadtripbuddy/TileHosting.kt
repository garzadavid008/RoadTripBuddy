package com.example.roadtripbuddy

import okhttp3.OkHttpClient


private val tileHosts = listOf(
    "a.api.tomtom.com",
    "b.api.tomtom.com",
    "c.api.tomtom.com",
    "d.api.tomtom.com"
)
private val nextHost = java.util.concurrent.atomic.AtomicInteger(0)

private fun pickHost(): String {
    val idx = nextHost.getAndUpdate { (it + 1) % tileHosts.size }
    return tileHosts[idx]
}

val tiledHttpClient = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val req = chain.request()
        val url = req.url
        if (url.host == "api.tomtom.com") {
            val newHost = pickHost()
            val newUrl = url.newBuilder()
                .host(newHost)
                .build()
            val newReq = req.newBuilder()
                .url(newUrl)
                .build()
            chain.proceed(newReq)
        } else {
            chain.proceed(req)
        }
    }
    .build()