package io.icure.asyncjacksonhttpclient.netty

import io.icure.asyncjacksonhttpclient.net.web.HttpMethod
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import reactor.core.publisher.Mono

@ExperimentalCoroutinesApi
internal class NettyWebClientTest {
    @org.junit.jupiter.api.Test
    fun testStatusHandlers() = runBlocking {
        try {
            NettyWebClient()
                .uri("https://kraken.icure.dev/rest/v1/user/current")
                .method(HttpMethod.GET)
                .basicAuth("a", "a")
                .retrieve()
                .onStatus(401) { Mono.error(IllegalArgumentException("Bad credentials")) }
                .toTextFlow().collect {
                    println(it)
                }
            fail("Should catch 401")
        } catch (e:IllegalArgumentException) {
            //Do nothing
        }
    }
}
