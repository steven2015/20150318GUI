/**
 *
 */
package steven.sql.sqlplus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import steven.sql.client.ExecutionCallback;
import steven.sql.client.OracleClient;
import steven.sql.client.SqlClient;

/**
 * @author Steven
 *
 */
public class Sqlplus implements SqlClient{
	private final byte[] lock = new byte[0];
	private volatile SqlClient client;

	public Sqlplus(){
	}
	public void connect(final String database, final String username, final String password) throws SQLException{
		synchronized(this.lock){
			this.disconnect();
			if(OracleClient.canConnectTo(database)){
				this.client = new OracleClient(database, username, password);
			}
		}
	}
	public void disconnect() throws SQLException{
		synchronized(this.lock){
			if(this.client != null){
				try{
					this.client.cancel();
				}finally{
					try{
						this.client.rollback();
					}finally{
						try{
							this.client.close();
						}finally{
							this.client = null;
						}
					}
				}
			}
		}
	}
	@Override
	public String getUsername(){
		synchronized(this.lock){
			if(this.client == null){
				return null;
			}else{
				return this.client.getUsername();
			}
		}
	}
	@Override
	public String getDatabase(){
		synchronized(this.lock){
			if(this.client == null){
				return null;
			}else{
				return this.client.getDatabase();
			}
		}
	}
	@Override
	public void close() throws SQLException{
		this.disconnect();
	}
	@Override
	public void commit() throws SQLException{
		synchronized(this.lock){
			if(this.client != null){
				this.client.commit();
			}
		}
	}
	@Override
	public void rollback() throws SQLException{
		synchronized(this.lock){
			if(this.client != null){
				this.client.rollback();
			}
		}
	}
	@Override
	public void prepare(final String name, final String sql) throws SQLException{
		synchronized(this.lock){
			if(this.client != null){
				this.client.prepare(name, sql);
			}
		}
	}
	@Override
	public void execute(final String name, final Object[] parameters, final ExecutionCallback callback) throws SQLException{
		synchronized(this.lock){
			if(this.client != null){
				new Thread(() -> {
					try{
						Sqlplus.this.client.execute(name, parameters, callback);
					}catch(final SQLException e){
						if(callback != null){
							callback.onAsyncSQLException(e);
						}
					}
				}).start();
			}
		}
	}
	@Override
	public void execute(final String sql, final ExecutionCallback callback) throws SQLException{
		synchronized(this.lock){
			if(this.client != null){
				new Thread(() -> {
					try{
						Sqlplus.this.client.execute(sql, callback);
					}catch(final SQLException e){
						if(callback != null){
							callback.onAsyncSQLException(e);
						}
					}
				}).start();
			}
		}
	}
	@Override
	public void cancel() throws SQLException{
		synchronized(this.lock){
			if(this.client != null){
				this.client.cancel();
			}
		}
	}
	@Override
	public boolean isActive(){
		synchronized(this.lock){
			if(this.client == null){
				return false;
			}else{
				return this.client.isActive();
			}
		}
	}
	public static final void main(final String[] args) throws SQLException, IOException{
		try(final Sqlplus sqlplus = new Sqlplus(); final InputStreamReader isr = new InputStreamReader(System.in); final BufferedReader br = new BufferedReader(isr);){
			final ExecutionCallback callback = new ExecutionCallback(){
				@Override
				public void onRowFetched(final Object[] row){
					for(final Object o : row){
						System.out.print(o);
						System.out.print('\t');
					}
					System.out.println();
				}
				@Override
				public void onRowAffected(final int affectedRows, final Object[] parameters){
					System.out.println(affectedRows + " rows affected.");
				}
				@Override
				public void onNoMoreRows(final int fetchedRows){
					System.out.println(fetchedRows + " rows fetched.");
				}
				@Override
				public void onNoMoreMessages(){
				}
				@Override
				public void onMetaDataReceived(final ResultSetMetaData meta){
					try{
						final int columnCount = meta.getColumnCount();
						for(int i = 0; i < columnCount; i++){
							System.out.print(meta.getColumnLabel(i + 1));
							System.out.print('\t');
						}
						System.out.println();
					}catch(final SQLException e){
						e.printStackTrace();
					}
				}
				@Override
				public void onMessageReceived(final String line){
					System.out.println(line);
				}
				@Override
				public void onSuccess(final long timeSpent){
					System.out.println("Spent " + timeSpent + " ms.");
				}
				@Override
				public void onFailure(final long timeSpent){
					System.out.println("Spent " + timeSpent + " ms.");
				}
				@Override
				public void onAsyncSQLException(final SQLException e){
					e.printStackTrace();
				}
			};
			sqlplus.connect("shpd", null, null);
			System.out.println("Connected to " + sqlplus.getUsername() + "@" + sqlplus.getDatabase());
			sqlplus.execute("select sysdate from dual", callback);
			String line = null;
			while((line = br.readLine()) != null){
				line = line.trim();
				if(line.endsWith(";")){
					line = line.substring(0, line.length() - 1);
				}
				if("exit".equalsIgnoreCase(line)){
					sqlplus.rollback();
					System.out.println("Disconnected.");
					break;
				}else if("commit".equalsIgnoreCase(line)){
					sqlplus.commit();
					System.out.println("Committed.");
				}else if("roll".equalsIgnoreCase(line) || "rollback".equalsIgnoreCase(line)){
					sqlplus.rollback();
					System.out.println("Rolled back.");
				}else{
					sqlplus.execute(line, callback);
				}
			}
		}
	}
}
