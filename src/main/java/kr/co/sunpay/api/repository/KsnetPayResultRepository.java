package kr.co.sunpay.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kr.co.sunpay.api.domain.KsnetPayResult;

@Repository
public interface KsnetPayResultRepository extends JpaRepository<KsnetPayResult, Integer> {

	/**
	 * GET /payment/{memberUid}/{storeUid} 호출 시 사용됨
	 * @param storeIds
	 * @param trdStartDt
	 * @param trdEndDt
	 * @param serviceTypeCodes
	 * @return
	 */
	// 거래취소건과 결제실패건은 나오지 말하야 한다.
	@Query(value=
			  "SELECT * FROM SP_KSNET_PAY_RESULT "
			+ "WHERE STORE_ID IN :storeIds "
			+ "AND TRD_DT >= :trdStartDt "
			+ "AND TRD_DT <= :trdEndDt "
			+ "AND SERVICE_TYPE_CD IN :serviceTypeCodes "
			+ "AND AUTH_YN = :authyn "
			+ "AND UID NOT IN ( "
			+ "	SELECT DISTINCT(P.UID) "
			+"	FROM SP_KSNET_PAY_RESULT P, SP_KSNET_REFUND_LOGS R " 
			+"	WHERE P.UID=R.KSNET_PAY_RESULT_UID_FK " 
			+"	AND R.STATUS_CD = 'COMPLETED'" 
			+ ") "
			+ "ORDER BY CREATED_DT DESC", nativeQuery=true)
	List<KsnetPayResult> findByStoreIdAndtrddtAndserviceTypeCdAndAuthyn(
			@Param("storeIds") List<String> storeIds, 
			@Param("trdStartDt") String trdStartDt, 
			@Param("trdEndDt") String trdEndDt, 
			@Param("serviceTypeCodes") List<String> serviceTypeCodes,
			@Param("authyn") String authyn
			);
	
	Optional<KsnetPayResult> findByTrnoAndStoreIdAndAuthyn(String trno, String storeId, String Authyn);
	Optional<KsnetPayResult> findByTrnoAndAuthyn(String trno, String Authyn);
}
