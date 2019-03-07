package kr.co.sunpay.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.sunpay.api.domain.KsnetPayResult;
import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.model.PaymentItem;
import kr.co.sunpay.api.repository.KsnetPayResultRepository;

@Service
public class PaymentService {
	
	@Autowired
	KsnetPayResultRepository ksnetPayResultRepo;
	
	/**
	 * 상점별 결제데이터 검색, 리스트 리턴
	 * @param storeUid
	 * @param sDate
	 * @param endDate
	 * @param paymethods
	 * @param serviceTypeCodes
	 * @return
	 */
	public List<PaymentItem> getPaymentItems(Store store, String sDate, String eDate, List<String> paymethods, List<String> serviceTypeCodes) {
		
		List<String> storeIds = new ArrayList<String>();
		store.getStoreIds().forEach(storeId -> {
			storeIds.add(storeId.getId());
		});
		
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
			
			list.add(item);
		});

		return list;
	}
}
