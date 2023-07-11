package hello.jdbc.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import hello.jdbc.domain.Member;

class MemberRepositoryV0Test {

	MemberRepositoryV0 repository = new MemberRepositoryV0();
	
	@Test
	void curd() throws SQLException {
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
	}

}
