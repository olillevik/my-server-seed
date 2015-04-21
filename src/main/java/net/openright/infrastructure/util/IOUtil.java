package net.openright.infrastructure.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IOUtil {
	
	private static final Logger log = LoggerFactory.getLogger(IOUtil.class);

	public static void extractResourceFile(String filename) {
		File file = new File(filename);
		if (file.exists())
			return;

		try (InputStream input = IOUtil.class.getResourceAsStream("/" + filename)) {
			if (input == null) {
				throw new IllegalArgumentException("Can't find /" + filename + " in classpath");
			}

			copy(input, file);
		} catch (IOException e) {
			throw ExceptionUtil.soften(e);
		}
	}

	public static String toString(URL url) {
		try (InputStream content = (InputStream) url.getContent()) {
			return toString(content);
		} catch (IOException e) {
			throw ExceptionUtil.soften(e);
		}
	}

	public static String toString(InputStream content) {
		return toString(new InputStreamReader(content));
	}

	public static String toString(Reader reader) {
		char[] buffer = new char[1024];
		StringBuilder out = new StringBuilder();

		try {
			for (;;) {
				int rsz = reader.read(buffer, 0, buffer.length);
				if (rsz < 0)
					break;
				out.append(buffer, 0, rsz);
			}
			return out.toString();
		} catch (IOException e) {
			throw ExceptionUtil.soften(e);
		}
	}
	
	public static JsonObject toJson(String string) {
		log.debug("Converting string to json: " + string);
		try(JsonReader reader = Json.createReader(new StringReader(string))) {
			return reader.readObject();
		}
	}

	public static File copy(URL url, File file) {
	    if (file.isDirectory()) {
	        file = new File(file, new File(url.getPath()).getName());
	    }
		try (InputStream content = (InputStream) url.getContent()) {
			copy(content, file);
			return file;
		} catch (IOException e) {
			throw ExceptionUtil.soften(e);
		}

	}

	public static void copy(InputStream content, File file) {
		try (FileOutputStream output = new FileOutputStream(file)) {
			copy(content, output);
		} catch (IOException e) {
			throw ExceptionUtil.soften(e);
		}
	}

	public static void copy(InputStream in, OutputStream out) {
		try {
			byte[] buf = new byte[1024];
			int count = 0;
			while ((count = in.read(buf)) >= 0) {
				out.write(buf, 0, count);
			}
		} catch (IOException e) {
			throw ExceptionUtil.soften(e);
		}
	}
}
