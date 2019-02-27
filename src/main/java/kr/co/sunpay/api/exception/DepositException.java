package kr.co.sunpay.api.exception;


public class DepositException extends Exception {

	private final int ERR_CODE;
	public final static int CODE_DEFAULT_ERR = 100;
	public final static int CODE_DEPOSIT_LACK = 200;
	
	public DepositException(String msg, int errCode) {
		super(msg);
		ERR_CODE = errCode;
	}
	
	public DepositException(String msg) {
		this(msg, CODE_DEFAULT_ERR);
	}

	public int getErrCode() {
		return ERR_CODE;
	}
}
