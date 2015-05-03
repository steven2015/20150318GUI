/**
 *
 */
package steven.jetty;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

/**
 * @author Steven
 *
 */
public class WebSocket implements WebSocketListener{
	private final long idleTimeout;
	private volatile Session session;

	public WebSocket(final long idleTimeout){
		this.idleTimeout = idleTimeout;
	}
	@Override
	public void onWebSocketBinary(final byte[] payload, final int offset, final int len){
	}
	@Override
	public void onWebSocketClose(final int statusCode, final String reason){
		this.session = null;
	}
	@Override
	public void onWebSocketConnect(final Session session){
		this.session = session;
		session.setIdleTimeout(this.idleTimeout);
	}
	@Override
	public void onWebSocketError(final Throwable cause){
	}
	@Override
	public void onWebSocketText(final String message){
	}
	public void sendText(final String message) throws IOException{
		this.session.getRemote().sendString(message);
	}
	public final Session getSession(){
		return this.session;
	}
}
