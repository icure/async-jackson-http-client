package io.icure.asyncjacksonhttpclient.parser

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import java.nio.ByteBuffer

class JsonExtensionsKtTest {
    val objectMapper = ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule()).apply {
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
        configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true)
    }

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
        val items2: List<TestClass> = flowToObject(flowOf(ByteBuffer.wrap(bytes)))!!
        assertTrue(items1[1] is TestClass)
        assertTrue(items2[2] is TestClass)
    }

    suspend inline fun <reified T>flowToObject(flow: Flow<ByteBuffer>): T? {
        return flow.toObject(T::class.java, objectMapper, true)
    }

}

data class TestClass(val id:String, val names:List<String> = listOf())
