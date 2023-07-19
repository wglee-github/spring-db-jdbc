package hello.jdbc.exception.basic;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.ConnectException;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

public class CheckedAppTest {

	@Test
	void checked() {
		Controller controller = new Controller();
		assertThatThrownBy(() -> controller.request())
			.isInstanceOf(Exception.class);
	}
	
	static class Controller {
		Service service = new Service();
		
		public void request() throws ConnectException, SQLException {
			service.logic();
		}
		
	}
	
	static class Service {
		Repository repository = new Repository();
		NetworkClient client = new NetworkClient();
		
		public void logic() throws SQLException, ConnectException {
			repository.call();
			client.call();
		}
	}
	
	static class NetworkClient {
		
		public void call() throws ConnectException {
			throw new ConnectException("연결 오류");
		}
	}
	
	static class Repository {
		
		public void call() throws SQLException {
			throw new SQLException("ex");
		}
	}
}
