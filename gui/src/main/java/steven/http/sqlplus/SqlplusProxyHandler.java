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

import steven.http.FileRequestHandler;
import steven.http.Session;
import steven.sqlplus.SqlplusProxyClient;

/**
 * @author Steven
 *
 */
public enum SqlplusProxyHandler implements HttpRequestHandler{
	INSTANCE;
	private static final String PAGE = "/sqlplusproxy.html";

	@Override
	public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context) throws HttpException, IOException{
		final String uri = request.getRequestLine().getUri();
		if(uri.equals("/sqlplusproxy")){
			FileRequestHandler.INSTANCE.serveFile(request, response, context, SqlplusProxyHandler.PAGE);
		}else if(uri.equals("/sqlplusproxy/ajax")){
			final HttpEntity entity = ((HttpEntityEnclosingRequest)request).getEntity();
			final ContentType contentType = ContentType.get(entity);
			Charset charset = null;
			if(contentType != null){
				charset = contentType.getCharset();
			}
			if(charset == null){
				charset = Charset.forName("UTF8");
			}
			final Session session = (Session)context.getAttribute(Session.COOKIE_NAME);
			SqlplusProxyClient client = (SqlplusProxyClient)session.getAttribute("client");
			if(client == null){
				client = new SqlplusProxyClient();
				session.setAttribute("client", client);
			}
			final String line;
			try(final InputStreamReader isr = new InputStreamReader(entity.getContent(), charset);){
				final StringBuilder sb = new StringBuilder();
				final char[] buffer = new char[4096];
				int size = 0;
				while((size = isr.read(buffer)) >= 0){
					sb.append(buffer, 0, size);
				}
				line = sb.toString();
			}
			if("@POLL".equals(line) == false){
				client.send(line, true);
			}
			String output = null;
			do{
				output = client.readOutput();
				try{
					Thread.sleep(100);
				}catch(final InterruptedException e){
				}
			}while(output.length() == 0);
			response.setStatusCode(HttpStatus.SC_OK);
			response.setEntity(new StringEntity(output, ContentType.TEXT_PLAIN));
		}else{
			response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
			response.setEntity(null);
		}
	}
}
