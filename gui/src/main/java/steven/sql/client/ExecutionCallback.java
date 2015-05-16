/**
 *
 */
package steven.sql.client;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * @author Steven
 *
 */
public interface ExecutionCallback{
	public void onMetaDataReceived(ResultSetMetaData meta);
	public void onRowFetched(Object[] row);
	public void onNoMoreRows();
	public void onRowAffected(int affectedRows);
	public void onMessageReceived(String line);
	public void onNoMoreMessages();
	public void onSuccess(long timeSpent, int fetchedRows, int affectedRows);
	public void onFailure(long timeSpent);
	public void onAsyncSQLException(SQLException e);
}
