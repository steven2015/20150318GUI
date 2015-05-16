/**
 *
 */
package steven.sql.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.jdbc.OracleDriver;

/**
 * @author Steven
 *
 */
public class OracleClient implements SqlClient{
	private static Map<String, DatabaseEnvironment> _MAP;
	public static final String INTERNAL_DBMS_OUTPUT_ENABLE = "INTERNAL.DBMS_OUTPUT.ENABLE";
	public static final String INTERNAL_DBMS_OUTPUT_DISABLE = "INTERNAL.DBMS_OUTPUT.DISABLE";
	public static final String INTERNAL_DBMS_OUTPUT_GET_LINE = "INTERNAL.DBMS_OUTPUT.GET_LINE";
	private final String database;
	private final String username;
	private final Connection connection;
	private final Map<String, CallableStatement> statements = new HashMap<>();
	private volatile CallableStatement currentStatement;
	static{
		try{
			DriverManager.registerDriver(new OracleDriver());
			OracleClient.refreshSettings();
		}catch(final SQLException | IOException e){
			e.printStackTrace();
		}
	}

	public OracleClient(final String database, final boolean defaultOrDba) throws SQLException{
		this.database = database;
		final DatabaseEnvironment environment = OracleClient._MAP.get(database);
		if(defaultOrDba){
			this.username = environment.defaultUsername;
		}else{
			this.username = environment.dbaUsername;
		}
		this.connection = OracleClient.createConnection(environment.url, this.username, environment.users.get(this.username));
		this.initialize();
	}
	public OracleClient(final String database) throws SQLException{
		this(database, true);
	}
	public OracleClient(final String database, final String username) throws SQLException{
		this(database, username, null);
	}
	public OracleClient(final String database, final String username, final String password) throws SQLException{
		this.database = database;
		final DatabaseEnvironment environment = OracleClient._MAP.get(database);
		if(username == null){
			this.username = environment.defaultUsername;
		}else{
			this.username = username;
		}
		if(password == null){
			this.connection = OracleClient.createConnection(environment.url, this.username, environment.users.get(this.username));
		}else{
			this.connection = OracleClient.createConnection(environment.url, this.username, password);
		}
		this.initialize();
	}
	private static final Connection createConnection(final String url, final String username, final String password) throws SQLException{
		final Connection c = DriverManager.getConnection(url, username, password);
		c.setAutoCommit(false);
		return c;
	}
	private final void initialize() throws SQLException{
		this.prepare(OracleClient.INTERNAL_DBMS_OUTPUT_DISABLE, "{call dbms_output.disable}");
		this.prepare(OracleClient.INTERNAL_DBMS_OUTPUT_ENABLE, "{call dbms_output.enable(null)}");
		this.prepare(OracleClient.INTERNAL_DBMS_OUTPUT_GET_LINE, "{call dbms_output.get_line(?,?)}");
		{
			final CallableStatement cs = this.statements.get(OracleClient.INTERNAL_DBMS_OUTPUT_GET_LINE);
			cs.registerOutParameter(1, JDBCType.VARCHAR.getVendorTypeNumber());
			cs.registerOutParameter(2, JDBCType.NUMERIC.getVendorTypeNumber());
		}
		{
			final CallableStatement cs = this.statements.get(OracleClient.INTERNAL_DBMS_OUTPUT_ENABLE);
			cs.execute();
		}
	}
	@SuppressWarnings("unchecked")
	public static final void refreshSettings() throws IOException{
		final Map<String, DatabaseEnvironment> map = new HashMap<>();
		try(final InputStream is = new FileInputStream(new File(System.getenv("TNS_ADMIN") + File.separator + "tnsnames.ora")); final InputStreamReader isr = new InputStreamReader(is, "UTF8"); final BufferedReader br = new BufferedReader(isr);){
			String line = null;
			final StringBuilder sb = new StringBuilder();
			final List<KeyValue> stack = new ArrayList<>();
			boolean key = true;
			while((line = br.readLine()) != null){
				line = line.replace(" ", "").replace("\t", "").toLowerCase();
				if(line.startsWith("#") == false){
					sb.append(line);
					boolean pass = true;
					while(pass && sb.length() > 0){
						if(key){
							final int equalIndex = sb.indexOf("=");
							if(equalIndex >= 0){
								stack.add(new KeyValue(sb.substring(0, equalIndex)));
								sb.delete(0, equalIndex + 1);
								key = false;
							}else{
								pass = false;
							}
						}else if(sb.charAt(0) == '('){
							sb.delete(0, 1);
							key = true;
						}else if(sb.charAt(0) == ')'){
							sb.delete(0, 1);
							final KeyValue current = stack.remove(stack.size() - 1);
							final KeyValue top = stack.get(stack.size() - 1);
							if(top.value == null){
								top.value = current;
							}else if(top.value instanceof KeyValue){
								final List<KeyValue> list = new ArrayList<>();
								list.add((KeyValue)top.value);
								list.add(current);
								top.value = list;
							}else{
								((List<KeyValue>)top.value).add(current);
							}
							if(stack.size() == 1){
								key = true;
								final String name = top.key.replace(".world", "");
								String host = null;
								String port = null;
								String sid = null;
								while(stack.size() > 0){
									final KeyValue kv = stack.remove(0);
									if(kv.value instanceof String){
										if("host".equals(kv.key)){
											host = (String)kv.value;
										}else if("port".equals(kv.key)){
											port = (String)kv.value;
										}else if("sid".equals(kv.key)){
											sid = (String)kv.value;
										}else if("service_name".equals(kv.key)){
											sid = (String)kv.value;
										}
									}else if(kv.value instanceof List){
										stack.addAll((List<KeyValue>)kv.value);
									}else if(kv.value instanceof KeyValue){
										stack.add((KeyValue)kv.value);
									}
								}
								final DatabaseEnvironment environment = new DatabaseEnvironment("jdbc:oracle:thin:@" + host + ":" + port + ":" + sid);
								map.put(name, environment);
							}
						}else{
							final int closeIndex = sb.indexOf(")");
							if(closeIndex >= 0){
								stack.get(stack.size() - 1).value = sb.substring(0, closeIndex);
								sb.delete(0, closeIndex);
							}else{
								pass = false;
							}
						}
					}
				}
			}
		}
		try(final InputStream is = new FileInputStream(new File(System.getenv("SQLPATH") + File.separator + "db.properties")); final InputStreamReader isr = new InputStreamReader(is, "UTF8"); final BufferedReader br = new BufferedReader(isr);){
			String line = null;
			int mode = 0;
			while((line = br.readLine()) != null){
				line = line.trim();
				if("# dev".equals(line)){
					mode = 1;
				}else if("# uat".equals(line)){
					mode = 2;
				}else if("# production".equals(line)){
					mode = 3;
				}else if(line.length() > 0 && mode > 0){
					final int equalIndex = line.indexOf('=');
					final String name = line.substring(0, equalIndex);
					final String[] parts = line.substring(equalIndex + 1).split(",");
					final DatabaseEnvironment environment = map.get(name);
					if(environment != null){
						for(int i = 0; i < parts.length; i += 2){
							environment.users.put(parts[i], parts[i + 1]);
						}
						environment.dbaUsername = parts[0];
						if(mode == 1){
							environment.defaultUsername = "sys_iv";
							environment.users.put("sys_iv", "sys_iv");
							environment.users.put("syspos", "syspos");
							environment.users.put("sysvip", "sysvip");
							environment.users.put("sysapp", "sysapp");
							environment.users.put("sm1", "sm1");
						}else if(mode == 2){
							environment.defaultUsername = "sys_iv";
						}else if(mode == 3){
							environment.defaultUsername = parts[0];
						}
					}
				}
			}
		}
		OracleClient._MAP = map;
	}
	public static final boolean canConnectTo(final String database){
		return OracleClient._MAP.containsKey(database);
	}
	@Override
	public void close() throws SQLException{
		this.connection.close();
	}
	@Override
	public void commit() throws SQLException{
		this.connection.commit();
	}
	@Override
	public void rollback() throws SQLException{
		this.connection.rollback();
	}
	@Override
	public void prepare(final String name, final String sql) throws SQLException{
		if(this.statements.containsKey(name) == false){
			final CallableStatement cs = this.connection.prepareCall(sql);
			this.statements.put(name, cs);
		}
	}
	@Override
	public void execute(final String name, final Object[] parameters, final ExecutionCallback callback) throws SQLException{
		final CallableStatement cs = this.statements.get(name);
		this.execute(cs, parameters, callback);
	}
	@Override
	public void executeSql(final String sql, final Object[] outParameters, final ExecutionCallback callback) throws SQLException{
		try(final CallableStatement cs = this.connection.prepareCall(sql);){
			this.execute(cs, outParameters, callback);
		}
	}
	private void execute(final CallableStatement cs, final Object[] parameters, final ExecutionCallback callback) throws SQLException{
		boolean success = false;
		int fetchedRows = 0;
		int affectedRows = 0;
		final long startTime = System.currentTimeMillis();
		try{
			this.currentStatement = cs;
			boolean hasOutParameters = false;
			if(parameters != null && parameters.length > 0){
				int parameterIndex = 1;
				for(final Object p : parameters){
					if(p instanceof SQLType){
						cs.registerOutParameter(parameterIndex, ((SQLType)p).getVendorTypeNumber());
						hasOutParameters = true;
					}else{
						cs.setObject(parameterIndex, p);
					}
					parameterIndex++;
				}
			}
			final boolean result = cs.execute();
			if(hasOutParameters){
				for(int i = 0, j = 1; i < parameters.length; i = j, j++){
					if(parameters[i] instanceof SQLType){
						parameters[i] = cs.getObject(j);
					}
				}
			}
			if(callback != null){
				if(result){
					affectedRows = -1;
					try(ResultSet rs = cs.getResultSet();){
						final ResultSetMetaData meta = rs.getMetaData();
						callback.onMetaDataReceived(meta);
						rs.setFetchSize(50);
						final int columnCount = meta.getColumnCount();
						while(rs.next()){
							final Object[] record = new Object[columnCount];
							for(int i = 0, j = 1; i < columnCount; i = j, j++){
								record[i] = rs.getObject(j);
							}
							callback.onRowFetched(record);
							fetchedRows++;
						}
						callback.onNoMoreRows();
					}
				}else{
					fetchedRows = -1;
					affectedRows = cs.getUpdateCount();
					callback.onRowAffected(affectedRows);
				}
				final CallableStatement cs2 = this.statements.get(OracleClient.INTERNAL_DBMS_OUTPUT_GET_LINE);
				while(true){
					cs2.executeUpdate();
					final int status = cs2.getInt(2);
					if(status == 0){
						callback.onMessageReceived(cs2.getString(1));
					}else{
						break;
					}
				}
				callback.onNoMoreMessages();
			}
			success = true;
		}finally{
			if(callback != null){
				final long timeSpent = System.currentTimeMillis() - startTime;
				if(success){
					callback.onSuccess(timeSpent, fetchedRows, affectedRows);
				}else{
					callback.onFailure(timeSpent);
				}
			}
			this.currentStatement = null;
			cs.clearParameters();
		}
	}
	@Override
	public void cancel() throws SQLException{
		if(this.currentStatement != null){
			// avoid synchronize
			try{
				this.currentStatement.cancel();
			}catch(final NullPointerException e){
				// do nothing
			}
		}
	}
	@Override
	public boolean isActive(){
		return this.currentStatement != null;
	}
	@Override
	public final String getUsername(){
		return this.username;
	}
	@Override
	public final String getDatabase(){
		return this.database;
	}

	private static final class DatabaseEnvironment{
		private final String url;
		private final Map<String, String> users = new HashMap<>();
		private String defaultUsername;
		private String dbaUsername;

		private DatabaseEnvironment(final String url){
			this.url = url;
		}
	}
	private static final class KeyValue{
		private final String key;
		private Object value;

		private KeyValue(final String key){
			this.key = key;
		}
		private KeyValue(final String key, final Object value){
			this.key = key;
			this.value = value;
		}
	}
}
