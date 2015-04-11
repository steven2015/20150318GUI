/**
 *
 */
package steven.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import steven.http.handler.FileRequestHandler;
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
	public Session getSession(final String sessionId, final boolean force){
		synchronized(this.sessionsLock){
			Session session = this.sessions.get(sessionId);
			if(session == null && force){
				String newSessionId = null;
				do{
					newSessionId = this.get8BytesHex() + this.get8BytesHex();
				}while(this.sessions.containsKey(newSessionId));
				session = new Session(newSessionId);
				this.sessions.put(newSessionId, session);
				WebClient.LOG.info("Session created [" + newSessionId + "].");
			}
			return session;
		}
	}
	private String get8BytesHex(){
		final String s = "00000000" + Integer.toHexString((int)(Math.random() * Integer.MAX_VALUE));
		return s.substring(s.length() - 8, s.length()).toUpperCase();
	}
	public static final void main(final String[] args) throws IOException{
		final HttpServer httpServer = new HttpServer(80, () -> new WebClientContext());
		httpServer.setRequestHandler((request, response, context) -> {
			try{
				final WebClientContext myContext = (WebClientContext)context;
				myContext.loadCookies(request);
				// session
				Session session = WebClient.INSTANCE.getSession(myContext.getCookie(Session.COOKIE_NAME), false);
				if(session == null){
					session = WebClient.INSTANCE.getSession(null, true);
					response.addHeader("Set-Cookie", Session.COOKIE_NAME + "=" + session.getSessionId());
				}
				myContext.setSession(session);
				ThreadContext.put("sessionId", session.getSessionId());
				final String uri = request.getRequestLine().getUri();
				WebClient.LOG.info(uri);
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
				WebClient.LOG.error("Internal Server Error.", e);
				response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				response.setEntity(null);
			}
		});
		httpServer.start();
	}
}
