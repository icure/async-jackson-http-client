package io.icure.asyncjacksonhttpclient.uribuilder

import io.icure.asyncjacksonhttpclient.net.addSinglePathComponent
import io.icure.asyncjacksonhttpclient.net.param
import io.icure.asyncjacksonhttpclient.net.params
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.net.URI

class URITest : StringSpec({

    "append path component to URI" {
        val uri = URI.create("http://example.com")
        uri.addSinglePathComponent("test") shouldBe URI.create("http://example.com/test")
    }

    "append path component to URI with characters that should be URLEncoded" {
        val uri = URI.create("http://example.com")
        uri.addSinglePathComponent("test|param") shouldBe URI.create("http://example.com/test%7Cparam")
    }

    "append path component to URI with characters that should be URLEncoded and then a parameter" {
        val uri = URI.create("http://example.com")
        uri.addSinglePathComponent("test|param").param("key", "value+2") shouldBe
            URI.create("http://example.com/test%7Cparam?key=value%2B2")
    }

    "append path component to URI with existing path" {
        val uri = URI.create("http://example.com/path")
        uri.addSinglePathComponent("test") shouldBe URI.create("http://example.com/path/test")
    }

    "append path component to URI with existing path and trailing slash" {
        val uri = URI.create("http://example.com/path/")
        uri.addSinglePathComponent("test") shouldBe URI.create("http://example.com/path/test")
    }

    "append path component to URI with existing path and leading slash" {
        val uri = URI.create("http://example.com/path")
        uri.addSinglePathComponent("/test") shouldBe URI.create("http://example.com/path/test")
    }

    "append path component to URI with null path component" {
        val uri = URI.create("http://example.com")
        uri.addSinglePathComponent(null) shouldBe URI.create("http://example.com")
    }

    "append multiple path components sequentially" {
        val uri = URI.create("http://example.com")
        uri.addSinglePathComponent("a").addSinglePathComponent("b").addSinglePathComponent("c") shouldBe
            URI.create("http://example.com/a/b/c")
    }

    "append path component preserves existing query parameters" {
        val uri = URI.create("http://example.com/path?key=value")
        uri.addSinglePathComponent("test") shouldBe URI.create("http://example.com/path/test?key=value")
    }

    "add parameter to URI" {
        val uri = URI.create("http://example.com")
        uri.param("key", "value") shouldBe URI.create("http://example.com?key=value")
    }

    "add parameter to URI with existing parameters" {
        val uri = URI.create("http://example.com?param1=value1")
        uri.param("key", "value") shouldBe URI.create("http://example.com?param1=value1&key=value")
    }

    "add parameter to URI with existing parameters and fragment" {
        val uri = URI.create("http://example.com?param1=value1#fragment")
        uri.param("key", "value") shouldBe URI.create("http://example.com?param1=value1&key=value#fragment")
    }

    "add multiple parameters to URI" {
        val uri = URI.create("http://example.com")
        val parameters = mapOf("key1" to listOf("value1"), "key2" to listOf("value2"))
        uri.params(parameters) shouldBe URI.create("http://example.com?key1=value1&key2=value2")
    }

    "add multiple parameters to URI with existing parameters" {
        val uri = URI.create("http://example.com?param1=value1")
        val parameters = mapOf("key1" to listOf("value1"), "key2" to listOf("value2"))
        uri.params(parameters) shouldBe URI.create("http://example.com?param1=value1&key1=value1&key2=value2")
    }

    "add multiple parameters to URI with existing parameters, user info and fragment" {
        val uri = URI.create("http://a:b@example.com?param1=value1#fragment")
        val parameters = mapOf("key1" to listOf("value1"), "key2" to listOf("value2"))
        uri.params(parameters) shouldBe URI.create("http://a:b@example.com?param1=value1&key1=value1&key2=value2#fragment")
    }

    "add multiple values for single key to URI" {
        val uri = URI.create("http://example.com")
        val parameters = mapOf("key" to listOf("value1", "value2"))
        uri.params(parameters) shouldBe URI.create("http://example.com?key=value1&key=value2")
    }

    "params with an empty map leaves the URI unchanged" {
        val uri = URI.create("http://example.com/path?param1=value1")
        uri.params(emptyMap()) shouldBe uri
    }

    "add parameter to URI with path" {
        val uri = URI.create("http://example.com/path")
        uri.param("key", "value") shouldBe URI.create("http://example.com/path?key=value")
    }

    "add parameter to URI with path, with existing parameters" {
        val uri = URI.create("http://example.com/path?param1=value1")
        uri.param("key", "value") shouldBe URI.create("http://example.com/path?param1=value1&key=value")
    }

    "add parameter to URI with path, with existing parameters and fragment" {
        val uri = URI.create("http://example.com/path?param1=value1#fragment")
        uri.param("key", "value") shouldBe URI.create("http://example.com/path?param1=value1&key=value#fragment")
    }

    "add multiple parameters to URI with path" {
        val uri = URI.create("http://example.com/path")
        val parameters = mapOf("key1" to listOf("value1"), "key2" to listOf("value2"))
        uri.params(parameters) shouldBe URI.create("http://example.com/path?key1=value1&key2=value2")
    }

    "add multiple parameters to URI with path, with existing parameters" {
        val uri = URI.create("http://example.com/path?param1=value1")
        val parameters = mapOf("key1" to listOf("value1"), "key2" to listOf("value2"))
        uri.params(parameters) shouldBe URI.create("http://example.com/path?param1=value1&key1=value1&key2=value2")
    }

    "add multiple parameters to URI with path, with existing parameters and fragment" {
        val uri = URI.create("http://example.com/path?param1=value1#fragment")
        val parameters = mapOf("key1" to listOf("value1"), "key2" to listOf("value2"))
        uri.params(parameters) shouldBe URI.create("http://example.com/path?param1=value1&key1=value1&key2=value2#fragment")
    }

    "add multiple values for single key to URI with path" {
        val uri = URI.create("http://example.com/path")
        val parameters = mapOf("key" to listOf("value1", "value2"))
        uri.params(parameters) shouldBe URI.create("http://example.com/path?key=value1&key=value2")
    }

    "parameters in the URI are correctly UrlEncoded even if they contain UTF characters" {
        val uri = URI.create("https://example.com/path")
            .param("key1", "[\"￰\"]")
            .param("key2", "[\"v1\", \"v2\"]")
        uri.toString() shouldBe "https://example.com/path?key1=%5B%22%EF%BF%B0%22%5D&key2=%5B%22v1%22%2C+%22v2%22%5D"
    }
})
