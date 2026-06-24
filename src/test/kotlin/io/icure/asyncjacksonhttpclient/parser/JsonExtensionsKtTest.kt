package io.icure.asyncjacksonhttpclient.parser

import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.core.json.JsonReadFeature
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import tools.jackson.module.kotlin.readValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import java.nio.ByteBuffer

class JsonExtensionsKtTest {
    // Jackson 3: ObjectMapper is immutable and configured through a builder. The Kotlin module is added
    // explicitly; Java 8 date/time support is built into jackson-databind and registered automatically
    // (the former JavaTimeModule no longer exists). JsonReadFeature is set directly on the builder, so the
    // 2.x `JsonReadFeature.mappedFeature()` bridge is gone.
    val objectMapper = JsonMapper.builder()
        .addModule(KotlinModule.Builder().build())
        .changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_NULL) }
        .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
        .build()

    @org.junit.jupiter.api.Test
    fun testObject() = runBlocking {
        val bytes = objectMapper.writeValueAsBytes(TestClass("1", listOf("A","AA")))
        val item: TestClass = flowToObject(flowOf(ByteBuffer.wrap(bytes)))!!
        assertTrue(item is TestClass)
    }

    @org.junit.jupiter.api.Test
    fun testFlowOfObjects() = runBlocking {
        val bytes = objectMapper.writeValueAsBytes(listOf(TestClass("1", listOf("A","AA")), TestClass("2", listOf("B","BB"))))
        val items1: List<TestClass> = flowOf(ByteBuffer.wrap(bytes)).toObject(object : TypeReference<List<TestClass>>() {}, objectMapper, true)!!
        val items2: List<TestClass> = objectMapper.readValue(bytes)
        val items3: List<TestClass> = flowToObject(flowOf(ByteBuffer.wrap(bytes)))!!
        assertTrue(items1[0] is TestClass)
        assertTrue(items2[0] is TestClass)
        assertTrue(items3[0] is TestClass)
    }

    suspend inline fun <reified T>flowToObject(flow: Flow<ByteBuffer>): T? {
        return flow.toObject(objectMapper, true)
    }

}

data class TestClass(val id:String, val names:List<String> = listOf())
