/**
 *
 */
package steven.sqlplus;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import steven.sqlplus.request.SqlplusConnectRequest;
import steven.sqlplus.request.SqlplusUnknownCommandRequest;
import steven.sqlplus.response.SqlplusExceptionResponse;
import steven.sqlplus.response.SqlplusUnknownCommandResponse;

/**
 * @author Steven
 *
 */
public class SqlplusClient{
	private final SqlplusCallback callback;
	private final byte[] lock = new byte[0];
	private volatile Connection connection;
	private volatile Statement statement;
	private volatile String database;
	private volatile String username;

	public SqlplusClient(final SqlplusCallback callback){
		this.callback = callback;
	}
	public void handleRequest(final SqlplusRequest request){
		if(request instanceof SqlplusConnectRequest){
			final SqlplusConnectRequest r = (SqlplusConnectRequest)request;
			synchronized(this.lock){
				if(this.connection != null){
					if(this.statement != null){
						try{
							this.statement.cancel();
						}catch(final SQLException e){
							e.printStackTrace(System.err);
						}finally{
							try{
								this.statement.close();
							}catch(final SQLException e){
								e.printStackTrace(System.err);
							}
						}
						this.statement = null;
					}
					try{
						this.connection.rollback();
					}catch(final SQLException e){
						e.printStackTrace(System.err);
					}finally{
						try{
							this.connection.close();
						}catch(final SQLException e){
							e.printStackTrace(System.err);
						}
					}
					this.connection = null;
				}
				if(r.getDatabase() != null){
					database = r.getDatabase();
				}
				username = r.getUsername();
				String password = r.getPassword();
				try{
					//this.connection = ConnectionManager.INSTANCE.openConnection(database, username, password);
					throw new SQLException(password);
				}catch(final SQLException e){
					this.connection = null;
					this.callback.handleResponse(new SqlplusExceptionResponse("Cannot open connection to " + username + "@" + database + ".", e));
					return;
				}
			}
		}else if(request instanceof SqlplusUnknownCommandRequest){
			final SqlplusResponse response = new SqlplusUnknownCommandResponse(((SqlplusUnknownCommandRequest)request).getCommand());
			this.callback.handleResponse(response);
			return;
		}
	}
	public void close(){
		synchronized(this.lock){
			if(this.connection != null){
				if(this.statement != null){
					try{
						this.statement.cancel();
					}catch(final SQLException e){
						e.printStackTrace(System.err);
					}finally{
						try{
							this.statement.close();
						}catch(final SQLException e){
							e.printStackTrace(System.err);
						}
					}
					this.statement = null;
				}
				try{
					this.connection.rollback();
				}catch(final SQLException e){
					e.printStackTrace(System.err);
				}finally{
					try{
						this.connection.close();
					}catch(final SQLException e){
						e.printStackTrace(System.err);
					}
				}
				this.connection = null;
			}
		}
	}
}
