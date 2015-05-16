/**
 *
 */
package steven.sqlplus.request;

import steven.sqlplus.SqlplusRequest;

/**
 * @author Steven
 *
 */
public class SqlplusConnectRequest implements SqlplusRequest{
	private final String username;
	private final String password;
	private final String database;

	public SqlplusConnectRequest(final String username){
		this(username, null);
	}
	public SqlplusConnectRequest(final String username, final String database){
		this(username, null, database);
	}
	public SqlplusConnectRequest(final String username, final String password, final String database){
		this.username = username;
		this.password = password;
		this.database = database;
	}
	public final String getUsername(){
		return this.username;
	}
	public final String getPassword(){
		return this.password;
	}
	public final String getDatabase(){
		return this.database;
	}
}
