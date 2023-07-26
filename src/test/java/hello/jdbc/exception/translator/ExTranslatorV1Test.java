package hello.jdbc.exception.translator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcUtils;

import hello.jdbc.connection.ConnectionConst;
import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDuplicateKeyException;
import hello.jdbc.repository.ex.MydbException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExTranslatorV1Test {

	private Repository repository;
	private Service service;
	
	@BeforeEach
	void init() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource(ConnectionConst.URL, ConnectionConst.USERNAME, ConnectionConst.PASSWORD);
		repository = new Repository(dataSource);
		service = new Service(repository);
	}
	
	@Test
	void memberDuplicateSave() {
		service.create("myId");
		service.create("myId");
	}
	
	@RequiredArgsConstructor
	static class Service {
		private final Repository repository;
		
		public void create(String memberId) {
			
			try {
				repository.save(new Member(memberId, 0));
			} catch (MyDuplicateKeyException e) {
				log.info("key 중복 에러 발생={}", e);
				String retryId = generateNewId(memberId);
				log.info("retrykey={}", retryId);
				repository.save(new Member(retryId, 0));
			} catch (MydbException ex) {
				throw ex;
			}
		}
		
		private String generateNewId(String memberId) {
			return memberId + new Random().nextInt(10000);
		}
	}
	
	@RequiredArgsConstructor
	static class Repository {
		private final DataSource dataSource;
		
		public Member save(Member member) {
			
			String sql = "insert into member (member_id, money) values (?, ?)";
			Connection con = null;
			PreparedStatement pstmt = null;
			
			try {
				con = dataSource.getConnection();
				pstmt = con.prepareCall(sql);
				pstmt.setString(1, member.getMemberId());
				pstmt.setInt(2, member.getMoney());
				pstmt.executeUpdate();
				return member;
			} catch (SQLException e) {
				if(e.getErrorCode() == 23505) {
					throw new MyDuplicateKeyException(e);
				}
				throw new MydbException(e);
			} finally {
				JdbcUtils.closeStatement(pstmt);
				JdbcUtils.closeConnection(con);
			}
			
		}
	}
}
