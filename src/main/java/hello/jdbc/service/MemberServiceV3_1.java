package hello.jdbc.service;

import java.sql.SQLException;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 트랜잭션 - 트랜잭션 매니저(트랜잭션을 추상화, 리소스 동기화)
 *
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {
	// 트랜잭션을 추상화, 리소스 동기화
	private final PlatformTransactionManager transactionManager;
	private final MemberRepositoryV3 memberRepositoryV3;
	
	public void accountTransfer(String fromId, String toId, int money) throws SQLException {
		TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			
			bizLogic(fromId, toId, money);
			transactionManager.commit(status);
		} catch (Exception e) {
			transactionManager.rollback(status);
			log.error("error ", e);
			throw new IllegalStateException("이제 중 오류 발생");
		}
	}

	private void bizLogic(String fromId, String toId, int money) throws SQLException {
		Member fromMember = memberRepositoryV3.findById(fromId);
		Member toMember = memberRepositoryV3.findById(toId);
		
		memberRepositoryV3.update(fromId, fromMember.getMoney() - money);
		validation(toMember);
		memberRepositoryV3.update(toId, toMember.getMoney() + money);
	}
	
	private void validation(Member toMember) {
		if(toMember.getMemberId().equals("ex")) {
			throw new IllegalStateException("이체중 예외 발생");
		}
	}
}
