package io.icure.asyncjacksonhttpclient.netty

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import io.icure.asyncjacksonhttpclient.exception.TimeoutException
import io.icure.asyncjacksonhttpclient.net.web.HttpMethod
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono
import java.net.InetSocketAddress
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

@ExperimentalCoroutinesApi
internal class NettyWebClientTest : StringSpec() {

    private lateinit var server: HttpServer
    private lateinit var baseUri: String

    init {
        beforeSpec {
            server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
                executor = Executors.newCachedThreadPool()

                createContext("/ok") { exchange ->
                    exchange.responseHeaders.add("X-Custom", "custom-value")
                    exchange.respond(200, "hello world")
                }
                createContext("/unauthorized") { exchange ->
                    exchange.respond(401, "bad credentials")
                }
                createContext("/server-error") { exchange ->
                    exchange.respond(503, "service unavailable")
                }
                createContext("/echo") { exchange ->
                    val body = exchange.requestBody.readBytes()
                    exchange.respond(200, body)
                }
                createContext("/slow") { exchange ->
                    Thread.sleep(1500)
                    exchange.respond(200, "too late")
                }
                start()
            }
            baseUri = "http://127.0.0.1:${server.address.port}"
        }

        afterSpec {
            server.stop(0)
        }

        "a successful GET returns the body as text" {
            val text = NettyWebClient()
                .uri("$baseUri/ok")
                .method(HttpMethod.GET)
                .retrieve()
                .toTextFlow()
                .toList()
                .joinToString("") { it.toString() }

            text shouldBe "hello world"
        }

        "a 401 triggers the matching status handler" {
            val ex = shouldThrow<IllegalArgumentException> {
                NettyWebClient()
                    .uri("$baseUri/unauthorized")
                    .method(HttpMethod.GET)
                    .basicAuth("aaaa", "bbbb")
                    .retrieve()
                    .onStatus(401) { Mono.error(IllegalArgumentException("Bad credentials")) }
                    .toTextFlow()
                    .toList()
            }
            ex.message shouldBe "Bad credentials"
        }

        "the timing handler is invoked with a positive elapsed time and the response headers" {
            val timing = AtomicLong(-1)
            val seenHeaders = AtomicReference<Map<String, List<String>>>()
            val timingCalled = CountDownLatch(1)

            shouldThrow<IllegalArgumentException> {
                NettyWebClient()
                    .uri("$baseUri/unauthorized")
                    .method(HttpMethod.GET)
                    .retrieve()
                    .onStatus(401) { Mono.error(IllegalArgumentException("Bad credentials")) }
                    .withTiming { elapsed, headers ->
                        mono {
                            timing.set(elapsed)
                            seenHeaders.set(headers)
                            timingCalled.countDown()
                        }
                    }
                    .toTextFlow()
                    .toList()
            }

            timingCalled.await(5, TimeUnit.SECONDS).shouldBeTrue()
            timing.get() shouldBeGreaterThan -1
            seenHeaders.get().keys.any { it.equals("content-length", ignoreCase = true) }.shouldBeTrue()
        }

        "a status-class handler matches any status in that class" {
            val ex = shouldThrow<IllegalStateException> {
                NettyWebClient()
                    .uri("$baseUri/server-error")
                    .method(HttpMethod.GET)
                    .retrieve()
                    .onStatus(500) { Mono.error(IllegalStateException("Server said: ${it.responseBodyAsString()}")) }
                    .toTextFlow()
                    .toList()
            }
            ex.message shouldContain "service unavailable"
        }

        "the onHeader handler receives the matching header value" {
            val headerValue = AtomicReference<String>()
            val headerCalled = CountDownLatch(1)

            NettyWebClient()
                .uri("$baseUri/ok")
                .method(HttpMethod.GET)
                .retrieve()
                // onHeader matches the header name exactly; the JDK HttpServer normalises
                // "X-Custom" to "X-custom" on the wire, which is what Netty sees.
                .onHeader("X-custom") { value ->
                    mono {
                        headerValue.set(value)
                        headerCalled.countDown()
                    }
                }
                .toTextFlow()
                .toList()

            headerCalled.await(5, TimeUnit.SECONDS).shouldBeTrue()
            headerValue.get() shouldBe "custom-value"
        }

        "a POST body is sent and echoed back" {
            val text = NettyWebClient()
                .uri("$baseUri/echo")
                .method(HttpMethod.POST)
                .body("ping")
                .retrieve()
                .toTextFlow()
                .toList()
                .joinToString("") { it.toString() }

            text shouldBe "ping"
        }

        "a response slower than the timeout raises a TimeoutException" {
            shouldThrow<TimeoutException> {
                NettyWebClient()
                    .uri("$baseUri/slow")
                    .method(HttpMethod.GET, Duration.ofMillis(200))
                    .retrieve()
                    .toTextFlow()
                    .toList()
            }
        }
    }
}

private fun HttpExchange.respond(status: Int, body: String) = respond(status, body.toByteArray(Charsets.UTF_8))

private fun HttpExchange.respond(status: Int, body: ByteArray) {
    sendResponseHeaders(status, body.size.toLong())
    responseBody.use { it.write(body) }
}
