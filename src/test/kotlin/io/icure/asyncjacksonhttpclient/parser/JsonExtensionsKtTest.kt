package io.icure.asyncjacksonhttpclient.parser

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import java.nio.ByteBuffer

@OptIn(ExperimentalCoroutinesApi::class)
class JsonExtensionsKtTest : StringSpec({

    val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build()).registerModule(JavaTimeModule()).apply {
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
        configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true)
    }

    suspend fun flowToTestClass(flow: Flow<ByteBuffer>): TestClass? =
        flow.toObject(object : TypeReference<TestClass>() {}, objectMapper, true)

    fun bytesOf(value: Any): ByteBuffer = ByteBuffer.wrap(objectMapper.writeValueAsBytes(value))

    fun newAsyncParser() = objectMapper.factory.createNonBlockingByteArrayParser()

    "deserializes a single object from a flow" {
        val item: TestClass? = flowToTestClass(flowOf(bytesOf(TestClass("1", listOf("A", "AA")))))
        item shouldBe TestClass("1", listOf("A", "AA"))
    }

    "deserializes a list of objects from a flow" {
        val source = listOf(TestClass("1", listOf("A", "AA")), TestClass("2", listOf("B", "BB")))
        val bytes = objectMapper.writeValueAsBytes(source)

        val viaTypeReference: List<TestClass>? =
            flowOf(ByteBuffer.wrap(bytes)).toObject(object : TypeReference<List<TestClass>>() {}, objectMapper, true)
        val viaReadValue: List<TestClass> = objectMapper.readValue(bytes)
        val viaInline: List<TestClass>? = flowOf(ByteBuffer.wrap(bytes)).toObject(objectMapper, true)

        viaTypeReference shouldBe source
        viaReadValue shouldBe source
        viaInline shouldBe source
    }

    "deserializes using the reified overload" {
        val item: TestClass? = flowOf(bytesOf(TestClass("42", listOf("X")))).toObject(objectMapper, true)
        item shouldBe TestClass("42", listOf("X"))
    }

    "reassembles an object whose bytes are split across multiple buffers" {
        val bytes = objectMapper.writeValueAsBytes(TestClass("split", listOf("a", "b")))
        val mid = bytes.size / 2
        val chunks = flowOf(
            ByteBuffer.wrap(bytes.copyOfRange(0, mid)),
            ByteBuffer.wrap(bytes.copyOfRange(mid, bytes.size))
        )
        val item: TestClass? = chunks.toObject(objectMapper, true)
        item shouldBe TestClass("split", listOf("a", "b"))
    }

    "an empty response deserializes to null when emptyResponseAsNull is true" {
        val item: TestClass? = flowOf(ByteBuffer.wrap(ByteArray(0))).toObject(objectMapper, true)
        item.shouldBeNull()
    }

    "a JSON null deserializes to null" {
        val item: TestClass? = flowOf(ByteBuffer.wrap("null".toByteArray())).toObject(objectMapper, true)
        item.shouldBeNull()
    }

    "toJsonEvents emits the expected event sequence for an object" {
        val bytes = "{\"id\":\"1\",\"names\":[\"A\"]}".toByteArray()
        val events = flowOf(ByteBuffer.wrap(bytes)).toJsonEvents(newAsyncParser()).toList()

        events.filterIsInstance<FieldName>().map { it.name } shouldContainExactly listOf("id", "names")
        events.first() shouldBe StartObject
        events.last() shouldBe EndObject
    }

    "toJsonEvents distinguishes numeric value types" {
        val bytes = "[1,2147483648,1.5]".toByteArray()
        val values = flowOf(ByteBuffer.wrap(bytes)).toJsonEvents(newAsyncParser()).toList()
            .filterIsInstance<NumberValue<*>>()

        values[0].shouldBeInstanceOf<IntValue>()
        values[1].shouldBeInstanceOf<LongValue>()
        values[2].shouldBeInstanceOf<DoubleValue>()
        values.map { it.value } shouldContainExactly listOf(1, 2147483648L, 1.5)
    }

    "toJsonEvents emits boolean and null sentinels" {
        val bytes = "[true,false,null]".toByteArray()
        val events = flowOf(ByteBuffer.wrap(bytes)).toJsonEvents(newAsyncParser()).toList()

        events shouldContainExactly listOf(StartArray, TrueValue, FalseValue, NullValue, EndArray)
    }

    "the iterable toJsonEvents overload yields the same events as the flow overload" {
        val bytes = "{\"id\":\"1\"}".toByteArray()
        val events = listOf(ByteBuffer.wrap(bytes)).toJsonEvents(newAsyncParser())

        events.first() shouldBe StartObject
        events.filterIsInstance<FieldName>().single().name shouldBe "id"
        events.last() shouldBe EndObject
    }
})

data class TestClass(val id: String, val names: List<String> = listOf())
