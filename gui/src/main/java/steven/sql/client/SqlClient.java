/**
 *
 */
package steven.sql.client;

import java.sql.SQLException;

/**
 * @author Steven
 *
 */
public interface SqlClient extends AutoCloseable{
	public String getUsername();
	public String getDatabase();
	@Override
	public void close() throws SQLException;
	public void commit() throws SQLException;
	public void rollback() throws SQLException;
	public void prepare(String name, String sql) throws SQLException;
	public void execute(String name, Object[] parameters, ExecutionCallback callback) throws SQLException;
	public void execute(String sql, ExecutionCallback callback) throws SQLException;
	public void cancel() throws SQLException;
	public boolean isActive();
}
