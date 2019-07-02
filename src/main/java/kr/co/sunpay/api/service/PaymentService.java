package kr.co.sunpay.api.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.sunpay.api.domain.KsnetPayResult;
import kr.co.sunpay.api.domain.KsnetRefundLog;
import kr.co.sunpay.api.domain.KsnetPay;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.model.PaymentItem;
import kr.co.sunpay.api.model.RefundItemResponse;
import kr.co.sunpay.api.repository.KsnetPayResultRepository;

@Service
public class PaymentService {
	
	@Autowired
	KsnetPayResultRepository ksnetPayResultRepo;
	
	@Autowired
	StoreService storeService;
	
	@Autowired
	CodeService codeService;
	
	@Autowired
	MemberService memberService;
	
	
	
	/**
	 * 상점별 결제데이터 검색, 리스트 리턴
	 * @param storeUid
	 * @param sDate
	 * @param endDate
	 * @param paymethods
	 * @param serviceTypeCodes
	 * @return
	 */
	public List<PaymentItem> getPaymentItems(List<Store> stores, String sDate, String eDate, List<String> paymethods, List<String> serviceTypeCodes) {				

		List<String> storeIds = new ArrayList<String>();	
		for (Store store : stores) {
			store.getStoreIds().forEach(storeId -> {
				storeIds.add(storeId.getId());
			});			
		}	
		
		List<KsnetPayResult> payList = ksnetPayResultRepo.findByStoreIdAndtrddtAndserviceTypeCdAndAuthyn(storeIds, sDate, eDate, serviceTypeCodes, "O");
		
		List<PaymentItem> list = new ArrayList<PaymentItem>(); 
		
		payList.forEach(pay -> {
			KsnetPay ksnetPay=pay.getKsnetPay();
			if (!paymethods.contains(ksnetPay.getSndPaymethod())) {
				return;
			}
			
			PaymentItem item = new PaymentItem();
			
			item.setPaidDate(pay.getTrddt() +" "+ pay.getTrdtm());
			//item.setPaidDate(pay.getCreatedDate());
			item.setAmount(pay.getAmt());
			item.setServiceTypeCode(pay.getServiceTypeCd());
			item.setTrNo(pay.getTrno());
			item.setPaymethodCode(ksnetPay.getSndPaymethod());
			item.setGoodsName(ksnetPay.getSndGoodname());
			item.setOrderName(ksnetPay.getSndOrdername());
			item.setOrderNo(ksnetPay.getSndOrdernumber());
			//추가 작업1
			Store store=storeService.getStoreByStoreId(pay.getStoreId());
			String groupRoleName =codeService.getCodeMap("GROUP_ROLE").get(store.getGroup().getRoleCode());
			item.setGroupRoleName(groupRoleName);
			item.setGroupBizName(store.getGroup().getBizName());
			item.setStoreBizeName(store.getBizName());
			
			//추가 작업2
			item.setCbtrno(pay.getCbtrno());//영수증 번호(신용카드),cbtrno
			item.setAuthno(pay.getAuthno());//승인번호(신용카드)
			item.setBizOwner(store.getBizOwner());// 소유자 이름
			
			List<Member> members=store.getMembers();
			Member ownerMember=memberService.getOwnerMember(members);
			// 문제있는 "테스트 데이타" 가 존재하는  store 는  가져 오지 않는다.
			if(ownerMember==null) {
				return;
			}
	
			item.setOwnerMemberId(ownerMember.getId()); // owner 권한을 갖는 상점 멤버의  아이디 
			item.setBizContact(store.getBizContact());// 사업장 연락처
			
			item.setSndMobile(ksnetPay.getSndMobile());// 구매자 연락처(SP_KSNET_PAY.mobile)
			item.setHalbu(pay.getHalbu());// 할부(SP_KSNET_PAY_RESULT.INSTALMENT, halbu)
			item.setMsg1(pay.getMsg1());// 발급사명(SP_KSNET_PAY_RESULT.MSG1)
			// fee 계산
			//putProfitInto(item, pay);
			item.setProfitPg(pay.getProfitPg());		
			item.setProfitHead(pay.getProfitHead());
			item.setProfitBranch(pay.getProfitBranch());
			item.setProfitAgency(pay.getProfitAgency());
			item.setProfitStore(pay.getProfitStore());
			
			item.setKsnetPay(ksnetPay);  //단지 fee 에 대한 %등 검증을 위해 필요(browser console.log 로 볼 예정 )			
			//
			updateWithRefundInfo(item, pay);
			
			list.add(item);			
		});
		return list;
	}
	private void updateWithRefundInfo(PaymentItem item, KsnetPayResult pay) {
		List<KsnetRefundLog> refundList=pay.getKsnetRefundLogs();
		for (Iterator<KsnetRefundLog> iterator = refundList.iterator(); iterator.hasNext();) {
			KsnetRefundLog refund = iterator.next();
			if(RefundService.REFUND_STATUS_COMPLETED.equals(refund.getStatusCode())) {
				putRefundProfitInto(item, refund);
				break;
			}						
		}					
	}	
	/*거래가 일어난(result) 모든 값을 가져와서(asis query), 그중 환불완료 된 것이 있는 거래번호는 
	 1.취소처리(취소완료:날짜), 2. 상점정산금액(0으로 한다), 3.상점차감액, 4.취소예치금차감액 을 환불내역 테이블의 것으로 교체한다.*/
	private void putRefundProfitInto(PaymentItem item, KsnetRefundLog refund) {
		
		KsnetPayResult ksnetPayResult=refund.getKsnetPayResult();
		
		int profitPg=0;
		int profitHead=0;
		int profitBranch=0;
		int profitAgency=0;
		int beforeRefundProfitStore=0; // 매출시 상점 정산액
		int vatTotalTransFee=0; 
		int depositDeduction=0; //일반결제(D+2)시 는 예치금을 차감하지 않는다. SERVICE_TYPE_D2 = "D2"
		
		if(StoreService.SERVICE_TYPE_INSTANT.equals(ksnetPayResult.getServiceTypeCd())) {//순간거래			
			profitPg=ksnetPayResult.getProfitPg();
			profitHead=ksnetPayResult.getProfitHead();
			profitBranch=ksnetPayResult.getProfitBranch();
			profitAgency=ksnetPayResult.getProfitAgency();				
			beforeRefundProfitStore=ksnetPayResult.getProfitStore();	
			int totalTransFee=ksnetPayResult.getTotalTransFee();
			vatTotalTransFee=(int)((totalTransFee)*1.1);
			depositDeduction= beforeRefundProfitStore + vatTotalTransFee;
		} else { //일반거래
			
		}
		item.setProfitPg(profitPg);		
		item.setProfitHead(profitHead);
		item.setProfitBranch(profitBranch);
		item.setProfitAgency(profitAgency);		
		item.setProfitStore(-vatTotalTransFee);
		
		item.setBeforeRefundProfitStore(beforeRefundProfitStore);//환불전 상점정산액
		//item.setStoreDeduction(vatTotalTransFee);//송금수수료
		item.setDepositDeduction(depositDeduction);		
		item.setRefundDateTime(refund.getCreatedDate());	
	}	
	/*
	private void putProfitInto(PaymentItem item, KsnetPayResult pay) {							
		int profitPg=0 ; 
		int profitHead=0 ;
		int profitBranch=0 ;
		int profitAgency=0 ;
		int profitStore=0 ;
		
		KsnetPay ksnetPay=pay.getKsnetPay();
		int amt=pay.getAmt();
		
		if(StoreService.SERVICE_TYPE_INSTANT.equals(pay.getServiceTypeCd())) {//순간거래			
			int profitPg0=(int)( amt*ksnetPay.getInstantFeePg() );
			int transFeePg=ksnetPay.getTransFeePg();
			profitPg= profitPg0+transFeePg;
			
			int profitHead0=(int)( amt*ksnetPay.getInstantFeeHead() );
			int transFeeHead=ksnetPay.getTransFeeHead();
			profitHead= profitHead0+transFeeHead; //본사수익 : 매출액*본사순간정산수수료 + 본사순간송금수수료, 소숫점 이하 버림
			
			int profitBranch0= (int)( amt*ksnetPay.getInstantFeeBranch() );; 
			int transFeeBranch=ksnetPay.getTransFeeBranch();
			profitBranch= profitBranch0+transFeeBranch;; 
			
			int profitAgency0= (int)( amt*ksnetPay.getInstantFeeAgency() );; 
			int transFeeAgency=ksnetPay.getTransFeeAgency();
			profitAgency= profitAgency0+transFeeAgency; 
			
			int profit0= profitPg0+profitHead0+profitBranch0+profitAgency0;
			int transFee= transFeePg+transFeeHead+transFeeBranch+transFeeAgency;
			profitStore= (int)( amt-(profit0)*1.1-transFee*1.1 ); //상점 정산금액 : 매출액 – ( 매출액 *(순간정산수수료)*1.1) – 순간송금수수료*1.1		
		}else { //일반거래
			int profitPg0=(int)( amt*ksnetPay.getInstantFeePg() );
			//int transFeePg=ksnetPay.getTransFeePg();
			profitPg= profitPg0;
			
			int profitHead0=(int)( amt*ksnetPay.getInstantFeeHead() );
			//int transFeeHead=ksnetPay.getTransFeeHead();
			profitHead= profitHead0; //본사수익 : 매출액*본사순간정산수수료 , 소숫점 이하 버림
			
			int profitBranch0= (int)( amt*ksnetPay.getInstantFeeBranch() );
			//int transFeeBranch=ksnetPay.getTransFeeBranch();
			profitBranch= profitBranch0;
			
			int profitAgency0= (int)( amt*ksnetPay.getInstantFeeAgency() );
			//int transFeeAgency=ksnetPay.getTransFeeAgency();
			profitAgency= profitAgency0;
			
			int profit0= profitPg0+profitHead0+profitBranch0+profitAgency0;
			//int transFee= transFeePg+transFeeHead+transFeeBranch+transFeeAgency;
			profitStore= (int)( amt-(profit0)*1.1 ); //상점 정산금액 : 매출액 – ( 매출액 *(일반정산수수료)*1.1)				
		}
		
		item.setProfitPg(profitPg);		
		item.setProfitHead(profitHead);
		item.setProfitBranch(profitBranch);
		item.setProfitAgency(profitAgency);
		item.setProfitStore(profitStore);
	}	*/
	
}
