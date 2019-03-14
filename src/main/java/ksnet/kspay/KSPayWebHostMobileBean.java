package ksnet.kspay;

import java.io.*;
import java.util.*;
import java.net.*;

public class KSPayWebHostMobileBean {
	public static void main(String[] args) throws Exception {
		;
	}

	static String KSPAY_WEBHOST_URL = "http://kspay.ksnet.to/store/mb2/web_host/recv_post.jsp";

	static char DEFAULT_DELIM = '`';
	String DEFAULT_RPARAMS = "authyn`trno`trddt`trdtm`amt`authno`msg1`msg2`ordno`isscd`aqucd`result";
	// authyn : O/X 상태
	// trno : KSNET거래번호(영수증 및 취소 등 결제데이터용 KEY
	// trddt : 거래일자(YYYYMMDD)
	// trdtm : 거래시간(hhmmss)
	// amt : 금액
	// authno : 승인번호(신용카드:결제성공시), 에러코드(신용카드:승인거절시), 은행코드(가상계좌,계좌이체)
	// ordno : 주문번호
	// isscd : 발급사코드(신용카드), 가상계좌번호(가상계좌) ,기타결제수단의 경우 의미없음
	// aqucd : 매입사코드(신용카드)
	// result : 승인구분

	String payKey;
	String rparams;
	String mtype;

	String[] rnames = null;
	HashMap rvHash = null;

	public KSPayWebHostMobileBean(String _payKey) {
		this.payKey		= _payKey;
		this.rparams	= DEFAULT_RPARAMS;
		
		init();
	}

	public KSPayWebHostMobileBean(String _payKey ,String _rparams) {
		this.payKey		= _payKey;
		this.rparams	= _rparams;
		
		init();
	}

	private void init() {
		this.rnames = split(this.rparams, DEFAULT_DELIM);
		this.rvHash = new HashMap();
	}

	public String getValue(String pname) {
		if (pname == null || this.rnames == null)
			return null;
		return (String) rvHash.get(pname);
	}

	public boolean send_msg(String _mtype) {
		this.mtype = _mtype;
		String rmsg = send_url();
		if (rmsg.indexOf('`') != -1) {
			String[] tmpvals = split(rmsg, DEFAULT_DELIM);
			if (this.rnames.length < tmpvals.length) {
				for (int i = 0; i < this.rnames.length; i++)
					rvHash.put(this.rnames[i], tmpvals[i + 1]);
				return true;
			}
		}
		return false;
	}

	public String send_url() {
		URLConnection httpConn = null;
		BufferedReader brd = null;
		PrintStream pout = null;

		StringBuffer sb = new StringBuffer();
		try {
			String post_msg = sb.append("sndCommConId=").append(this.payKey).append("&sndActionType=")
					.append(this.mtype).append("&sndRpyParams=").append(URLEncoder.encode(this.rparams, "euc-kr"))
					.toString();
			sb.setLength(0);

			httpConn = new URL(KSPAY_WEBHOST_URL).openConnection();
			httpConn.setDoOutput(true);
			httpConn.setUseCaches(false);
			pout = new PrintStream(httpConn.getOutputStream(), false, "euc-kr");
			pout.print(post_msg);
			pout.flush();

			brd = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "euc-kr"));

			String tmpStr = null;
			while ((tmpStr = brd.readLine()) != null) {
				sb.append(tmpStr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (pout != null)
					pout.close();
				pout = null;
			} catch (Exception e) {
			}
			;
			try {
				if (brd != null)
					brd.close();
				brd = null;
			} catch (Exception e) {
			}
			;
			httpConn = null;
		}

		return sb.toString();
	}

	public static String[] split(String srcStr, char c1) {
		return split(srcStr, String.valueOf(c1));
	}

	public static String[] split(String srcStr, String str1) {
		if (srcStr == null)
			return new String[0];

		String[] tokenArr = null;
		if (srcStr.indexOf(str1) == -1) {
			tokenArr = new String[1];
			tokenArr[0] = srcStr;

			return tokenArr;
		}

		LinkedList linkedlist = new LinkedList();

		int srcLength = srcStr.length();
		int tockenLength = str1.length();

		int pos = 0, startPos = 0;
		while (startPos < srcLength) {
			pos = srcStr.indexOf(str1, startPos);

			if (-1 == pos)
				break;

			linkedlist.add(srcStr.substring(startPos, pos));
			startPos = pos + tockenLength;
		}

		if (startPos <= srcLength)
			linkedlist.add(srcStr.substring(startPos));

		return (String[]) linkedlist.toArray(new String[0]);
	}// split
}