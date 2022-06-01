package io.icure.asyncjacksonhttpclient.netty

import io.icure.asyncjacksonhttpclient.net.web.HttpMethod
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.*
import reactor.core.publisher.Mono
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
internal class NettyWebClientTest {
    @org.junit.jupiter.api.Test
    fun testStatusHandlers() = runBlocking {
        var timing = 0L
        try {
            NettyWebClient()
                .uri("https://kraken.icure.dev/rest/v1/user/current")
                .method(HttpMethod.GET)
                .basicAuth("aaaa", "bbbb")
                .retrieve()
                .onStatus(401) { Mono.error(IllegalArgumentException("Bad credentials")) }
                .withTiming { mono {
                    timing = it
                } }
                .toTextFlow().collect {
                    println(it)
                }
            fail("Should catch 401")
        } catch (e:IllegalArgumentException) {
            assertTrue(timing>0)
        }
    }
}
