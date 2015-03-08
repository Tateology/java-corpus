package freecs.external;

public class AccessForbiddenException extends RuntimeException {

	private static final long serialVersionUID = -5625457892356046398L;
	boolean hidePage = true;
	
	public AccessForbiddenException(boolean hidePage) {
		super();
		this.hidePage = hidePage;
	}

	public boolean hidePage() {
		return hidePage;
	}
	
}
