/*
 * @(#) JSONPointer.kt
 *
 * json-kotlin-pointer Kotlin implementation of JSON Pointer
 * Copyright (c) 2020 Peter Wall
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pwall.json.pointer

import net.pwall.json.JSONArray
import net.pwall.json.JSONObject
import net.pwall.json.JSONValue
import net.pwall.util.CharUnmapper
import net.pwall.util.Strings

class JSONPointer private constructor(private val tokens: List<String>) {

    constructor(string: String) : this(parse(string))

    fun eval(base: JSONValue?): JSONValue? {
        var result = base
        for (i in tokens.indices) {
            val token = tokens[i]
            when (result) {
                is JSONObject -> {
                    if (!result.containsKey(token))
                        throw JSONPointerException("Can't resolve JSON Pointer ${toString(i + 1)}")
                    result = result[token]
                }
                is JSONArray -> {
                    if (token == "-")
                        throw JSONPointerException("Can't dereference end-of-array JSON Pointer ${toString(i + 1)}")
                    val index = checkIndex(token) { "Illegal array index in JSON Pointer ${toString(i + 1)}" }
                    if (index < 0 || index >= result.size)
                        throw JSONPointerException("Array index out of range in JSON Pointer ${toString(i + 1)}")
                    result = result[index]
                }
                else -> throw JSONPointerException("Can't resolve JSON Pointer ${toString(i + 1)}")
            }
        }
        return result
    }

    fun exists(base: JSONValue?): Boolean {
        var current = base
        for (token in tokens) {
            when (current) {
                is JSONObject -> {
                    if (!current.containsKey(token))
                        return false
                    current = current[token]
                }
                is JSONArray -> {
                    if (!(numberRegex matches token))
                        return false
                    val index = token.toInt()
                    if (index < 0 || index >= current.size)
                        return false
                    current = current[index]
                }
                else -> return false
            }
        }
        return true
    }

    fun parent(): JSONPointer {
        check(tokens.isNotEmpty()) { "Can't get parent of root JSON Pointer" }
        return JSONPointer(tokens.dropLast(1))
    }

    fun child(string: String) = JSONPointer(tokens.plus(string))

    fun child(index: Int): JSONPointer {
        require(index >= 0) { "JSON Pointer index must not be negative" }
        return JSONPointer(tokens.plus(index.toString()))
    }

    fun insert(base: JSONValue, newValue: JSONValue?): JSONValue {
        if (tokens.isEmpty())
            throw JSONPointerException("Can't insert using root JSON Pointer")
        return insertInto(0, base, newValue)
    }

    private fun insertInto(index: Int, base: JSONValue?, newValue: JSONValue?): JSONValue {
        if (base == null)
            throw JSONPointerException("Null JSON Pointer ${toString(index)}")
        val token = tokens[index]
        if (index + 1 < tokens.size) {
            return when (base) {
                is JSONObject -> {
                    if (!base.containsKey(token))
                        throw JSONPointerException("Can't resolve JSON Pointer ${toString(index + 1)}")
                    JSONObject(base).apply {
                        set(token, insertInto(index + 1, base[token], newValue))
                    }
                }
                is JSONArray -> {
                    if (token == "-")
                        throw JSONPointerException("Can't dereference end-of-array JSON Pointer ${toString(index + 1)}")
                    val childIndex = checkIndex(token) { "Illegal array index in JSON Pointer ${toString(index + 1)}" }
                    if (childIndex < 0 || childIndex >= base.size)
                        throw JSONPointerException("Array index out of range in JSON Pointer ${toString(index + 1)}")
                    JSONArray().apply {
                        addAll(base)
                        set(childIndex, insertInto(index + 1, base[childIndex], newValue))
                    }
                }
                else -> throw JSONPointerException("Can't resolve JSON Pointer ${toString(index + 1)}")
            }
        }
        return when (base) {
            is JSONObject -> {
                if (base.containsKey(token))
                    throw JSONPointerException("Can't insert duplicate JSON Pointer $this")
                JSONObject(base).apply {
                    set(token, newValue)
                }
            }
            is JSONArray -> {
                if (token == "-") {
                    JSONArray().apply {
                        addAll(base)
                        addJSON(newValue)
                    }
                }
                else {
                    val childIndex = checkIndex(token) { "Illegal array index in JSON Pointer $this" }
                    if (childIndex < 0 || childIndex > base.size)
                        throw JSONPointerException("Array index out of range in JSON Pointer $this")
                    JSONArray().apply {
                        addAll(base)
                        add(childIndex, newValue)
                    }
                }
            }
            else -> throw JSONPointerException("Can't insert using JSON Pointer $this")
        }
    }

    fun remove(base: JSONValue): JSONValue {
        if (tokens.isEmpty())
            throw JSONPointerException("Can't remove using root JSON Pointer")
        return removeFrom(0, base)
    }

    private fun removeFrom(index: Int, base: JSONValue?): JSONValue {
        if (base == null)
            throw JSONPointerException("Null JSON Pointer ${toString(index)}")
        val token = tokens[index]
        if (index + 1 < tokens.size) {
            return when (base) {
                is JSONObject -> {
                    if (!base.containsKey(token))
                        throw JSONPointerException("Can't resolve JSON Pointer ${toString(index + 1)}")
                    JSONObject(base).apply {
                        set(token, removeFrom(index + 1, base[token]))
                    }
                }
                is JSONArray -> {
                    if (token == "-")
                        throw JSONPointerException("Can't dereference end-of-array JSON Pointer ${toString(index + 1)}")
                    val childIndex = checkIndex(token) { "Illegal array index in JSON Pointer ${toString(index + 1)}" }
                    if (childIndex < 0 || childIndex >= base.size)
                        throw JSONPointerException("Array index out of range in JSON Pointer ${toString(index + 1)}")
                    JSONArray().apply {
                        addAll(base)
                        set(childIndex, removeFrom(index + 1, base[childIndex]))
                    }
                }
                else -> throw JSONPointerException("Can't resolve JSON Pointer ${toString(index + 1)}")
            }
        }
        return when (base) {
            is JSONObject -> {
                if (!base.containsKey(token))
                    throw JSONPointerException("Can't resolve JSON Pointer $this")
                JSONObject(base).apply {
                    remove(token)
                }
            }
            is JSONArray -> {
                if (token == "-")
                    throw JSONPointerException("Can't dereference end-of-array JSON Pointer $this")
                val childIndex = checkIndex(token) { "Illegal array index in JSON Pointer $this" }
                if (childIndex < 0 || childIndex >= base.size)
                    throw JSONPointerException("Array index out of range in JSON Pointer $this")
                JSONArray().apply {
                    addAll(base)
                    removeAt(childIndex)
                }
            }
            else -> throw JSONPointerException("Can't remove using JSON Pointer $this")
        }
    }

    fun replace(base: JSONValue, newValue: JSONValue?): JSONValue {
        if (tokens.isEmpty())
            throw JSONPointerException("Can't replace using root JSON Pointer")
        return replaceWithin(0, base, newValue)
    }

    private fun replaceWithin(index: Int, base: JSONValue?, newValue: JSONValue?): JSONValue {
        if (base == null)
            throw JSONPointerException("Null JSON Pointer ${toString(index)}")
        val token = tokens[index]
        if (index + 1 < tokens.size) {
            return when (base) {
                is JSONObject -> {
                    if (!base.containsKey(token))
                        throw JSONPointerException("Can't resolve JSON Pointer ${toString(index + 1)}")
                    JSONObject(base).apply {
                        set(token, replaceWithin(index + 1, base[token], newValue))
                    }
                }
                is JSONArray -> {
                    if (token == "-")
                        throw JSONPointerException("Can't dereference end-of-array JSON Pointer ${toString(index + 1)}")
                    val childIndex = checkIndex(token) { "Illegal array index in JSON Pointer ${toString(index + 1)}" }
                    if (childIndex < 0 || childIndex >= base.size)
                        throw JSONPointerException("Array index out of range in JSON Pointer ${toString(index + 1)}")
                    JSONArray().apply {
                        addAll(base)
                        set(childIndex, replaceWithin(index + 1, base[childIndex], newValue))
                    }
                }
                else -> throw JSONPointerException("Can't resolve JSON Pointer ${toString(index + 1)}")
            }
        }
        return when (base) {
            is JSONObject -> {
                if (!base.containsKey(token))
                    throw JSONPointerException("Can't resolve JSON Pointer $this")
                JSONObject(base).apply {
                    set(token, newValue)
                }
            }
            is JSONArray -> {
                if (token == "-")
                    throw JSONPointerException("Can't dereference end-of-array JSON Pointer $this")
                val childIndex = checkIndex(token) { "Illegal array index in JSON Pointer $this" }
                if (childIndex < 0 || childIndex > base.size)
                    throw JSONPointerException("Array index out of range in JSON Pointer $this")
                JSONArray().apply {
                    addAll(base)
                    set(childIndex, newValue)
                }
            }
            else -> throw JSONPointerException("Can't replace using JSON Pointer $this")
        }
    }

    fun toString(n: Int) = tokens.subList(0, n).joinToString(separator = "") { "/${escapeToken(it)}" }

    override fun toString() = toString(tokens.size)

    override fun equals(other: Any?) = other is JSONPointer && tokens == other.tokens

    override fun hashCode() = tokens.hashCode()

    private fun checkIndex(token: String, lazyMessage: () -> String): Int {
        if (!(numberRegex matches token))
            throw JSONPointerException(lazyMessage())
        return token.toInt()
    }

    companion object {

        val numberRegex = Regex("^(0|[1-9][0-9]{0,8})$")

        fun parse(string: String): List<String> {
            if (string.isEmpty())
                return emptyList()
            if (!string.startsWith("/"))
                throw JSONPointerException("Illegal JSON Pointer $string")
            return string.substring(1).split('/').map { unescapeToken(it) }
        }

        fun escapeToken(token: String): String {
            return Strings.escape(token) {
                when (it) {
                    '~'.toInt() -> "~0"
                    '/'.toInt() -> "~1"
                    else -> null
                }
            }
        }

        fun unescapeToken(str: String): String = Strings.unescape(str, Unmapper)

        infix fun JSONValue.locate(pointer: JSONPointer) = pointer.eval(this)

        infix fun JSONValue.locate(pointer: String) = JSONPointer(pointer).eval(this)

    }

    object Unmapper : CharUnmapper {

        override fun isEscape(s: CharSequence, offset: Int) = s[offset] == '~'

        override fun unmap(sb: StringBuilder, s: CharSequence, offset: Int): Int {
            val nextIndex = offset + 1
            if (nextIndex < s.length) {
                when (s[nextIndex]) {
                    '0' -> {
                        sb.append('~')
                        return 2
                    }
                    '1' -> {
                        sb.append('/')
                        return 2
                    }
                }
            }
            sb.append('~')
            return 1
        }

    }

}
