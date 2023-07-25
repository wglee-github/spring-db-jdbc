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

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import hello.jdbc.repository.MemberRepositoryV3;
import hello.jdbc.repository.MemberRepositoryV4_1;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 트랜잭션 - dataSource, transactionManager 자동 등록
 */
@Slf4j
@SpringBootTest
class MemberServiceV4Test {

	private static final String MEMBER_A = "memberA";
	private static final String MEMBER_B = "memberB";
	private static final String MEMBER_EX = "ex";
	
	@Autowired
	private MemberRepository repository;
	@Autowired
	private MemberServiceV4 service;
	
	@TestConfiguration
	static class TestConfig {
		
		private final DataSource dataSource;
		
		public TestConfig(DataSource dataSource) {
			this.dataSource = dataSource;
		}

		@Bean
		MemberRepository memberRepository() {
			return new MemberRepositoryV4_1(dataSource);
		}
		
		@Bean
		MemberServiceV4 memberServiceV4() {
			return new MemberServiceV4(memberRepository());
		}
	}
	
	@AfterEach
	void after() {
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
	void accountTransfer() {
		
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
	void accountTransferEx() {
		
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
