/**
 *
 */
package steven.sqlplus.request;

import steven.sqlplus.SqlplusRequest;

/**
 * @author Steven
 *
 */
public class SqlplusUnknownCommandRequest implements SqlplusRequest{
	private final String command;

	public SqlplusUnknownCommandRequest(final String command){
		this.command = command;
	}
	public final String getCommand(){
		return this.command;
	}
}
