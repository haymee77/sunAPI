package kr.co.sunpay.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import kr.co.sunpay.api.domain.KsnetRefundLog;

public interface KsnetRefundLogRepository extends CrudRepository<KsnetRefundLog, Integer> {

	Optional<KsnetRefundLog> findByTrNoAndRStatus(String trNo, String rStatus);

	List<KsnetRefundLog> findByTrNoAndStatusCodeNot(String trNo, String statusCode);

	/**
	 * 상점ID + 기간 + 환불상태로 검색
	 * @param storeIds
	 * @param createdDateStart
	 * @param createdDateEnd
	 * @param statusCode
	 * @return
	 */
	List<KsnetRefundLog> findByStoreIdInAndCreatedDateBetweenAndStatusCodeOrderByCreatedDateDesc(List<String> storeIds,
			LocalDateTime createdDateStart, LocalDateTime createdDateEnd, String statusCode);
	
	/**
	 * 상점ID + 기간 + 환불상태 + 정산타입으로 검색
	 * @param storeIds
	 * @param createdDateStart
	 * @param createdDateEnd
	 * @param statusCode
	 * @param serviceTypeCodes
	 * @return
	 */
	List<KsnetRefundLog> findByStoreIdInAndCreatedDateBetweenAndStatusCodeAndServiceTypeCodeInOrderByCreatedDateDesc(
			List<String> storeIds, LocalDateTime createdDateStart, LocalDateTime createdDateEnd, String statusCode,
			List<String> serviceTypeCodes);
	
	/**
	 * 상점ID + 기간 + 환불상태 + 결제수단으로 검색
	 * @param storeIds
	 * @param createdDateStart
	 * @param createdDateEnd
	 * @param statusCode
	 * @param serviceTypeCodes
	 * @param paymethodCodes
	 * @return
	 */
	List<KsnetRefundLog> findByStoreIdInAndCreatedDateBetweenAndStatusCodeAndPaymethodCodeInOrderByCreatedDateDesc(
			List<String> storeIds, LocalDateTime createdDateStart, LocalDateTime createdDateEnd, String statusCode,
			List<String> paymethodCodes);
	
	/**
	 * 상점ID + 기간 + 환불상태 + 정산타입 + 결제수단으로 검색
	 * @param storeIds
	 * @param createdDateStart
	 * @param createdDateEnd
	 * @param statusCode
	 * @param serviceTypeCodes
	 * @param paymethodCodes
	 * @return
	 */
	List<KsnetRefundLog> findByStoreIdInAndCreatedDateBetweenAndStatusCodeAndServiceTypeCodeInAndPaymethodCodeInOrderByCreatedDateDesc(
			List<String> storeIds, LocalDateTime createdDateStart, LocalDateTime createdDateEnd, String statusCode,
			List<String> serviceTypeCodes, List<String> paymethodCodes);
}
