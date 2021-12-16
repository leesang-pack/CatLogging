package com.catlogging.event.es;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.catlogging.model.LogPointer;

/**
 * Serializes {@link LogPointer}s JSON data as string value instead of complex
 * JSON object to simplify mapping in ES.
 * 
 * @author Tester
 *
 */
public class EsLogPointerSerializer extends JsonSerializer<LogPointer> {

	@Override
	public void serialize(final LogPointer value, final JsonGenerator gen, final SerializerProvider serializers)
			throws IOException, JsonProcessingException {
		gen.writeStartObject();
		gen.writeStringField("_json", value.getJson());
		gen.writeEndObject();
	}
}
