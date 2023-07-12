package hello.jdbc.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.zaxxer.hikari.HikariDataSource;

import hello.jdbc.connection.ConnectionConst;
import hello.jdbc.domain.Member;

class MemberRepositoryV1Test {

	MemberRepositoryV1 repository;
	
	@BeforeEach
	void beforeEach() {
		//기본 DriverManager - 항상 새로운 커넥션 획득
//		DriverManagerDataSource dataSource = new DriverManagerDataSource(ConnectionConst.URL, ConnectionConst.USERNAME, ConnectionConst.PASSWORD);
		
		//커넥션 풀링: HikariProxyConnection -> JdbcConnection
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl(ConnectionConst.URL);
		dataSource.setUsername(ConnectionConst.USERNAME);
		dataSource.setPassword(ConnectionConst.PASSWORD);
		dataSource.setMaximumPoolSize(10);
		dataSource.setPoolName("MyPool");
		
		repository = new MemberRepositoryV1(dataSource);
	}
	
	@Test
	void curd() throws SQLException, InterruptedException {
		Member member = new Member("memberV3", 10000);
		Member saveMember = repository.save(member);
		
		Member findMember = repository.findById(saveMember.getMemberId());
		assertThat(saveMember).isEqualTo(findMember);
		
		repository.update(findMember.getMemberId(), 20000);
		
		Member updatedMember = repository.findById(findMember.getMemberId());
		assertThat(updatedMember.getMoney()).isEqualTo(20000);
		
		repository.delete(updatedMember.getMemberId());
		assertThrows(NoSuchElementException.class, () -> repository.findById(updatedMember.getMemberId()));
		assertThatThrownBy(() -> repository.findById(updatedMember.getMemberId()))
			.isInstanceOf(NoSuchElementException.class);
		
		Thread.sleep(1000);
	}

}
