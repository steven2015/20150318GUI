/**
 *
 */
package steven.sqlplus;

import steven.sqlplus.request.SqlplusUnknownCommandRequest;
import steven.sqlplus.request.SqlplusUnknownCommandResponse;

/**
 * @author Steven
 *
 */
public class SqlplusClient{
	private final SqlplusCallback callback;

	public SqlplusClient(final SqlplusCallback callback){
		this.callback = callback;
	}
	public void handleRequest(final SqlplusRequest request){
		if(request instanceof SqlplusUnknownCommandRequest){
			final SqlplusResponse response = new SqlplusUnknownCommandResponse(((SqlplusUnknownCommandRequest)request).getCommand());
			this.callback.handleResponse(response);
		}
	}
}
