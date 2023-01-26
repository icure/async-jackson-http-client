/*
 * Copyright (C) 2007, 2008 Apple Inc.  All rights reserved.
 * Copyright (C) 2008, 2009 Anthony Ricaud <rik@webkit.org>
 * Copyright (C) 2011 Google Inc. All rights reserved.
 * Copyright (C) 2016 Maciej Gawinecki <mgawinecki@gmail.com>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1.  Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 * 2.  Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 * 3.  Neither the name of Apple Computer, Inc. ("Apple") nor the names of
 *     its contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY APPLE AND ITS CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL APPLE OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.icure.asyncjacksonhttpclient

import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors

/**
 * Represents curl command and provides a way to serialize it through [.asString] method.
 */
class CurlCommand {
    private val headers: MutableList<Header> = ArrayList()
    private val formParts: MutableList<FormPart> = ArrayList()
    private val datasBinary: MutableList<String> = ArrayList()
    private var url: String? = null
    private var cookieHeader = Optional.empty<String>()
    private var compressed = false
    private var verbose = false
    private var insecure = false
    private var method = Optional.empty<String>()
    private var serverAuthentication: Optional<ServerAuthentication> = Optional.empty()
    fun setUrl(url: String?): CurlCommand {
        this.url = url
        return this
    }

    fun addHeader(name: String, value: String): CurlCommand {
        headers.add(Header(name, value))
        return this
    }

    fun removeHeader(name: String): CurlCommand {
        headers.removeIf { header: Header -> header.name == name }
        return this
    }

    fun addFormPart(name: String, content: String): CurlCommand {
        formParts.add(FormPart(name, content))
        return this
    }

    fun addDataBinary(dataBinary: String): CurlCommand {
        datasBinary.add(dataBinary)
        return this
    }

    fun setCookieHeader(cookieHeader: String): CurlCommand {
        this.cookieHeader = Optional.of(cookieHeader)
        return this
    }

    fun setCompressed(compressed: Boolean): CurlCommand {
        this.compressed = compressed
        return this
    }

    fun setVerbose(verbose: Boolean): CurlCommand {
        this.verbose = verbose
        return this
    }

    fun setInsecure(insecure: Boolean): CurlCommand {
        this.insecure = insecure
        return this
    }

    fun setMethod(method: String): CurlCommand {
        this.method = Optional.of(method)
        return this
    }

    fun setServerAuthentication(user: String, password: String): CurlCommand {
        serverAuthentication = Optional.of(ServerAuthentication(user, password))
        return this
    }

    override fun toString(): String {
        return asString(Platform.RECOGNIZE_AUTOMATICALLY, false, true, true)
    }

    fun asString(
        targetPlatform: Platform,
        useShortForm: Boolean,
        printMultiliner: Boolean,
        escapeNonAscii: Boolean
    ): String {
        return Serializer(targetPlatform, useShortForm, printMultiliner, escapeNonAscii).serialize(this)
    }

    fun hasData(): Boolean {
        return !datasBinary.isEmpty()
    }

    class Header(val name: String, val value: String)
    class FormPart(val name: String, val content: String)
    class ServerAuthentication(val user: String, val password: String)
    private class Serializer(
        targetPlatform: Platform,
        useShortForm: Boolean,
        printMultiliner: Boolean,
        escapeNonAscii: Boolean
    ) {
        private val targetPlatform: Platform
        private val useShortForm: Boolean
        private val printMultiliner: Boolean
        private val escapeNonAscii: Boolean

        companion object {
            private val SHORT_PARAMETER_NAMES: MutableMap<String, String?> = HashMap()
            private fun parameterName(longParameterName: String, useShortForm: Boolean): String {
                return if (useShortForm) (if (SHORT_PARAMETER_NAMES[longParameterName] == null) longParameterName else SHORT_PARAMETER_NAMES[longParameterName])!! else longParameterName
            }

            private fun line(
                useShortForm: Boolean, longParameterName: String,
                vararg arguments: String
            ): List<String> {
                val line = mutableListOf(*arguments)
                line.add(0, parameterName(longParameterName, useShortForm))
                return line
            }

            /**
             * Replace quote by double quote (but not by \") because it is recognized by both cmd.exe and MS
             * Crt arguments parser.
             *
             *
             * Replace % by "%" because it could be expanded to an environment variable value. So %% becomes
             * "%""%". Even if an env variable "" (2 doublequotes) is declared, the cmd.exe will not
             * substitute it with its value.
             *
             *
             * Replace each backslash with double backslash to make sure MS Crt arguments parser won't
             * collapse them.
             *
             *
             * Replace new line outside of quotes since cmd.exe doesn't let to do it inside.
             */
            private fun escapeStringWin(s: String): String {
                // Escaping non-printable ASCII characters is limited only to few characters
                // Escaping non-ASCII characters is not supported
                return ("\""
                        + s
                    .replace("\"".toRegex(), "\"\"")
                    .replace("%".toRegex(), "\"%\"")
                    .replace("\\\\".toRegex(), "\\\\")
                    .replace("[\r\n]+".toRegex(), "\"^\r\n$0\"")
                        + "\"")
            }

            private fun isAscii(c: Char): Boolean {
                return c.code <= 127
            }

            private fun isAsciiPrintable(c: Char): Boolean {
                return c.code in 32..126
            }

            private fun escapeAsHex(c: Char): String {
                val code = c.code
                return if (code < 256) {
                    String.format("\\x%02x", c.code)
                } else String.format("\\u%04x", c.code)
            }

            private fun quoteStringWin(s: String?): String {
                return ("\""
                        + s
                        + "\"")
            }

            private fun quoteStringPosix(s: String?): String {
                return ("'"
                        + s
                        + "'")
            }

            init {
                SHORT_PARAMETER_NAMES["--user"] = "-u"
                SHORT_PARAMETER_NAMES["--data"] = "-d"
                SHORT_PARAMETER_NAMES["--insecure"] =
                    "-k"
                SHORT_PARAMETER_NAMES["--form"] = "-F"
                SHORT_PARAMETER_NAMES["--cookie"] = "-b"
                SHORT_PARAMETER_NAMES["--header"] = "-H"
                SHORT_PARAMETER_NAMES["--request"] = "-X"
                SHORT_PARAMETER_NAMES["--verbose"] = "-v"
            }
        }

        private fun escapeStringPosix(s: String): String {
            val escaped = s.chars()
                .mapToObj { c: Int -> escape(c.toChar()) }
                .collect(Collectors.joining())
            return if (escaped != s) {
                // ANSI-C Quoting performed
                "$\'$escaped'"
            } else {
                "'$escaped'"
            }
        }

        private fun escape(c: Char): String {
            return if (isAscii(c)) {
                // Perform ANSI-C Quoting for ASCII characters
                // https://www.gnu.org/software/bash/manual/html_node/ANSI_002dC-Quoting.html
                when (c) {
                    '\n' -> "\\n"
                    '\'' -> "\\'"
                    '\t' -> "\\t"
                    '\r' -> "\\r"
                    '@' -> escapeAsHex(c)
                    else -> if (isAsciiPrintable(c)) c.toString() else escapeAsHex(c)
                }
            } else {
                // Perform ANSI-C Quoting for non-ASCII characters
                // https://www.gnu.org/software/bash/manual/html_node/ANSI_002dC-Quoting.html
                if (escapeNonAscii) escapeAsHex(c) else c.toString()
            }
        }

        fun serialize(curl: CurlCommand): String {
            val command: MutableList<List<String>> = ArrayList()
            command
                .add(line(useShortForm, "curl", quoteString(curl.url).replace("[[{}\\\\]]".toRegex(), "\\$&")))
            curl.method.ifPresent { method: String? ->
                command.add(
                    line(
                        useShortForm, "--request",
                        method!!
                    )
                )
            }
            curl.cookieHeader.ifPresent { cookieHeader: String? ->
                command.add(
                    line(
                        useShortForm,
                        "--cookie",
                        quoteString(cookieHeader)
                    )
                )
            }
            curl.headers.forEach(Consumer { header: Header ->
                command.add(
                    line(
                        useShortForm, "--header",
                        quoteString(header.name + ": " + header.value)
                    )
                )
            })
            curl.formParts.forEach(Consumer { formPart: FormPart ->
                command.add(
                    line(
                        useShortForm, "--form",
                        quoteString(formPart.name + "=" + formPart.content)
                    )
                )
            })
            curl.datasBinary
                .forEach(Consumer { data: String ->
                    command.add(
                        line(
                            useShortForm,
                            "--data-binary",
                            escapeString(data)
                        )
                    )
                })
            curl.serverAuthentication.ifPresent { sa: ServerAuthentication ->
                command
                    .add(
                        line(
                            useShortForm,
                            "--user",
                            quoteString(sa.user + ":" + sa.password)
                        )
                    )
            }
            if (curl.compressed) {
                command.add(line(useShortForm, "--compressed"))
            }
            if (curl.insecure) {
                command.add(line(useShortForm, "--insecure"))
            }
            if (curl.verbose) {
                command.add(line(useShortForm, "--verbose"))
            }
            return command.stream()
                .map { line: List<String> ->
                    line.stream().collect(Collectors.joining(" "))
                }
                .collect(Collectors.joining(chooseJoiningString(printMultiliner)))
        }

        private fun chooseJoiningString(printMultiliner: Boolean): CharSequence {
            val commandLineSeparator = if (targetPlatform.isOsWindows) "^" else "\\"
            return if (printMultiliner) java.lang.String.format(
                " %s%s  ",
                commandLineSeparator,
                targetPlatform.lineSeparator()
            ) else " "
        }

        private fun escapeString(s: String): String {
            // cURL command is expected to run on the same platform that test run
            return if (targetPlatform.isOsWindows) escapeStringWin(s) else escapeStringPosix(s)
        }

        private fun quoteString(s: String?): String {
            // cURL command is expected to run on the same platform that test run
            return if (targetPlatform.isOsWindows) quoteStringWin(s) else quoteStringPosix(s)
        }

        init {
            this.targetPlatform = targetPlatform
            this.useShortForm = useShortForm
            this.printMultiliner = printMultiliner
            this.escapeNonAscii = escapeNonAscii
        }
    }
}
enum class Platform(val isOsWindows: Boolean, private val lineSeparator: String) {
    RECOGNIZE_AUTOMATICALLY(
        System.getProperty("os.name") != null && System.getProperty("os.name").startsWith("Windows"),
        System.lineSeparator()
    ),
    WINDOWS(true, "\r\n"), UNIX(false, "\n");

    fun lineSeparator(): String {
        return lineSeparator
    }
}
