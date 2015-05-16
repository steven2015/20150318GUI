/**
 *
 */
package steven.sqlplus.net;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;

import steven.jetty.WebSocket;
import steven.sqlplus.SqlplusCallback;
import steven.sqlplus.SqlplusClient;
import steven.sqlplus.SqlplusResponse;

import com.google.gson.Gson;

/**
 * @author Steven
 *
 */
public class SqlplusWebSocket extends WebSocket implements SqlplusCallback{
	private static final Logger LOG = LogManager.getLogger();
	private static final Gson GSON = new Gson();
	private final SqlplusClient client = new SqlplusClient(this);

	public SqlplusWebSocket(){
		super(-1);
	}
	@Override
	public void onWebSocketConnect(final Session session){
		super.onWebSocketConnect(session);
	}
	@Override
	public void onWebSocketClose(final int statusCode, final String reason){
		super.onWebSocketClose(statusCode, reason);
		this.client.close();
	}
	@Override
	public void onWebSocketText(final String message){
		this.client.handleRequest(SqlplusWebSocket.GSON.fromJson(message, JsonRequest.class).to());
	}
	@Override
	public void handleResponse(final SqlplusResponse response){
		try{
			super.sendText(SqlplusWebSocket.GSON.toJson(JsonResponse.from(response)));
		}catch(final IOException e){
			SqlplusWebSocket.LOG.error("Cannot send text.", e);
		}
	}
}
