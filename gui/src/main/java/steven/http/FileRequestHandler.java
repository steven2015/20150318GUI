/**
 *
 */
package steven.http;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.FileEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpDateGenerator;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Steven
 *
 */
public enum FileRequestHandler implements HttpRequestHandler{
	INSTANCE;
	private static final Logger LOG = LogManager.getLogger();
	private static final DateTimeFormatter HTTP_DATE_FORMATTER = DateTimeFormatter.ofPattern(HttpDateGenerator.PATTERN_RFC1123, Locale.US);

	private FileRequestHandler(){
	}
	@Override
	public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context) throws HttpException, IOException{
		final String path = request.getRequestLine().getUri();
		this.serveFile(request, response, context, path);
	}
	public void serveFile(final HttpRequest request, final HttpResponse response, final HttpContext context, final String path){
		File file = null;
		try{
			final URL url = FileRequestHandler.class.getResource(path);
			if(url != null){
				file = new File(url.toURI());
			}
		}catch(final Exception e){
			FileRequestHandler.LOG.error("Cannot locate [" + path + "].", e);
			response.setStatusCode(HttpStatus.SC_NOT_FOUND);
			response.setEntity(null);
			return;
		}
		if(file != null && file.isFile() && file.canRead()){
			boolean modified = true;
			final ZonedDateTime fileZdt = Instant.ofEpochMilli(file.lastModified()).atZone(ZoneId.systemDefault());
			if(request.containsHeader(HttpHeaders.IF_MODIFIED_SINCE)){
				final ZonedDateTime requestZdt = FileRequestHandler.HTTP_DATE_FORMATTER.parse(request.getFirstHeader(HttpHeaders.IF_MODIFIED_SINCE).getValue(), ZonedDateTime::from);
				if(fileZdt.isAfter(requestZdt) == false){
					modified = false;
				}
			}
			if(modified){
				response.setStatusCode(HttpStatus.SC_OK);
				response.setHeader(HttpHeaders.LAST_MODIFIED, fileZdt.format(FileRequestHandler.HTTP_DATE_FORMATTER));
				response.setEntity(new FileEntity(file));
			}else{
				response.setStatusCode(HttpStatus.SC_NOT_MODIFIED);
				response.setEntity(null);
			}
		}else{
			response.setStatusCode(HttpStatus.SC_NOT_FOUND);
			response.setEntity(null);
		}
	}
}
