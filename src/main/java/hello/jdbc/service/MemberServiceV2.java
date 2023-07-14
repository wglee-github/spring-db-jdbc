package hello.jdbc.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 트랜잭션 - 파라미터 연동, 풀을 고려한 종료
 *
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

	private final DataSource dataSource;
	private final MemberRepositoryV2 memberRepositoryV1;
	
	public void accountTransfer(String fromId, String toId, int money) throws SQLException {
		Connection con = dataSource.getConnection();
		
		try {
			con.setAutoCommit(false);
			
			bizLogic(con, fromId, toId, money);
			
			con.commit();
			
		} catch (Exception e) {
			con.rollback();
			log.error("error ", e);
			throw new IllegalStateException("이제 중 오류 발생");
		} finally {
			release(con);
		}
	}

	private void bizLogic(Connection con, String fromId, String toId, int money) throws SQLException {
		Member fromMember = memberRepositoryV1.findById(con, fromId);
		Member toMember = memberRepositoryV1.findById(con, toId);
		
		memberRepositoryV1.update(con, fromId, fromMember.getMoney() - money);
		validation(toMember);
		memberRepositoryV1.update(con, toId, toMember.getMoney() + money);
	}
	
	private void release(Connection con) {
		if(con != null) {
			try {
				con.setAutoCommit(true);
				con.close();
			} catch (Exception e) {
				log.error("error", e);
			}
		}
	}
	
	private void validation(Member toMember) {
		if(toMember.getMemberId().equals("ex")) {
			throw new IllegalStateException("이체중 예외 발생");
		}
	}
}
