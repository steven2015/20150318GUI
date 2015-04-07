/**
 * 
 */
package steven.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import steven.http.sqlplus.SqlplusHandler;
import steven.http.sqlplus.SqlplusProxyHandler;

/**
 * @author Steven
 *
 */
public enum WebClient{
	INSTANCE;
	private static final Logger LOG = LogManager.getLogger();
	private final byte[] sessionsLock = new byte[0];
	private final Map<String, Session> sessions = new HashMap<>();

	private WebClient(){
	}
	public Session getOrCreateSession(final String sessionId){
		synchronized(sessionsLock){
			Session session = sessions.get(sessionId);
			if(session == null){
				String newSessionId = null;
				do{
					newSessionId = get8BytesHex() + get8BytesHex();
				}while(sessions.containsKey(newSessionId));
				session = new Session(newSessionId);
				sessions.put(newSessionId, session);
				LOG.info("Session created [" + newSessionId + "].");
			}
			return session;
		}
	}
	private String get8BytesHex(){
		String s = "00000000" + Integer.toHexString((int)(Math.random() * Integer.MAX_VALUE));
		return s.substring(s.length() - 8, s.length()).toUpperCase();
	}
	public static final void main(final String[] args) throws IOException{
		final HttpServer httpServer = new HttpServer(80);
		httpServer.setRequestHandler(new HttpRequestHandler(){
			@Override
			public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException{
				try{
					// session
					Session session = null;
					for(Header header : request.getHeaders("Cookie")){
						for(String cookie : header.getValue().split(";")){
							int equalIndex = cookie.indexOf('=');
							String name = cookie.substring(0, equalIndex);
							if(Session.COOKIE_NAME.equals(name)){
								String value = cookie.substring(equalIndex + 1);
								session = WebClient.INSTANCE.getOrCreateSession(value);
							}
						}
					}
					if(session == null){
						session = WebClient.INSTANCE.getOrCreateSession(null);
					}
					context.setAttribute(Session.COOKIE_NAME, session);
					response.addHeader("Set-Cookie", Session.COOKIE_NAME + "=" + session.getSessionId());
					ThreadContext.put("sessionId", session.getSessionId());
					final String uri = request.getRequestLine().getUri();
					LOG.info(uri);
					if(uri.startsWith("/sqlplusproxy")){
						SqlplusProxyHandler.INSTANCE.handle(request, response, context);
					}else if(uri.startsWith("/sqlplus")){
						SqlplusHandler.INSTANCE.handle(request, response, context);
					}else{
						if(uri.equals("/")){
							FileRequestHandler.INSTANCE.serveFile(request, response, context, "/index.html");
						}else{
							FileRequestHandler.INSTANCE.handle(request, response, context);
						}
					}
				}catch(final Exception e){
					LOG.error("Internal Server Error.", e);
					response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
					response.setEntity(null);
				}
			}
		});
		httpServer.start();
	}
}
