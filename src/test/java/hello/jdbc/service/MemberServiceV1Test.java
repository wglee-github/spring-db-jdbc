package hello.jdbc.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import hello.jdbc.connection.ConnectionConst;
import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;

/**
 * 
 * 기본동작, 트랜잭션이 없어서 문제 발생
 */
class MemberServiceV1Test {

	private static final String MEMBER_A = "memberA";
	private static final String MEMBER_B = "memberB";
	private static final String MEMBER_EX = "ex";
	
	private MemberRepositoryV1 repository;
	private MemberServiceV1 service;
	
	@BeforeEach
	void before() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource(ConnectionConst.URL, ConnectionConst.USERNAME, ConnectionConst.PASSWORD);
		repository = new MemberRepositoryV1(dataSource);
		service = new MemberServiceV1(repository);
	}
	
	@AfterEach
	void after() throws SQLException {
		repository.delete(MEMBER_A);
		repository.delete(MEMBER_B);
		repository.delete(MEMBER_EX);
	}
	
	@Test
	@DisplayName("정상이체")
	void accountTransfer() throws SQLException {
		
		Member memberA = new Member(MEMBER_A, 10000);
		Member memberB = new Member(MEMBER_B, 10000);
		
		repository.save(memberA);
		repository.save(memberB);
		
		service.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);
		
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
		
//		service.accountTransfer(memberA.getMemberId(), memberEX.getMemberId(), 2000);
		
		Assertions.assertThrows(IllegalStateException.class, () -> service.accountTransfer(memberA.getMemberId(), memberEX.getMemberId(), 2000));
		
		Member findMemberA = repository.findById(memberA.getMemberId());
		Member findMemberB = repository.findById(memberEX.getMemberId());
		
		assertThat(findMemberA.getMoney()).isEqualTo(8000);
		assertThat(findMemberB.getMoney()).isEqualTo(10000);
	}

}
