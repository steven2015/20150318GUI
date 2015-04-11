/**
 *
 */
package steven.http.sqlplus;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import steven.http.handler.FileRequestHandler;

import com.google.gson.Gson;

/**
 * @author Steven
 *
 */
public enum SqlplusHandler implements HttpRequestHandler{
	INSTANCE;
	private static final String PAGE = "/sqlplus.html";
	private final Gson gson = new Gson();

	@Override
	public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context) throws HttpException, IOException{
		final String uri = request.getRequestLine().getUri();
		if(uri.equals("/sqlplus")){
			FileRequestHandler.INSTANCE.serveFile(request, response, context, SqlplusHandler.PAGE);
		}else if(uri.equals("/sqlplus/ajax")){
			final HttpEntity entity = ((HttpEntityEnclosingRequest)request).getEntity();
			final ContentType contentType = ContentType.get(entity);
			Charset charset = null;
			if(contentType != null){
				charset = contentType.getCharset();
			}
			if(charset == null){
				charset = Charset.forName("UTF8");
			}
			final SqlplusJsonRequest requestJson;
			final SqlplusJsonResponse responseJson = new SqlplusJsonResponse();
			try(final InputStreamReader isr = new InputStreamReader(entity.getContent(), charset);){
				requestJson = this.gson.fromJson(isr, SqlplusJsonRequest.class);
			}
			if(requestJson.getType() == null){
				response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
				response.setEntity(null);
			}else{
				switch(requestJson.getType()){
					case INITIALIZE:
						responseJson.setPrompt("sys_iv@dev>");
						break;
					default:
						break;
				}
				response.setStatusCode(HttpStatus.SC_OK);
				response.setEntity(new StringEntity(this.gson.toJson(responseJson), ContentType.APPLICATION_JSON));
			}
		}else{
			response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
			response.setEntity(null);
		}
	}
}
