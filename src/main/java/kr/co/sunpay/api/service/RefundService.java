package kr.co.sunpay.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.sunpay.api.domain.KsnetPay;
import kr.co.sunpay.api.domain.KsnetPayResult;
import kr.co.sunpay.api.domain.KsnetRefundLog;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.domain.StoreId;
import kr.co.sunpay.api.model.PaymentItem;
import kr.co.sunpay.api.model.RefundItemResponse;
import kr.co.sunpay.api.repository.KsnetRefundLogRepository;
import kr.co.sunpay.api.util.Sunpay;
import lombok.extern.java.Log;

@Log
@Service
public class RefundService extends StoreService {
	
	public final static String REFUND_STATUS_COMPLETED = "COMPLETED";
	
	@Autowired
	KsnetRefundLogRepository ksnetRefundLogRepo;
	
	@Autowired
	CodeService codeService;
	
	@Autowired
	MemberService memberService;	

	public List<RefundItemResponse> getRefundItems(List<Store> stores, LocalDateTime sDateTime, LocalDateTime eDateTime, List<String> paymethodCodes, List<String> serviceTypeCodes) {
		
		List<String> storeIds = new ArrayList<String>();	
		for (Store store : stores) {
			store.getStoreIds().forEach(storeId -> {
				storeIds.add(storeId.getId());
			});			
		}	
		
		// 정산타입이 없는 경우
		if (Sunpay.isEmpty(serviceTypeCodes)) {
			return getRefundItemsByPaymethods(storeIds, sDateTime, eDateTime, paymethodCodes);
		}
		
		// 결제수단이 없는 경우
		if (Sunpay.isEmpty(paymethodCodes)) {
			return getRefundItemsByServiceTypeCodes(storeIds, sDateTime, eDateTime, serviceTypeCodes);
		}						
		
		return getRefundItemsByAll(storeIds, sDateTime, eDateTime, paymethodCodes, serviceTypeCodes);
	}
	
	/**
	 * 상점ID + 결제수단 + 정산타입으로 환불건 검색
	 * @param storeIds
	 * @param sDateTime
	 * @param eDateTime
	 * @param paymethods
	 * @param serviceTypeCodes
	 * @return
	 */
	public List<RefundItemResponse> getRefundItemsByAll(List<String> storeIds, LocalDateTime sDateTime,
			LocalDateTime eDateTime, List<String> paymethodCodes, List<String> serviceTypeCodes) {
		
		List<KsnetRefundLog> refundList=
				ksnetRefundLogRepo.findByStoreIdInAndCreatedDateBetweenAndStatusCodeAndServiceTypeCodeInAndPaymethodCodeInOrderByCreatedDateDesc(storeIds,
				sDateTime, eDateTime, REFUND_STATUS_COMPLETED, serviceTypeCodes, paymethodCodes);
		
		List<RefundItemResponse> list = new ArrayList<RefundItemResponse>(); 
		
		refundList.forEach(refund -> {					
			RefundItemResponse response = new RefundItemResponse(refund);

			//추가 작업1
			Store store=storeService.getStoreByStoreId(refund.getStoreId());
			String groupRoleName =codeService.getCodeMap("GROUP_ROLE").get(store.getGroup().getRoleCode());
			response.setGroupRoleName(groupRoleName);
			response.setGroupBizName(store.getGroup().getBizName());
			response.setStoreBizeName(store.getBizName());
			
			//추가 작업2
			response.setCbtrno(refund.getKsnetPayResult().getCbtrno());//영수증 번호(신용카드),cbtrno
			response.setAuthno(refund.getKsnetPayResult().getAuthno());//승인번호(신용카드)
			response.setBizOwner(store.getBizOwner());// 소유자 이름
			
			List<Member> members=store.getMembers();
			Member ownerMember=memberService.getOwnerMember(members);
			// 문제있는 "테스트 데이타" 가 존재하는  store 와  관계있는  데이트는 가져 오지 않는다.
			if(ownerMember==null) {
				return;
			}

			response.setOwnerMemberId(ownerMember.getId()); // owner 권한을 갖는 상점 멤버의  아이디 
			response.setBizContact(store.getBizContact());// 사업장 연락처
			
			response.setSndMobile(refund.getKsnetPayResult().getKsnetPay().getSndMobile());// 구매자 연락처(SP_KSNET_PAY.mobile)
			response.setHalbu(refund.getKsnetPayResult().getHalbu());// 할부(SP_KSNET_PAY_RESULT.INSTALMENT, halbu)
			response.setMsg1(refund.getKsnetPayResult().getMsg1());// 발급사명(SP_KSNET_PAY_RESULT.MSG1)
			// fee 계산			
			putProfitInto(response, refund);
			//
			list.add(response);
		});
		
		return list;
	}
	
	private void putProfitInto(RefundItemResponse response, KsnetRefundLog refund) {							
		
		KsnetPayResult ksnetPayResult=refund.getKsnetPayResult();
		//Integer profitStore=ksnetPayResult.getProfitStore();
		//Integer totalTransFee=ksnetPayResult.getTotalTransFee();
		
		int profitPg=0;
		int profitHead=0;
		int profitBranch=0;
		int profitAgency=0;
		int profitStore=0; // 환불은 상점정산 금액을 0으로 한다.
		int vatTotalTransFee=0; 
		int depositDeduction=0; //일반결제(D+2)시 는 예치금을 차감하지 않는다. SERVICE_TYPE_D2 = "D2"
		
		if(StoreService.SERVICE_TYPE_INSTANT.equals(ksnetPayResult.getServiceTypeCd())) {//순간거래			
			profitPg=ksnetPayResult.getProfitPg();
			profitHead=ksnetPayResult.getProfitHead();
			profitBranch=ksnetPayResult.getProfitBranch();
			profitAgency=ksnetPayResult.getProfitAgency();				
			profitStore=ksnetPayResult.getProfitStore();	
			int totalTransFee=ksnetPayResult.getTotalTransFee();
			vatTotalTransFee=(int)((totalTransFee)*1.1);
			depositDeduction= profitStore + vatTotalTransFee;
		} else { //일반거래
			
		}
		response.setProfitPg(profitPg);		
		response.setProfitHead(profitHead);
		response.setProfitBranch(profitBranch);
		response.setProfitAgency(profitAgency);		
		response.setProfitStore(profitStore);
		response.setStoreDeductionn(depositDeduction);
		response.setDepositDeduction(depositDeduction);	
	}		
	
	/**
	 * 상점ID + 정산타입으로 환불건 검색
	 * @param storeIds
	 * @param sDateTime
	 * @param eDateTime
	 * @param serviceTypeCodes
	 * @return
	 */
	public List<RefundItemResponse> getRefundItemsByServiceTypeCodes(List<String> storeIds, LocalDateTime sDateTime, LocalDateTime eDateTime, List<String> serviceTypeCodes) {
		
		return ksnetRefundLogRepo
				.findByStoreIdInAndCreatedDateBetweenAndStatusCodeAndServiceTypeCodeInOrderByCreatedDateDesc(storeIds,
						sDateTime, eDateTime, REFUND_STATUS_COMPLETED, serviceTypeCodes)
				.stream().map(RefundItemResponse::new).collect(Collectors.toList());
	}
	
	/**
	 * 상점ID + 결제수단으로 환불건 검색
	 * @param storeIds
	 * @param sDateTime
	 * @param eDateTime
	 * @param paymethods
	 * @return
	 */
	public List<RefundItemResponse> getRefundItemsByPaymethods(List<String> storeIds, LocalDateTime sDateTime, LocalDateTime eDateTime, List<String> paymethodCodes) {
		
		if (Sunpay.isEmpty(paymethodCodes)) {
			return getRefundItems(storeIds, sDateTime, eDateTime);
		}
		
		return ksnetRefundLogRepo
				.findByStoreIdInAndCreatedDateBetweenAndStatusCodeAndPaymethodCodeInOrderByCreatedDateDesc(storeIds,
						sDateTime, eDateTime, REFUND_STATUS_COMPLETED, paymethodCodes)
				.stream().map(RefundItemResponse::new).collect(Collectors.toList());
	}
	
	/**
	 * 상점ID로 환불건 검색
	 * @param storeIds
	 * @param sDateTime
	 * @param eDateTime
	 * @return
	 */
	public List<RefundItemResponse> getRefundItems(List<String> storeIds, LocalDateTime sDateTime,
			LocalDateTime eDateTime) {
		
		return ksnetRefundLogRepo
				.findByStoreIdInAndCreatedDateBetweenAndStatusCodeOrderByCreatedDateDesc(storeIds,
						sDateTime, eDateTime, REFUND_STATUS_COMPLETED)
				.stream().map(RefundItemResponse::new).collect(Collectors.toList());
	}
}