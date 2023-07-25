package hello.jdbc.repository.ex;

public class MydbException extends RuntimeException{

	private static final long serialVersionUID = 1836701915586391502L;

	public MydbException(String message, Throwable cause) {
		super(message, cause);
	}

	public MydbException(String message) {
		super(message);
	}

	public MydbException(Throwable cause) {
		super(cause);
	}

}
