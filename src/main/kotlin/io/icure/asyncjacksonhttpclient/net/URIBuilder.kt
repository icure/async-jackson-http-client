/*
 *    Copyright 2020 Taktik SA
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package io.icure.asyncjacksonhttpclient.net

import java.net.URI
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets
import java.text.Normalizer

fun URI.append(pathComponent: String?): URI = pathComponent?.let { p -> URI(
    this.scheme,
    this.userInfo,
    this.host,
    this.port,
    ("${this.path.trimEnd('/')}/${p.trim('/')}"),
    this.query,
    this.fragment
) } ?: this

fun URI.param(k: String, v: String): URI = URI(
    this.scheme,
    this.userInfo,
    this.host,
    this.port,
    this.path,
    (this.query?.split("&") ?: emptyList())
        .filter { it.isNotBlank() }
        .plus("${encodeUtfCharacters(k)}=${encodeUtfCharacters(v)}")
        .joinToString("&"),
    this.fragment
)

fun URI.params(map: Map<String, List<String>>): URI = map.entries.fold(this) { uri, (k, values) ->
    values.fold(uri) { accUri, v ->
        accUri.param(k, v)
    }
}

// -- Escaping and encoding --
private val hexDigits = charArrayOf(
    '0', '1', '2', '3', '4', '5', '6', '7',
    '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
)

fun StringBuilder.appendEscape(b: Int) {
    append('%')
    append(hexDigits[(b shr 4) and 0x0f])
    append(hexDigits[b and 0x0F])
}

private fun encodeUtfCharacters(s: String): String {
    if(s.isEmpty()) return s

    // First check whether we actually need to encode
    s.firstOrNull { it >= '\u0080' } ?: return s

    val ns = Normalizer.normalize(s, Normalizer.Form.NFC)
    val bb: ByteBuffer = StandardCharsets.UTF_8.newEncoder().encode(CharBuffer.wrap(ns))

    return buildString {
        while (bb.hasRemaining()) {
            val b = bb.get().toInt() and 0xff
            if (b >= 0x80) {
                appendEscape(b)
            } else {
                append(b.toChar())
            }
        }
    }
}