package net.pwall.json.pointer

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.expect

import net.pwall.json.JSONArray
import net.pwall.json.JSONInteger
import net.pwall.json.JSONObject
import net.pwall.json.pointer.JSONPointer.Companion.locate

class JSONPointerInsertTest {

    @Test fun `should insert new property into object`() {
        val json = JSONObject().apply {
            putValue("a", 123)
        }
        val pointer = JSONPointer("/b")
        val expected = JSONObject().apply {
            putValue("a", 123)
            putValue("b", 456)
        }
        val result = pointer.insert(json, JSONInteger(456))
        expect(expected) { result }
        expect(JSONInteger(456)) { result locate pointer }
        expect(1) { json.size } // check that the original was not modified
    }

    @Test fun `should insert new property into nested object`() {
        val json1 = JSONObject().apply {
            putValue("a", 123)
        }
        val json2 = JSONObject().apply {
            putJSON("q", json1)
        }
        val pointer = JSONPointer("/q/b")
        val expected1 = JSONObject().apply {
            putValue("a", 123)
            putValue("b", 456)
        }
        val expected2 = JSONObject().apply {
            putJSON("q", expected1)
        }
        val result = pointer.insert(json2, JSONInteger(456))
        expect(expected2) { result }
        expect(JSONInteger(456)) { result locate pointer }
        expect(1) { json1.size } // check that the original was not modified
        expect(1) { (json2["q"] as JSONObject).size }
    }

    @Test fun `should insert new item into array`() {
        val json = JSONArray().apply {
            addValue(123)
        }
        val pointer = JSONPointer("/0")
        val expected = JSONArray().apply {
            addValue(456)
            addValue(123)
        }
        val result = pointer.insert(json, JSONInteger(456))
        expect(expected) { result }
        expect(JSONInteger(456)) { result locate pointer }
        expect(1) { json.size } // check that the original was not modified
    }

    @Test fun `should insert new item at end of array`() {
        val json = JSONArray().apply {
            addValue(123)
        }
        val pointer = JSONPointer("/-")
        val expected = JSONArray().apply {
            addValue(123)
            addValue(456)
        }
        val result = pointer.insert(json, JSONInteger(456))
        expect(expected) { result }
        expect(JSONInteger(456)) { result locate JSONPointer("/1") }
        expect(1) { json.size } // check that the original was not modified
    }

    @Test fun `should insert new property into array nested within object`() {
        val json1 = JSONArray().apply {
            addValue(123)
        }
        val json2 = JSONObject().apply {
            putJSON("q", json1)
        }
        val pointer = JSONPointer("/q/0")
        val expected1 = JSONArray().apply {
            addValue(456)
            addValue(123)
        }
        val expected2 = JSONObject().apply {
            putJSON("q", expected1)
        }
        val result = pointer.insert(json2, JSONInteger(456))
        expect(expected2) { result }
        expect(JSONInteger(456)) { result locate pointer }
        expect(1) { json1.size } // check that the original was not modified
        expect(1) { (json2["q"] as JSONArray).size }
    }

    @Test fun `should insert new property into array nested within array`() {
        val json1 = JSONArray().apply {
            addValue(123)
        }
        val json2 = JSONArray().apply {
            addJSON(json1)
        }
        val pointer = JSONPointer("/0/0")
        val expected1 = JSONArray().apply {
            addValue(456)
            addValue(123)
        }
        val expected2 = JSONArray().apply {
            addJSON(expected1)
        }
        val result = pointer.insert(json2, JSONInteger(456))
        expect(expected2) { result }
        expect(JSONInteger(456)) { result locate pointer }
        expect(1) { json1.size } // check that the original was not modified
        expect(1) { (json2[0] as JSONArray).size }
    }

    @Test fun `should give error on insert with root pointer`() {
        val json = JSONObject().apply {
            putValue("a", 123)
        }
        val pointer = JSONPointer("")
        val errorMessage = assertFailsWith<JSONPointerException> { pointer.insert(json, JSONInteger(123)) }
        expect("Can't insert using root JSON Pointer") { errorMessage.message }
    }

    @Test fun `should give error on insert with end-of-array as intermediate pointer`() {
        val json1 = JSONArray().apply {
            addValue(123)
        }
        val json2 = JSONArray().apply {
            addJSON(json1)
        }
        val json3 = JSONArray().apply {
            addJSON(json2)
        }
        val pointer = JSONPointer("/0/-/0")
        val errorMessage = assertFailsWith<JSONPointerException> { pointer.insert(json3, JSONInteger(123) )}
        expect("Can't dereference end-of-array JSON Pointer /0/-") { errorMessage.message }
    }

    @Test fun `should give error on insert with non-numeric array index`() {
        val json = JSONArray().apply {
            addValue(123)
        }
        val pointer = JSONPointer("/abc")
        val errorMessage = assertFailsWith<JSONPointerException> { pointer.insert(json, JSONInteger(123) )}
        expect("Illegal array index in JSON Pointer /abc") { errorMessage.message }
    }

    @Test fun `should give error on insert with out-of-range array index`() {
        val json = JSONArray().apply {
            addValue(123)
        }
        val pointer = JSONPointer("/9")
        val errorMessage = assertFailsWith<JSONPointerException> { pointer.insert(json, JSONInteger(123) )}
        expect("Array index out of range in JSON Pointer /9") { errorMessage.message }
    }

    @Test fun `should give error on insert with non-numeric array index as intermediate pointer`() {
        val json1 = JSONArray().apply {
            addValue(123)
        }
        val json2 = JSONArray().apply {
            addJSON(json1)
        }
        val json3 = JSONArray().apply {
            addJSON(json2)
        }
        val pointer = JSONPointer("/0/abc/0")
        val errorMessage = assertFailsWith<JSONPointerException> { pointer.insert(json3, JSONInteger(123) )}
        expect("Illegal array index in JSON Pointer /0/abc") { errorMessage.message }
    }

    @Test fun `should give error on insert with out-of-range array index as intermediate pointer`() {
        val json1 = JSONArray().apply {
            addValue(123)
        }
        val json2 = JSONArray().apply {
            addJSON(json1)
        }
        val json3 = JSONArray().apply {
            addJSON(json2)
        }
        val pointer = JSONPointer("/0/9/0")
        val errorMessage = assertFailsWith<JSONPointerException> { pointer.insert(json3, JSONInteger(123) )}
        expect("Array index out of range in JSON Pointer /0/9") { errorMessage.message }
    }

}
