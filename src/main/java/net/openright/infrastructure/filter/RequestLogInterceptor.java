package net.openright.infrastructure.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs body of incoming requests
 */
@Provider
@PreMatching
public class RequestLogInterceptor implements ReaderInterceptor {

	private static final Logger log = LoggerFactory.getLogger(RequestLogInterceptor.class);

	@Override
	public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
		if (log.isDebugEnabled()) {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			TeeInputStream inputStream = new TeeInputStream(context.getInputStream(), outputStream);
			String requestBody = IOUtils.toString(inputStream);
			IOUtils.closeQuietly(inputStream);
			log.debug(requestBody);
			InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
			context.setInputStream(is);
		}
		return context.proceed();
	}
}
