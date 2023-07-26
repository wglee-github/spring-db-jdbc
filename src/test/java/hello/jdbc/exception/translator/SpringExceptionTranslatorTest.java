package hello.jdbc.exception.translator;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import hello.jdbc.connection.ConnectionConst;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpringExceptionTranslatorTest {

	DataSource dataSource;
	
	@BeforeEach
	void init() {
		dataSource = new DriverManagerDataSource(ConnectionConst.URL, ConnectionConst.USERNAME, ConnectionConst.PASSWORD); 
	}
	
	@Test
	void sqlExceptionErrorCode() {
		
		String sql = "select bad grammar";
		
		try {
			Connection con = dataSource.getConnection();
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.executeQuery();
		} catch (SQLException e) {
			assertThat(e.getErrorCode()).isEqualTo(42122);
			int errorCode = e.getErrorCode();
            log.info("errorCode={}", errorCode);
            //org.h2.jdbc.JdbcSQLSyntaxErrorException
            log.info("error", e);
		} 
		
	}
	
	@Test
	void exceptionTranslator() {
		
		String sql = "select bad grammar";
		
		try {
			Connection con = dataSource.getConnection();
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.executeQuery();
		} catch (SQLException e) {
			assertThat(e.getErrorCode()).isEqualTo(42122);
			//org.springframework.jdbc.support.sql-error-codes.xml
			SQLExceptionTranslator translator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
			//org.springframework.jdbc.BadSqlGrammarException
			DataAccessException resultEx = translator.translate("select", sql, e);
			log.info("resultEx error", resultEx);
			assertThat(resultEx.getClass()).isEqualTo(BadSqlGrammarException.class);
		} 
		
	}
}
