/**
 *
 */
package steven.sqlplus.response;

import steven.sqlplus.SqlplusResponse;

/**
 * @author Steven
 *
 */
public class SqlplusUnknownCommandResponse implements SqlplusResponse{
	private final String command;

	public SqlplusUnknownCommandResponse(final String command){
		this.command = command;
	}
	public final String getCommand(){
		return this.command;
	}
}
