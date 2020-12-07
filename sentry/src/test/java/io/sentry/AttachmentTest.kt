package io.sentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AttachmentTest {

    private class Fixture {
        val defaultContentType = "application/octet-stream"
        val contentType = "application/json"
        val filename = "logs.txt"
        val bytes = "content".toByteArray()
        val pathname = "path/to/$filename"
    }

    private val fixture = Fixture()

    @Test
    fun `init with bytes sets default content type`() {
        val attachment = Attachment(fixture.bytes, fixture.filename)

        assertEquals(fixture.bytes, attachment.bytes)
        assertNull(attachment.pathname)
        assertEquals(fixture.filename, attachment.filename)
        assertEquals(fixture.defaultContentType, attachment.contentType)
    }

    @Test
    fun `init with file with pathname`() {
        val attachment = Attachment(fixture.pathname)

        assertEquals(fixture.pathname, attachment.pathname)
        assertNull(attachment.bytes)
        assertEquals(fixture.filename, attachment.filename)
        assertEquals(fixture.defaultContentType, attachment.contentType)
    }

    @Test
    fun `init with file with empty pathname`() {
        val attachment = Attachment("")

        assertEquals("", attachment.pathname)
        assertEquals("", attachment.filename)
    }

    @Test
    fun `init with file with filename as pathname`() {
        val attachment = Attachment(fixture.filename)

        assertEquals(fixture.filename, attachment.pathname)
        assertEquals(fixture.filename, attachment.filename)
    }

    @Test
    fun `init with file with pathname and filename`() {
        val otherFileName = "input.json"
        val attachment = Attachment(fixture.pathname, otherFileName)

        assertEquals(fixture.pathname, attachment.pathname)
        assertNull(attachment.bytes)
        assertEquals(otherFileName, attachment.filename)
        assertEquals(fixture.defaultContentType, attachment.contentType)
    }

    @Test
    fun `set content type`() {
        val attachment = Attachment(fixture.pathname)
        attachment.contentType = fixture.contentType
        assertEquals(fixture.contentType, attachment.contentType)
    }
}
