/*
 * @(#) JSONPointerTest.kt
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

import net.pwall.json.JSON
import net.pwall.json.JSONArray
import net.pwall.json.JSONInteger
import net.pwall.json.JSONString
import net.pwall.json.JSONValue
import net.pwall.json.pointer.JSONPointer.Companion.locate


class JSONPointerTest {

    @Test fun `should give results shown in example in specification`() {
        expect(document) { JSONPointer("").eval(document) }
        expect(JSONArray(JSONString("bar"), JSONString("baz"))) { JSONPointer("/foo").eval(document) }
        expect(JSONString("bar")) { JSONPointer("/foo/0").eval(document) }
        expect(JSONInteger(0)) { JSONPointer("/").eval(document) }
        expect(JSONInteger(1)) { JSONPointer("/a~1b").eval(document) }
        expect(JSONInteger(2)) { JSONPointer("/c%d").eval(document) }
        expect(JSONInteger(3)) { JSONPointer("/e^f").eval(document) }
        expect(JSONInteger(4)) { JSONPointer("/g|h").eval(document) }
        expect(JSONInteger(5)) { JSONPointer("/i\\j").eval(document) }
        expect(JSONInteger(6)) { JSONPointer("/k\"l").eval(document) }
        expect(JSONInteger(7)) { JSONPointer("/ ").eval(document) }
        expect(JSONInteger(8)) { JSONPointer("/m~0n").eval(document) }
    }

    @Test fun `should give same results using extension function`() {
        expect(document) { document locate JSONPointer("") }
        expect(JSONArray(JSONString("bar"), JSONString("baz"))) { document locate JSONPointer("/foo") }
        expect(JSONString("bar")) { document locate JSONPointer("/foo/0") }
        expect(JSONInteger(0)) { document locate JSONPointer("/") }
        expect(JSONInteger(1)) { document locate JSONPointer("/a~1b") }
        expect(JSONInteger(2)) { document locate JSONPointer("/c%d") }
        expect(JSONInteger(3)) { document locate JSONPointer("/e^f") }
        expect(JSONInteger(4)) { document locate JSONPointer("/g|h") }
        expect(JSONInteger(5)) { document locate JSONPointer("/i\\j") }
        expect(JSONInteger(6)) { document locate JSONPointer("/k\"l") }
        expect(JSONInteger(7)) { document locate JSONPointer("/ ") }
        expect(JSONInteger(8)) { document locate JSONPointer("/m~0n") }
    }

    @Test fun `should give same results using extension function with string`() {
        expect(document) { document locate "" }
        expect(JSONArray(JSONString("bar"), JSONString("baz"))) { document locate "/foo" }
        expect(JSONString("bar")) { document locate "/foo/0" }
        expect(JSONInteger(0)) { document locate "/" }
        expect(JSONInteger(1)) { document locate "/a~1b" }
        expect(JSONInteger(2)) { document locate "/c%d" }
        expect(JSONInteger(3)) { document locate "/e^f" }
        expect(JSONInteger(4)) { document locate "/g|h" }
        expect(JSONInteger(5)) { document locate "/i\\j" }
        expect(JSONInteger(6)) { document locate "/k\"l" }
        expect(JSONInteger(7)) { document locate "/ " }
        expect(JSONInteger(8)) { document locate "/m~0n" }
    }

    @Test fun `should escape correctly on toString`() {
        expect("") { JSONPointer("").toString() }
        expect("/foo") { JSONPointer("/foo").toString() }
        expect("/foo/0") { JSONPointer("/foo/0").toString() }
        expect("/") { JSONPointer("/").toString() }
        expect("/a~1b") { JSONPointer("/a~1b").toString() }
        expect("/c%d") { JSONPointer("/c%d").toString() }
        expect("/e^f") { JSONPointer("/e^f").toString() }
        expect("/g|h") { JSONPointer("/g|h").toString() }
        expect("/i\\j") { JSONPointer("/i\\j").toString() }
        expect("/k\"l") { JSONPointer("/k\"l").toString() }
        expect("/ ") { JSONPointer("/ ").toString() }
        expect("/m~0n") { JSONPointer("/m~0n").toString() }
    }

    @Test fun `should create correct URI fragment`() {
        expect("#") { JSONPointer("").toURIFragment() }
        expect("#/foo") { JSONPointer("/foo").toURIFragment() }
        expect("#/foo/0") { JSONPointer("/foo/0").toURIFragment() }
        expect("#/") { JSONPointer("/").toURIFragment() }
        expect("#/a~1b") { JSONPointer("/a~1b").toURIFragment() }
        expect("#/c%25d") { JSONPointer("/c%d").toURIFragment() }
        expect("#/e%5Ef") { JSONPointer("/e^f").toURIFragment() }
        expect("#/g%7Ch") { JSONPointer("/g|h").toURIFragment() }
        expect("#/i%5Cj") { JSONPointer("/i\\j").toURIFragment() }
        expect("#/k%22l") { JSONPointer("/k\"l").toURIFragment() }
        expect("#/%20") { JSONPointer("/ ").toURIFragment() }
        expect("#/m~0n") { JSONPointer("/m~0n").toURIFragment() }
        expect("#/o%2Ap") { JSONPointer("/o*p").toURIFragment() }
        expect("#/q%2Br") { JSONPointer("/q+r").toURIFragment() }
    }

    @Test fun `should correctly decode URI fragment`() {
        expect(JSONPointer("")) { JSONPointer.fromURIFragment("#") }
        expect(JSONPointer("/foo")) { JSONPointer.fromURIFragment("#/foo") }
        expect(JSONPointer("/foo/0")) { JSONPointer.fromURIFragment("#/foo/0") }
        expect(JSONPointer("/")) { JSONPointer.fromURIFragment("#/") }
        expect(JSONPointer("/a~1b")) { JSONPointer.fromURIFragment("#/a~1b") }
        expect(JSONPointer("/c%d")) { JSONPointer.fromURIFragment("#/c%25d") }
        expect(JSONPointer("/e^f")) { JSONPointer.fromURIFragment("#/e%5Ef") }
        expect(JSONPointer("/g|h")) { JSONPointer.fromURIFragment("#/g%7Ch") }
        expect(JSONPointer("/i\\j")) { JSONPointer.fromURIFragment("#/i%5Cj") }
        expect(JSONPointer("/k\"l")) { JSONPointer.fromURIFragment("#/k%22l") }
        expect(JSONPointer("/ ")) { JSONPointer.fromURIFragment("#/%20") }
        expect(JSONPointer("/m~0n")) { JSONPointer.fromURIFragment("#/m~0n") }
        expect(JSONPointer("/o*p")) { JSONPointer.fromURIFragment("#/o%2Ap") }
        expect(JSONPointer("/q+r")) { JSONPointer.fromURIFragment("#/q%2Br") }
    }

    @Test fun `should test whether pointer exists or not`() {
        expect(true) { JSONPointer("/foo").exists(document) }
        expect(true) { JSONPointer("/foo/0").exists(document) }
        expect(true) { JSONPointer("/foo/1").exists(document) }
        expect(false) { JSONPointer("/foo/2").exists(document) }
        expect(false) { JSONPointer("/fool").exists(document) }
    }

    @Test fun `should navigate correctly to child`() {
        val basePointer = JSONPointer("")
        expect(document) { document locate basePointer }
        val childPointer1 = basePointer.child("foo")
        expect(JSONArray(JSONString("bar"), JSONString("baz"))) { document locate childPointer1 }
        val childPointer2 = childPointer1.child(0)
        expect(JSONString("bar")) { document locate childPointer2 }
    }

    @Test fun `should navigate correctly to parent`() {
        val startingPointer = JSONPointer("/foo/1")
        expect(JSONString("baz")) { document locate startingPointer }
        val parentPointer1 = startingPointer.parent()
        expect(JSONArray(JSONString("bar"), JSONString("baz"))) { document locate parentPointer1 }
        val parentPointer2 = parentPointer1.parent()
        expect(document) { document locate parentPointer2 }
    }

    @Test fun `should give correct error message on bad reference`() {
        val errorMessage = assertFailsWith<JSONPointerException> { JSONPointer("/wrong/0").eval(document) }
        expect("Can't resolve JSON Pointer /wrong") { errorMessage.message }
    }

    @Test fun `should return valid root pointer`() {
        expect(JSONPointer("")) { JSONPointer.root }
    }

    companion object {

        /**
         * The following example is copied directly from the specification document
         * [JavaScript Object Notation (JSON) Pointer](https://tools.ietf.org/html/rfc6901).
         */
        val document: JSONValue = JSON.parse("""
{
  "foo": ["bar", "baz"],
  "": 0,
  "a/b": 1,
  "c%d": 2,
  "e^f": 3,
  "g|h": 4,
  "i\\j": 5,
  "k\"l": 6,
  " ": 7,
  "m~n": 8
}
""")
    }

}
