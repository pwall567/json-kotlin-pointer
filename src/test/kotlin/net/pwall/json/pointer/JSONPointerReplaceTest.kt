/*
 * @(#) JSONPointerReplaceTest.kt
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

import net.pwall.json.JSONInteger
import net.pwall.json.JSONObject
import net.pwall.json.pointer.JSONPointer.Companion.locate
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.expect

class JSONPointerReplaceTest {

    @Test fun `should replace property in object`() {
        val json = JSONObject().apply {
            putValue("a", 123)
            putValue("b", 456)
        }
        val pointer = JSONPointer("/b")
        val expected = JSONObject().apply {
            putValue("a", 123)
            putValue("b", 789)
        }
        val result = pointer.replace(json, JSONInteger(789))
        expect(expected) { result }
        expect(JSONInteger(789)) { result locate pointer }
        expect(JSONInteger(456)) { json["b"] } // check that the original was not modified
    }

    @Test fun `should give error on replace with root pointer`() {
        val json = JSONObject().apply {
            putValue("a", 123)
        }
        val pointer = JSONPointer("")
        val errorMessage = assertFailsWith<JSONPointerException> { pointer.replace(json, JSONInteger(123)) }
        expect("Can't replace using root JSON Pointer") { errorMessage.message }
    }

}
