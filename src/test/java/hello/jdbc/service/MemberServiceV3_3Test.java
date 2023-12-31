package hello.jdbc.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import hello.jdbc.connection.ConnectionConst;
import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 트랜잭션 - 커넥션 파라미터 전달 방식 동기화
 */
@Slf4j
@SpringBootTest
class MemberServiceV3_3Test {

	private static final String MEMBER_A = "memberA";
	private static final String MEMBER_B = "memberB";
	private static final String MEMBER_EX = "ex";
	
	@Autowired
	private MemberRepositoryV3 repository;
	@Autowired
	private MemberServiceV3_3 service;
	
	@TestConfiguration
	static class TestConfig {
		
		@Bean
		DataSource dataSource() {
			return new DriverManagerDataSource(ConnectionConst.URL, ConnectionConst.USERNAME, ConnectionConst.PASSWORD);
		}
		
		@Bean
		PlatformTransactionManager transactionManager() {
			return new JdbcTransactionManager(dataSource());
		}
		
		@Bean
		MemberRepositoryV3 memberRepositoryV3() {
			return new MemberRepositoryV3(dataSource());
		}
		
		@Bean
		MemberServiceV3_3 memberServiceV3_3() {
			return new MemberServiceV3_3(memberRepositoryV3());
		}
	}
	
	@AfterEach
	void after() throws SQLException {
		repository.delete(MEMBER_A);
		repository.delete(MEMBER_B);
		repository.delete(MEMBER_EX);
	}
	
	@Test
	void aopCheck() {
		log.info("MemberService={}", service.getClass());
		log.info("MemberRepository={}", repository.getClass());
		assertThat(AopUtils.isAopProxy(service)).isTrue();
		assertThat(AopUtils.isAopProxy(repository)).isFalse();
	}
	
	@Test
	@DisplayName("정상이체")
	void accountTransfer() throws SQLException {
		
		Member memberA = new Member(MEMBER_A, 10000);
		Member memberB = new Member(MEMBER_B, 10000);
		
		repository.save(memberA);
		repository.save(memberB);
		
		log.info("TRANS Start");
		
		service.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);
		log.info("TRANS End");
		
		Member findMemberA = repository.findById(memberA.getMemberId());
		Member findMemberB = repository.findById(memberB.getMemberId());
		
		assertThat(findMemberA.getMoney()).isEqualTo(8000);
		assertThat(findMemberB.getMoney()).isEqualTo(12000);
	}
	
	@Test
	@DisplayName("이체 중 예외 발생")
	void accountTransferEx() throws SQLException {
		
		Member memberA = new Member(MEMBER_A, 10000);
		Member memberEX = new Member(MEMBER_EX, 10000);
		
		repository.save(memberA);
		repository.save(memberEX);
		
		Assertions.assertThrows(IllegalStateException.class, () -> service.accountTransfer(memberA.getMemberId(), memberEX.getMemberId(), 2000));
		
		Member findMemberA = repository.findById(memberA.getMemberId());
		Member findMemberB = repository.findById(memberEX.getMemberId());
		
		assertThat(findMemberA.getMoney()).isEqualTo(10000);
		assertThat(findMemberB.getMoney()).isEqualTo(10000);
	}

}
