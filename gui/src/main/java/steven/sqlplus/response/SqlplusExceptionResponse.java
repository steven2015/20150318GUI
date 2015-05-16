/**
 *
 */
package steven.sqlplus.response;

import steven.sqlplus.SqlplusResponse;

/**
 * @author Steven
 *
 */
public class SqlplusExceptionResponse implements SqlplusResponse{
	private final String message;
	private final Exception e;

	public SqlplusExceptionResponse(final String message, final Exception e){
		this.message = message;
		this.e = e;
	}
	public final String getMessage(){
		return this.message;
	}
	public final Exception getE(){
		return this.e;
	}
}
