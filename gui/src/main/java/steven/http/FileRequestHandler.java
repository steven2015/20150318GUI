/**
 *
 */
package steven.http;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
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

/**
 * @author Steven
 *
 */
public class FileRequestHandler implements HttpRequestHandler{
	private static final DateTimeFormatter HTTP_DATE_FORMATTER = DateTimeFormatter.ofPattern(HttpDateGenerator.PATTERN_RFC1123, Locale.US);
	private final String welcomeFilePath;

	public FileRequestHandler(String welcomeFilePath){
		this.welcomeFilePath = welcomeFilePath;
	}
	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException{
		String path = request.getRequestLine().getUri();
		if("/".equals(path)){
			path = welcomeFilePath;
		}
		File file = null;
		try{
			file = new File(FileRequestHandler.class.getResource(path).toURI());
		}catch(URISyntaxException e){
			response.setStatusCode(HttpStatus.SC_NOT_FOUND);
			response.setEntity(null);
		}
		if(file.isFile() && file.canRead()){
			boolean modified = true;
			final ZonedDateTime fileZdt = Instant.ofEpochMilli(file.lastModified()).atZone(ZoneId.systemDefault());
			if(request.containsHeader(HttpHeaders.IF_MODIFIED_SINCE)){
				final ZonedDateTime requestZdt = HTTP_DATE_FORMATTER.parse(request.getFirstHeader(HttpHeaders.IF_MODIFIED_SINCE).getValue(), ZonedDateTime::from);
				if(fileZdt.isAfter(requestZdt) == false){
					modified = false;
				}
			}
			if(modified){
				response.setStatusCode(HttpStatus.SC_OK);
				response.setHeader(HttpHeaders.LAST_MODIFIED, fileZdt.format(HTTP_DATE_FORMATTER));
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
