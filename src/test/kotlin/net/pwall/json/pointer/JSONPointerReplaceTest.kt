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
