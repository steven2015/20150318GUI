/**
 *
 */
package steven.http;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Steven
 *
 */
public class Session implements Serializable{
	private static final long serialVersionUID = -6755198933657343072L;
	public static final String COOKIE_NAME = "WSESSIONID";
	private final String sessionId;
	private final Map<String, Object> map = new HashMap<>();

	public Session(final String sessionId){
		this.sessionId = sessionId;
	}
	public void setAttribute(final String key, final Object value){
		this.map.put(key, value);
	}
	public Object getAttribute(final String key){
		return this.map.get(key);
	}
	public final String getSessionId(){
		return this.sessionId;
	}
}
