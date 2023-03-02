/*******************************************************************************
 * catlogging, open source tool for viewing, monitoring and analysing log data.
 * Copyright (c) 2021 xzpluszone, www.catlogging.com
 *
 * catlogging is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * catlogging is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.catlogging.web.tags;

import com.catlogging.app.ContextProvider;
import com.catlogging.aspect.AspectHost;
import com.catlogging.model.RollingLog;
import com.catlogging.web.wizard.WizardInfo;
import com.catlogging.web.controller.wizard.WizardInfoController;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.BeansException;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.*;

/**
 * JSP functions library.
 * 
 * @author Tester
 * 
 */
@Slf4j
@Component
public class JstlFunctionsLibraryBean {
	private static ObjectMapper jsonMapper = new ObjectMapper();

	public boolean isRollingLog(final Object log) {
		return log instanceof RollingLog;
	}

	public  Object getAspect(final AspectHost host, final String aspectKey) {
		return host.getAspect(aspectKey, Object.class);
	}

	/**
	 * Convert number of bytes into human readable format from:
	 * http://codeaid.net
	 * /javascript/convert-size-in-bytes-to-human-readable-format-(javascript)
	 * 
	 * @param integer
	 *            bytes Number of bytes to convert
	 * @param integer
	 *            precision Number of digits after the decimal separator
	 * @return string
	 */
	public  String bytesToSize(final long bytes, final int precision) {
		final double kilobyte = 1024;
		final double megabyte = kilobyte * 1024;
		final double gigabyte = megabyte * 1024;
		final double terabyte = gigabyte * 1024;

		if (bytes >= 0 && bytes < kilobyte) {
			return bytes + " B";

		} else if (bytes >= kilobyte && bytes < megabyte) {
			return round(bytes / kilobyte, precision) + " KB";

		} else if (bytes >= megabyte && bytes < gigabyte) {
			return round(bytes / megabyte, precision) + " MB";

		} else if (bytes >= gigabyte && bytes < terabyte) {
			return round(bytes / gigabyte, precision) + " GB";

		} else if (bytes >= terabyte) {
			return round(bytes / terabyte, precision) + " TB";

		} else {
			return bytes + " B";
		}
	}

	/**
	 * Rounds given floating point value taking the precision into account.
	 * 
	 * @param value
	 * @param precision
	 * @return rounded value
	 */
	public  double round(final double value, final int precision) {
		return new BigDecimal(value).setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * Converts a timestamp to {@link Date} object.
	 * 
	 * @param value
	 *            timestamp in milliseconds
	 * @return timestamp as {@link Date} object
	 */
	public  Date getTimestampAsDate(final long value) {
		return new Date(value);
	}

	/**
	 * Transforms an object to JSON string representation.
	 * 
	 * @param object
	 *            arbitrary object to transform
	 * @return JSOn string or null in case of errors
	 */
	public  String jsonify(final Object object) {
		try {
			return jsonMapper.writeValueAsString(object);
		} catch (final Exception e) {
			log.error("JSON mapper error", e);
			return null;
		}
	}

	/**
	 * Encodes a string with base64.
	 * 
	 * @param strToEncode
	 *            string to encode
	 * @return encoded string
	 * @throws UnsupportedEncodingException
	 */
	public  String btoa(final String strToEncode) throws UnsupportedEncodingException {
		return Base64.encodeBase64String(strToEncode.getBytes("UTF-8"));
	}

	/**
	 * Parses a JSON string and returns it as a {@link JSONObject}
	 * representation.
	 * 
	 * @param jsonStr
	 *            JSON string
	 * @return a {@link JSONObject} representation
	 */
	public  JSONObject jsonObject(final String jsonStr) {
		try {
			return JSONObject.fromObject(jsonStr);
		} catch (final RuntimeException e) {
			log.error("Failed to parse JSON: " + jsonStr, e);
			return new JSONObject();
		}
	}

	/**
	 * Returns wizards info related to a bean type.
	 * 
	 * @param beanType
	 *            bean type
	 * @param locale
	 *            current locale
	 * @return wizards info related to a bean type
	 * @throws ClassNotFoundException
	 * @throws BeansException
	 */
	public  List<WizardInfo> getWizardsInfo(final String beanTypeStr, final Locale locale) throws BeansException, ClassNotFoundException {
		return ContextProvider.getContext().getBean(WizardInfoController.class).getWizardsInfo(beanTypeStr, locale);
	}

	/**
	 * Merges to lists into one.
	 * 
	 * @param col1
	 * @param col2
	 * @return
	 */
	public  List<Object> mergeLists(final List<Object> col1, final List<Object> col2) {
		final List<Object> m = new ArrayList<>();
		m.addAll(col1);
		m.addAll(col2);
		return m;
	}

	/**
	 * Returns true if value is contained in given collection.
	 * 
	 * @param collection
	 * @param value
	 * @return true if value is contained in given collection
	 */
	public  boolean contains(final Collection<Object> list, final Object value) {
		return list != null && value != null && list.contains(value);
	}
}
