package kr.co.sunpay.api.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.sunpay.api.domain.KsnetPayResult;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.model.PaymentItem;
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
		
		List<KsnetPayResult> payList = ksnetPayResultRepo.findByStoreIdAndtrddtAndserviceTypeCd(storeIds, sDate, eDate, serviceTypeCodes);
		
		List<PaymentItem> list = new ArrayList<PaymentItem>(); 
		
		payList.forEach(pay -> {
			if (!paymethods.contains(pay.getKsnetPay().getSndPaymethod())) {
				return;
			}
			
			PaymentItem item = new PaymentItem();
			
			item.setPaidDate(pay.getTrddt() + pay.getTrdtm());
			item.setAmount(pay.getAmt());
			item.setServiceTypeCode(pay.getServiceTypeCd());
			item.setTrNo(pay.getTrno());
			item.setPaymethodCode(pay.getKsnetPay().getSndPaymethod());
			item.setGoodsName(pay.getKsnetPay().getSndGoodname());
			item.setOrderName(pay.getKsnetPay().getSndOrdername());
			item.setOrderNo(pay.getKsnetPay().getSndOrdernumber());
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
			
			item.setSndMobile(pay.getKsnetPay().getSndMobile());// 구매자 연락처(SP_KSNET_PAY.mobile)
			item.setHalbu(pay.getHalbu());// 할부(SP_KSNET_PAY_RESULT.INSTALMENT, halbu)
			item.setMsg1(pay.getMsg1());// 발급사명(SP_KSNET_PAY_RESULT.MSG1)
			//			
			
			list.add(item);
		});
	
		return list;
	}
}
