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
    (this.query?.split("&") ?: emptyList()).filter { it.isNotBlank() }.plus("$k=$v").joinToString("&"),
    this.fragment
)

fun URI.params(map: Map<String, List<String>>): URI = map.entries.fold(this) { uri, (k, values) ->
    values.fold(uri) { uri, v ->
        uri.param(k, v)
    }
}