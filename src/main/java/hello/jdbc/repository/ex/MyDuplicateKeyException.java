package hello.jdbc.repository.ex;

public class MyDuplicateKeyException extends MydbException{

	private static final long serialVersionUID = -5881612909153542173L;

	public MyDuplicateKeyException(String message, Throwable cause) {
		super(message, cause);
	}

	public MyDuplicateKeyException(String message) {
		super(message);
	}

	public MyDuplicateKeyException(Throwable cause) {
		super(cause);
	}

}
