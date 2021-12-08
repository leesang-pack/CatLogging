package com.catlogging.model;

import com.catlogging.app.CoreAppConfig;
import com.catlogging.model.support.DefaultPointer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test for serializing / deserializing {@link LogEntry}s.
 * 
 * @author Tester
 *
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CoreAppConfig.class })
@Configuration
public class LogEntryJsonTest {
	@Autowired
	private ObjectMapper mapper;

	@Test
	public void testEmptySerialization() throws Exception {
		final LogEntry entry = new LogEntry();
		final String entryStr = mapper.writeValueAsString(entry);
		log.debug("JSON: {}", entryStr);

		final LogEntry check = mapper.readValue(entryStr, LogEntry.class);
		Assert.assertTrue(check.isEmpty());
	}

	@Test
	public void testPointerSerialization() throws Exception {
		final LogEntry entry = new LogEntry();
		entry.setStartOffset(new DefaultPointer(123, 500));
		entry.setEndOffset(new DefaultPointer(499, 500));
		Assert.assertEquals(2, entry.size());
		final String entryStr = mapper.writeValueAsString(entry);
		log.debug("JSON with pointers: {}", entryStr);

		final LogEntry check = mapper.readValue(entryStr, LogEntry.class);
		Assert.assertEquals(2, check.size());
		Assert.assertEquals(JSONObject.fromObject(entry.getStartOffset().getJson()),
				JSONObject.fromObject(check.getStartOffset().getJson()));
		Assert.assertEquals(JSONObject.fromObject(entry.getEndOffset().getJson()),
				JSONObject.fromObject(check.getEndOffset().getJson()));
	}
}
