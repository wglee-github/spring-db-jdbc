package hello.jdbc.exception.basic;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;

public class UnCheckedAppTest {

	@Test
	void unChecked() {
		Controller controller = new Controller();
		assertThatThrownBy(() -> controller.request())
			.isInstanceOf(Exception.class);
	}
	
	static class Controller {
		Service service = new Service();
		
		public void request(){
			service.logic();
		}
		
	}
	
	static class Service {
		Repository repository = new Repository();
		NetworkClient client = new NetworkClient();
		
		public void logic(){
			repository.call();
			client.call();
		}
	}
	
	static class NetworkClient {
		
		public void call() {
			throw new RuntimeConnectionException("연결 오류");
		}
	}
	
	static class Repository {
		
		public void call()  {
			try {
				runSql();
			} catch (SQLException e) {
				throw new RuntimeSqlException(e);			
			}
		}
		
		public void runSql() throws SQLException {
			throw new SQLException("ex");
		}
	}
	
	static class RuntimeConnectionException extends RuntimeException {
		public RuntimeConnectionException(String message) {
			super(message);
		}
		
	}
	
	static class RuntimeSqlException extends RuntimeException {
		public RuntimeSqlException(String message) {
			super(message);
		}

		public RuntimeSqlException(Throwable cause) {
			super(cause);
		}
	}
}
