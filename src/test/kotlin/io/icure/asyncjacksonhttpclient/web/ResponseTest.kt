package io.icure.asyncjacksonhttpclient.web

import io.icure.asyncjacksonhttpclient.net.web.Response
import io.icure.asyncjacksonhttpclient.net.web.ResponseStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.ByteBuffer
import java.time.Duration
import kotlin.random.Random

val random = Random(123)
@OptIn(ExperimentalCoroutinesApi::class)
class BinaryTestResponse: Response {
    override fun toFlux(): Publisher<ByteBuffer> = Flux.interval(Duration.ofMillis(1)).map { ByteBuffer.wrap(random.nextBytes(32)) }
    override fun onStatus(status: Int, handler: (ResponseStatus) -> Mono<out Throwable>): BinaryTestResponse = BinaryTestResponse()
    override fun onHeader(header: String, handler: (String) -> Mono<Unit>): BinaryTestResponse = BinaryTestResponse()
    override fun withTiming(handler: (Long) -> Mono<Unit>): BinaryTestResponse = BinaryTestResponse()
}

val allowedChars = (' '..'\uFF0F')
@OptIn(ExperimentalCoroutinesApi::class)
class CharTestResponse: Response {
    override fun toFlux(): Publisher<ByteBuffer> = Flux.interval(Duration.ofMillis(1)).map {
        val s = (0..31).map { allowedChars.random() }.joinToString("")
        ByteBuffer.wrap(s.toByteArray())
    }
    override fun onStatus(status: Int, handler: (ResponseStatus) -> Mono<out Throwable>): CharTestResponse = CharTestResponse()
    override fun onHeader(header: String, handler: (String) -> Mono<Unit>): CharTestResponse = CharTestResponse()
    override fun withTiming(handler: (Long) -> Mono<Unit>): CharTestResponse = CharTestResponse()
}

@OptIn(ExperimentalCoroutinesApi::class)
class TrivialTestResponse: Response {
    override fun toFlux(): Publisher<ByteBuffer> = Flux.interval(Duration.ofMillis(1)).map {
        ByteBuffer.wrap("abcdefghijklmnopqrstuvwxyz012345".toByteArray())
    }
    override fun onStatus(status: Int, handler: (ResponseStatus) -> Mono<out Throwable>): CharTestResponse = CharTestResponse()
    override fun onHeader(header: String, handler: (String) -> Mono<Unit>): CharTestResponse = CharTestResponse()
    override fun withTiming(handler: (Long) -> Mono<Unit>): CharTestResponse = CharTestResponse()
}

class ResponseTest {
    @Test
    fun testFlowOfTrivialChars() = runBlocking {
        val result = TrivialTestResponse().toTextFlow().take(1024).map {
            it.toString().also {
                assertEquals("abcdefghijklmnopqrstuvwxyz012345", it)
                println(it)
            }
        }.toList()
        assertEquals(1024, result.size)
    }

    @Test
    fun testFlowOfChars() = runBlocking {
        val result = CharTestResponse().toTextFlow().take(1024).map {
            it.toString().also {
                assertEquals(32, it.length)
                println(it)
            }
        }.toList()
        assertEquals(1024, result.size)
    }

    @Test
    fun testFlowOfBinary() = runBlocking {
        val result = BinaryTestResponse().toTextFlow().take(1024).map {
            it.toString().also { println(it) }
        }.toList()
        assertEquals(1024, result.size)
    }
}
