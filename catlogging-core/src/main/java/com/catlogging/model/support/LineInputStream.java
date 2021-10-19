/*******************************************************************************
 * catlogging, open source tool for viewing, monitoring and analysing log data.

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
package com.catlogging.model.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.catlogging.model.LogPointer;

/**
 * Buffered line log input stream. Inspired from
 * http://www.javaworld.com/javaworld/javatips/jw-javatip26.html.
 * 
 * @author Tester
 * 
 */
public class LineInputStream extends ByteLogInputStream {
	private final ByteLogInputStream stream;
	private final ByteLogAccess pointerFactory;
	private final byte buffer[];
	private int buf_end = 0;
	private int buf_pos = 0;
	private LogPointer real_pos;
	private final String charset;

	public LineInputStream(final ByteLogAccess logAccess, final ByteLogInputStream stream, final String charset)
			throws IOException {
		this.pointerFactory = logAccess;
		this.stream = stream;
		invalidate();
		this.buffer = new byte[4096 * 4];
		this.charset = charset;
	}

	@Override
	public final int read() throws IOException {
		if (buf_pos >= buf_end) {
			if (fillBuffer() < 0) {
				return -1;
			}
		}
		if (buf_end == 0) {
			return -1;
		} else {
			return buffer[buf_pos++];
		}
	}

	private int fillBuffer() throws IOException {
		final int n = stream.read(buffer, 0, buffer.length);
		if (n >= 0) {
			real_pos = pointerFactory.createRelative(real_pos, n);
			buf_end = n;
			buf_pos = 0;
		}
		return n;
	}

	private void invalidate() throws IOException {
		buf_end = 0;
		buf_pos = 0;
		real_pos = stream.getPointer();
	}

	@Override
	public int read(final byte b[], final int off, final int len) throws IOException {
		final int leftover = buf_end - buf_pos;
		if (len <= leftover) {
			System.arraycopy(buffer, buf_pos, b, off, len);
			buf_pos += len;
			return len;
		}
		for (int i = 0; i < len; i++) {
			final int c = this.read();
			if (c != -1) {
				b[off + i] = (byte) c;
			} else {
				return i;
			}
		}
		return len;
	}

	@Override
	public LogPointer getPointer() throws IOException {
		return pointerFactory.createRelative(real_pos, -buf_end + buf_pos);
	}

	/**
	 * return a next line in String
	 */
	public final String readNextLine() throws IOException {
		String str = null;
		if (buf_end - buf_pos <= 0) {
			if (fillBuffer() < 0) {
				return null;
			}
		}
		int lineend = -1;
		for (int i = buf_pos; i < buf_end; i++) {
			if (buffer[i] == '\n') {
				lineend = i;
				break;
			}
		}
		if (lineend < 0) {
			final ByteArrayOutputStream input = new ByteArrayOutputStream(512);
			input.write(buffer, buf_pos, buf_end - buf_pos);
			buf_pos = buf_end;
			int c;
			while ((c = read()) != -1 && c != '\n') {
				input.write((char) c);
			}
			if (c == -1 && input.size() == 0) {
				return null;
			}
			final byte[] bytesInput = input.toByteArray();
			if (c == '\n' && bytesInput.length > 0 && bytesInput[bytesInput.length - 1] == '\r') {
				return new String(bytesInput, 0, bytesInput.length - 1, charset);
			} else {
				return new String(bytesInput, charset);
			}
		}
		if (lineend > 0 && buffer[lineend - 1] == '\r') {
			str = new String(buffer, buf_pos, lineend - buf_pos - 1, charset);
		} else {
			str = new String(buffer, buf_pos, lineend - buf_pos, charset);
		}
		buf_pos = lineend + 1;
		return str;
	}

	@Override
	public void close() throws IOException {
		stream.close();
	}

}
