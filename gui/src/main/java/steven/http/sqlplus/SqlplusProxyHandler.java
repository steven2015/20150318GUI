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

import steven.http.Session;
import steven.http.WebClientContext;
import steven.http.handler.FileRequestHandler;
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
		final Session session = ((WebClientContext)context).getSession();
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
			if("@POLL".equals(line)){
				synchronized(session){
					final Thread thread = (Thread)session.getAttribute("POLL_THREAD");
					if(thread != null){
						thread.interrupt();
					}
					session.setAttribute("POLL_THREAD", Thread.currentThread());
				}
				String output = null;
				do{
					output = client.readOutput();
					try{
						Thread.sleep(100);
					}catch(final InterruptedException e){
						break;
					}
				}while(output.length() == 0);
				synchronized(session){
					final Thread thread = (Thread)session.getAttribute("POLL_THREAD");
					if(thread == Thread.currentThread()){
						session.setAttribute("POLL_THREAD", null);
					}
				}
				response.setStatusCode(HttpStatus.SC_OK);
				response.setEntity(new StringEntity(output, ContentType.TEXT_PLAIN));
			}else if("@RESTART".equals(line)){
				client.close();
				client = new SqlplusProxyClient();
				session.setAttribute("client", client);
			}else{
				client.send(line, true);
				response.setStatusCode(HttpStatus.SC_OK);
				response.setEntity(new StringEntity("", ContentType.TEXT_PLAIN));
			}
		}else{
			response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
			response.setEntity(null);
		}
	}
}
