package io.icure.asyncjacksonhttpclient.uribuilder

import io.icure.asyncjacksonhttpclient.net.append
import io.icure.asyncjacksonhttpclient.net.param
import io.icure.asyncjacksonhttpclient.net.params
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.URI

class URITest {

    @Test
    fun `append path component to URI`() {
        val uri = URI.create("http://example.com")
        val pathComponent = "test"
        val expected = URI.create("http://example.com/test")
        assertEquals(expected, uri.append(pathComponent))
    }
    @Test
    fun `append path component to URI with existing path`() {
        val uri = URI.create("http://example.com/path")
        val pathComponent = "test"
        val expected = URI.create("http://example.com/path/test")
        assertEquals(expected, uri.append(pathComponent))
    }
    @Test
    fun `append path component to URI with existing path and trailing slash`() {
        val uri = URI.create("http://example.com/path/")
        val pathComponent = "test"
        val expected = URI.create("http://example.com/path/test")
        assertEquals(expected, uri.append(pathComponent))
    }
    @Test
    fun `append path component to URI with existing path and leading slash`() {
        val uri = URI.create("http://example.com/path")
        val pathComponent = "/test"
        val expected = URI.create("http://example.com/path/test")
        assertEquals(expected, uri.append(pathComponent))
    }
    @Test
    fun `append path component to URI with null path component`() {
        val uri = URI.create("http://example.com")
        val pathComponent = null
        val expected = URI.create("http://example.com")
        assertEquals(expected, uri.append(pathComponent))
    }

    @Test
    fun `add parameter to URI`() {
        val uri = URI.create("http://example.com")
        val parameterKey = "key"
        val parameterValue = "value"
        val expected = URI.create("http://example.com?key=value")
        assertEquals(expected, uri.param(parameterKey, parameterValue))
    }
    @Test
    fun `add parameter to URI with existing parameters`() {
        val uri = URI.create("http://example.com?param1=value1")
        val parameterKey = "key"
        val parameterValue = "value"
        val expected = URI.create("http://example.com?param1=value1&key=value")
        assertEquals(expected, uri.param(parameterKey, parameterValue))
    }
    @Test
    fun `add parameter to URI with existing parameters and fragment`() {
        val uri = URI.create("http://example.com?param1=value1#fragment")
        val parameterKey = "key"
        val parameterValue = "value"
        val expected = URI.create("http://example.com?param1=value1&key=value#fragment")
        assertEquals(expected,  uri.param(parameterKey, parameterValue))
    }

    @Test
    fun `add multiple parameters to URI`() {
        val uri = URI.create("http://example.com")
        val parameters = mapOf("key1" to listOf("value1"), "key2" to listOf("value2"))
        val expected = URI.create("http://example.com?key1=value1&key2=value2")
        assertEquals(expected, uri.params(parameters))
    }
    @Test
    fun `add multiple parameters to URI with existing parameters`() {
        val uri = URI.create("http://example.com?param1=value1")
        val parameters = mapOf("key1" to listOf("value1"), "key2" to listOf("value2"))
        val expected = URI.create("http://example.com?param1=value1&key1=value1&key2=value2")
        assertEquals(expected, uri.params(parameters))
    }
    @Test
    fun `add multiple parameters to URI with existing parameters and fragment`() {
        val uri = URI.create("http://example.com?param1=value1#fragment")
        val parameters = mapOf("key1" to listOf("value1"), "key2" to listOf("value2"))
        val expected = URI.create("http://example.com?param1=value1&key1=value1&key2=value2#fragment")
        assertEquals(expected, uri.params(parameters))
    }
    @Test
    fun `add multiple values for single key to URI`() {
        val uri = URI.create("http://example.com")
        val parameters = mapOf("key" to listOf("value1", "value2"))
        val expected = URI.create("http://example.com?key=value1&key=value2")
        assertEquals(expected, uri.params(parameters))
    }

    @Test
    fun `add parameter to URI with path`() {
        val uri = URI.create("http://example.com/path")
        val parameterKey = "key"
        val parameterValue = "value"
        val expected = URI.create("http://example.com/path?key=value")
        assertEquals(expected, uri.param(parameterKey, parameterValue))
    }
    @Test
    fun `add parameter to URI with path, with existing parameters`() {
        val uri = URI.create("http://example.com/path?param1=value1")
        val parameterKey = "key"
        val parameterValue = "value"
        val expected = URI.create("http://example.com/path?param1=value1&key=value")
        assertEquals(expected, uri.param(parameterKey, parameterValue))
    }
    @Test
    fun `add parameter to URI with path, with existing parameters and fragment`() {
        val uri = URI.create("http://example.com/path?param1=value1#fragment")
        val parameterKey = "key"
        val parameterValue = "value"
        val expected = URI.create("http://example.com/path?param1=value1&key=value#fragment")
        assertEquals(expected,  uri.param(parameterKey, parameterValue))
    }

    @Test
    fun `add multiple parameters to URI with path`() {
        val uri = URI.create("http://example.com/path")
        val parameters = mapOf("key1" to listOf("value1"), "key2" to listOf("value2"))
        val expected = URI.create("http://example.com/path?key1=value1&key2=value2")
        assertEquals(expected, uri.params(parameters))
    }
    @Test
    fun `add multiple parameters to URI with path, with existing parameters`() {
        val uri = URI.create("http://example.com/path?param1=value1")
        val parameters = mapOf("key1" to listOf("value1"), "key2" to listOf("value2"))
        val expected = URI.create("http://example.com/path?param1=value1&key1=value1&key2=value2")
        assertEquals(expected, uri.params(parameters))
    }
    @Test
    fun `add multiple parameters to URI with path, with existing parameters and fragment`() {
        val uri = URI.create("http://example.com/path?param1=value1#fragment")
        val parameters = mapOf("key1" to listOf("value1"), "key2" to listOf("value2"))
        val expected = URI.create("http://example.com/path?param1=value1&key1=value1&key2=value2#fragment")
        assertEquals(expected, uri.params(parameters))
    }
    @Test
    fun `add multiple values for single key to URI with path,`() {
        val uri = URI.create("http://example.com/path")
        val parameters = mapOf("key" to listOf("value1", "value2"))
        val expected = URI.create("http://example.com/path?key=value1&key=value2")
        assertEquals(expected, uri.params(parameters))
    }

    @Test
    fun `parameters in the URI are correctly UrlEncoded even if they contain UTF characters`() {
        val uri = URI.create("https://example.com/path")
            .param("key1","[\"\uFFF0\"]")
            .param("key2", "[\"v1\", \"v2\"]")
        val expected = "https://example.com/path?key1=[%22%25EF%25BF%25B0%22]&key2=[%22v1%22,%20%22v2%22]"
        val actual = uri.toString()
        assertEquals(expected, actual)
    }
}