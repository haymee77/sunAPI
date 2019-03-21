package kr.co.sunpay.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.domain.StoreId;
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

	public List<RefundItemResponse> getRefundItems(int storeUid, LocalDateTime sDateTime, LocalDateTime eDateTime, List<String> paymethodCodes, List<String> serviceTypeCodes) {
		
		Store store = getStore(storeUid);
		if (Sunpay.isEmpty(store)) return null;
		
		List<String> storeIds = new ArrayList<String>();
		
		// 환불정보는 상점의 상점ID로 저장되므로 선택된 상점의 모든 상점 ID 가져옴
		for (StoreId id : store.getStoreIds()) {
			storeIds.add(id.getId());
		}
		
		// 정산타입이 없는 경우
		if (Sunpay.isEmpty(serviceTypeCodes)) {
			return getRefundItemsByPaymethods(storeIds, sDateTime, eDateTime, paymethodCodes);
		}
		
		// 결제수단이 없는 경우
		if (Sunpay.isEmpty(paymethodCodes)) {
			return getRefundItemsByServiceTypeCodes(storeIds, sDateTime, eDateTime, serviceTypeCodes);
		}
			
		return getRefundItems(storeIds, sDateTime, eDateTime, paymethodCodes, serviceTypeCodes);
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
	public List<RefundItemResponse> getRefundItems(List<String> storeIds, LocalDateTime sDateTime,
			LocalDateTime eDateTime, List<String> paymethodCodes, List<String> serviceTypeCodes) {

		return ksnetRefundLogRepo
				.findByStoreIdInAndCreatedDateBetweenAndStatusCodeAndServiceTypeCodeInAndPaymethodCodeInOrderByCreatedDateDesc(storeIds,
						sDateTime, eDateTime, REFUND_STATUS_COMPLETED, serviceTypeCodes, paymethodCodes)
				.stream().map(RefundItemResponse::new).collect(Collectors.toList());
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