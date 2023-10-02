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

fun URI.append(pathComponent: String?): URI = pathComponent?.let { p ->
    URI(
        buildString {
            appendSchemeToPath(this@append.scheme, this@append.userInfo, this@append.host, this@append.port, this@append.path.trimEnd('/'))
            append("/")
            append(p.trim('/'))
            this@append.rawQuery?.takeIf { it.isNotBlank() }?.also {
                append("?$it")
            }
            this@append.fragment?.takeIf { it.isNotBlank() }?.also {
                append("#$it")
            }
        }
    )
} ?: this

fun URI.param(k: String, v: String, uriEncode: Boolean = true): URI = URI(
    buildString {
        appendSchemeToPath(this@param.scheme, this@param.userInfo, this@param.host, this@param.port, this@param.path)
        (this@param.rawQuery?.split("&") ?: emptyList()).filter { it.isNotBlank() }.plus(
            "$k=${
                if (uriEncode) URLEncoder.encode(v, Charsets.UTF_8) else v
            }"
        ).joinToString("&").takeIf { it.isNotBlank() }?.also {
            append("?$it")
        }
        this@param.fragment?.takeIf { it.isNotBlank() }?.also {
            append("#$it")
        }
    }
)

private fun StringBuilder.appendSchemeToPath(
    scheme: String?,
    userInfo: String?,
    host: String?,
    port: Int,
    path: String
) {
    append(scheme)
    append("://")
    userInfo?.takeIf { it.isNotEmpty() }?.also { append("$it@") }
    append(host)
    port.takeIf { it > 0 }?.also { append(":$it") }
    if (path.isNotBlank() && !path.startsWith("/")) {
        append("/")
    }
    append(path)
}

fun URI.params(map: Map<String, List<String>>, uriEncode: Boolean = true): URI =
    map.entries.fold(this) { uri, (k, values) ->
        values.fold(uri) { uri, v ->
            uri.param(k, v, uriEncode)
        }
    }
