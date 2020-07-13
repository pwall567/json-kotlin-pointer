/*
 * @(#) JSONPointerRemoveTest.kt
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

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.expect

import net.pwall.json.JSONArray
import net.pwall.json.JSONObject

class JSONPointerRemoveTest {

    @Test fun `should remove property from object`() {
        val json = JSONObject().apply {
            putValue("a", 123)
            putValue("b", 456)
        }
        val pointer = JSONPointer("/b")
        val expected = JSONObject().apply {
            putValue("a", 123)
        }
        expect(expected) { pointer.remove(json) }
        expect(2) { json.size } // check that the original was not modified
    }

    @Test fun `should remove property from nested object`() {
        val json1 = JSONObject().apply {
            putValue("a", 123)
            putValue("b", 456)
        }
        val json2 = JSONObject().apply {
            putJSON("q", json1)
        }
        val pointer = JSONPointer("/q/b")
        val expected1 = JSONObject().apply {
            putValue("a", 123)
        }
        val expected2 = JSONObject().apply {
            putJSON("q", expected1)
        }
        val result = pointer.remove(json2)
        expect(expected2) { result }
        expect(2) { json1.size } // check that the original was not modified
        expect(2) { (json2["q"] as JSONObject).size }
    }

    @Test fun `should remove item from array`() {
        val json = JSONArray().apply {
            addValue(123)
            addValue(456)
        }
        val pointer = JSONPointer("/0")
        val expected = JSONArray().apply {
            addValue(456)
        }
        expect(expected) { pointer.remove(json) }
        expect(2) { json.size } // check that the original was not modified
    }

    @Test fun `should give error on remove with root pointer`() {
        val json = JSONObject().apply {
            putValue("a", 123)
        }
        val pointer = JSONPointer("")
        val errorMessage = assertFailsWith<JSONPointerException> { pointer.remove(json) }
        expect("Can't remove using root JSON Pointer") { errorMessage.message }
    }

}
