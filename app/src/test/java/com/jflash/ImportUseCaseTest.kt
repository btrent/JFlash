package com.jflash

import com.jflash.data.model.*
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class ImportUseCaseTest {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    @Test
    fun `parse valid import JSON`() {
        val jsonString = """
        {
            "meta": {
                "version": "1.0"
            },
            "lists": [
                {
                    "id": "test-uuid",
                    "title": "Test List",
                    "updatedAt": "2024-01-01T00:00:00Z",
                    "entries": [
                        {
                            "ref": 1234,
                            "updatedAt": "2024-01-01T00:00:00Z"
                        },
                        {
                            "ref": 5678,
                            "updatedAt": "2024-01-01T00:00:00Z"
                        }
                    ]
                }
            ]
        }
        """.trimIndent()
        
        val importData = json.decodeFromString<ImportData>(jsonString)
        
        assertEquals("1.0", importData.meta.version)
        assertEquals(1, importData.lists.size)
        assertEquals("Test List", importData.lists[0].title)
        assertEquals(2, importData.lists[0].entries.size)
        assertEquals(1234L, importData.lists[0].entries[0].ref)
    }
    
    @Test
    fun `parse JSON with missing meta version`() {
        val jsonString = """
        {
            "meta": {},
            "lists": [
                {
                    "id": "test-uuid",
                    "title": "Test List",
                    "updatedAt": "2024-01-01T00:00:00Z",
                    "entries": []
                }
            ]
        }
        """.trimIndent()
        
        val importData = json.decodeFromString<ImportData>(jsonString)
        
        assertNull(importData.meta.version)
        assertEquals(1, importData.lists.size)
    }
    
    @Test
    fun `parse JSON with empty lists`() {
        val jsonString = """
        {
            "meta": {
                "version": "1.0"
            },
            "lists": []
        }
        """.trimIndent()
        
        val importData = json.decodeFromString<ImportData>(jsonString)
        
        assertEquals(0, importData.lists.size)
    }
}