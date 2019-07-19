package kr.co.sunpay.api.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import kr.co.sunpay.api.domain.Code;
import kr.co.sunpay.api.domain.Group;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.domain.StoreId;
import kr.co.sunpay.api.model.MemberRequest;
import kr.co.sunpay.api.model.PaymentItem;
import kr.co.sunpay.api.model.StoreRequest;
import kr.co.sunpay.api.model.api.IS001Request;
import kr.co.sunpay.api.model.api.IS001Response;
import kr.co.sunpay.api.model.api.IS002Request;
import kr.co.sunpay.api.model.api.IS002Response;
import kr.co.sunpay.api.model.api.IS003Request;
import kr.co.sunpay.api.model.api.IS003Response;
import kr.co.sunpay.api.model.api.IS004Request;
import kr.co.sunpay.api.model.api.IS004Response;
import kr.co.sunpay.api.model.api.IS005Request;
import kr.co.sunpay.api.model.api.IS005Response;
import kr.co.sunpay.api.model.api.IS006Request;
import kr.co.sunpay.api.model.api.IS006Response;
import kr.co.sunpay.api.model.api.MemberResponse;
import kr.co.sunpay.api.model.api.PaymentListItem;
import kr.co.sunpay.api.model.api.SettleListItem;
import kr.co.sunpay.api.service.CodeService;
import kr.co.sunpay.api.service.MemberService;
import kr.co.sunpay.api.service.PaymentService;
import kr.co.sunpay.api.service.StoreService;
import lombok.extern.java.Log;

/**
 * Open API
 */
@Log
@RestController
@RequestMapping("/api")
public class ApiController {
	
	@Autowired
	MemberService memberService;
	
	@Autowired
	StoreService storeService;
	
	@Autowired
	PaymentService paymentService;
	
	@Autowired
	CodeService codeService;
	
	/**
	 * 회원생성 승인 요청 (2019-06-21:JAEROX)
	 * 
	 * @param uid
	 * @return
	 */
	@PostMapping("/member/{uid}")
	@ApiOperation(value="회원생성 승인 요청", notes="{uid} 회원 승인 처리")
	public MemberResponse confirmMember(@ApiParam("회원 UID") @PathVariable int uid) {
		MemberResponse response = new MemberResponse();
		response.setSuccess(false);
		response.setMessage("");
		response.setUid(uid);
		
		Member member = memberService.getMember(uid);
		int memberGroupUid = member.getGroup().getUid();
		double fee = member.getApiFee();
		int transFee = member.getApiTransFee();
		
		MemberRequest memberRequest = new MemberRequest();
		
		StoreRequest storeRequest = new StoreRequest();
		storeRequest.setMemberReq(memberRequest);
		
		// storeRequest의 groupUid에 대리점의 groupUid를 삽입
		storeRequest.setGroupUid(memberGroupUid);
		
		storeRequest.setBizName(member.getApiBizNm());
		storeRequest.setBizTypeCode(member.getApiBizTypeCd());
		
		storeRequest.setFeeAgency(fee);
		storeRequest.setTransFeeAgency(transFee);
		
		
		Store insertedStore = new Store();
		try {
			insertedStore = storeService.registApiStore(storeRequest);
		} catch (IllegalArgumentException e) {
			response.setMessage(e.getLocalizedMessage());
			
			return response;
		} catch (DuplicateKeyException e) {
			response.setMessage(e.getLocalizedMessage());
			
			return response;
		}
		
		// 생성된 상점 고유 번호
		int storeUid = insertedStore.getUid();
		
		// 업데이트 대상 회원 정보
		Member updatedMember = new Member();
		try {
			updatedMember = memberService.confirmMember(uid, storeUid);
		} catch (EntityNotFoundException e) {
			response.setMessage("회원UID를 확인해 주세요.");
			
			return response;
		}
		
		response.setSuccess(true);
		response.setMessage("성공");
		response.setUid(updatedMember.getUid());
		
		return response;
	}
	
	/**
	 * ID 유무 검사 (2019-06-21:JAEROX)
	 * 회원 ID에 대한 중복 검사를 시행한다.
	 * 
	 * @param IS001Request
	 * @return
	 */
	@PostMapping("/IS001")
	@ApiOperation(value="ID 유무 검사", notes="회원 ID에 대한 중복 검사를 시행한다.")
	public ResponseEntity<Object> IS001(@RequestBody @Valid IS001Request IS001Request) {
		log.info("-- ApiController.IS001 called..");
		
		IS001Response IS001Response = new IS001Response();
		IS001Response.setSuccess(false);
		
		// 요청값 체크
		if (IS001Request == null) {
			IS001Response.setMessage("요청 데이터 오류");
			log.info("-- return: " + IS001Response.toString());
			
			return new ResponseEntity<>(IS001Response, HttpStatus.BAD_REQUEST);
		}
		
		// 요청한 회원 ID
		IS001Response.setMemberId(IS001Request.getSearchId());
		
		if (StringUtils.isEmpty(IS001Request.getSunKey())) {
			IS001Response.setMessage("SUN-KEY 요청 오류");
			log.info("-- return: " + IS001Response.toString());
			
			return new ResponseEntity<>(IS001Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS001Request.getBranchId())) {
			IS001Response.setMessage("지사ID 요청 오류");
			log.info("-- return: " + IS001Response.toString());
			
			return new ResponseEntity<>(IS001Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS001Request.getAgencyId())) {
			IS001Response.setMessage("대리점ID 요청 오류");
			log.info("-- return: " + IS001Response.toString());
			
			return new ResponseEntity<>(IS001Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS001Request.getAdminId())) {
			IS001Response.setMessage("관리자ID 요청 오류");
			log.info("-- return: " + IS001Response.toString());
			
			return new ResponseEntity<>(IS001Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS001Request.getSearchId())) {
			IS001Response.setMessage("조회ID 요청 오류");
			log.info("-- return: " + IS001Response.toString());
			
			return new ResponseEntity<>(IS001Response, HttpStatus.BAD_REQUEST);
		}
		
		
		// 대리점 회원 정보
		Member agencyMember = null;
		try {
			agencyMember = memberService.getMember(IS001Request.getAgencyId());
		} catch (NoSuchElementException e) {
			IS001Response.setMessage("대리점 정보 오류 ");
			log.info("-- return: " + IS001Response.toString());
			
			return new ResponseEntity<>(IS001Response, HttpStatus.OK);
		}
		
		// 대리점이 속해 있는 그룹 정보
		Group agencyGroup = agencyMember.getGroup();
		if (agencyGroup == null) {
			IS001Response.setMessage("대리점 그룹 정보 오류 ");
			log.info("-- return: " + IS001Response.toString());
			
			return new ResponseEntity<>(IS001Response, HttpStatus.OK);
		}
		
		// 연동키
		String privateKey = agencyGroup.getApiAgencyPrivateKey();
		// 연동키가 일치하지 않으면
		if (StringUtils.equals(privateKey, IS001Request.getSunKey()) == false) {
			IS001Response.setMessage("연동키 오류 ");
			log.info("-- return: " + IS001Response.toString());
			
			return new ResponseEntity<>(IS001Response, HttpStatus.OK);
		}
		
		
		// 지사 회원 정보
		Member branchMember = null;
		try {
			branchMember = memberService.getMember(IS001Request.getBranchId());
		} catch (NoSuchElementException e) {
			IS001Response.setMessage("지사 정보 오류 ");
			log.info("-- return: " + IS001Response.toString());
			
			return new ResponseEntity<>(IS001Response, HttpStatus.OK);
		}
		
		// 지사가 속해 있는 그룹 정보
		Group branchGroup = branchMember.getGroup();
		if (branchGroup == null) {
			IS001Response.setMessage("지사 그룹 정보 오류 ");
			log.info("-- return: " + IS001Response.toString());
			
			return new ResponseEntity<>(IS001Response, HttpStatus.OK);
		}
		
		// 지사-대리점 관계가 아니면
		if (branchGroup.getUid() != agencyGroup.getParentGroupUid()) {
			IS001Response.setMessage("상위 그룹 정보 오류 ");
			log.info("-- return: " + IS001Response.toString());
			
			return new ResponseEntity<>(IS001Response, HttpStatus.OK);
		}
		
		
		// 관리자 회원 정보
		Member adminMember = null;
		try {
			adminMember = memberService.getMember(IS001Request.getAdminId());
		} catch (NoSuchElementException e) {
			IS001Response.setMessage("관리자 정보 오류 ");
			log.info("-- return: " + IS001Response.toString());
			
			return new ResponseEntity<>(IS001Response, HttpStatus.OK);
		}
		
		if (adminMember == null) {
			IS001Response.setMessage("관리자 정보 오류 ");
			log.info("-- return: " + IS001Response.toString());
			
			return new ResponseEntity<>(IS001Response, HttpStatus.OK);
		}
		
		
		// 회원 조회
		Member member = null;
		try {
			member = memberService.getMember(IS001Request.getSearchId());
		} catch (NoSuchElementException e) {
			// 정상처리
			IS001Response.setSuccess(true);
			IS001Response.setMessage("성공");
			log.info("-- return: " + IS001Response.toString());
			
			return new ResponseEntity<>(IS001Response, HttpStatus.OK);
		}
		
		if (StringUtils.isNotEmpty(member.getId())) {
			IS001Response.setMessage("ID중복");
			log.info("-- return: " + IS001Response.toString());
			
			return new ResponseEntity<>(IS001Response, HttpStatus.OK);
		}
		
		IS001Response.setMessage("오류");
		log.info("-- return: " + IS001Response.toString());
		
		return new ResponseEntity<>(IS001Response, HttpStatus.OK);
	}
	
	/**
	 * 회원생성 요청 (2019-06-21:JAEROX)
	 * 회원을 생성한다.
	 * 상점 및 대표사용자 생성에 필요한 정보를 파라미터로 설정한다.
	 * 
	 * @param IS002Request
	 * @return
	 */
	@PostMapping("/IS002")
	@ApiOperation(value="회원생성 요청", notes="회원을 생성한다.")
	public ResponseEntity<Object> IS002(@RequestBody @Valid IS002Request IS002Request) {
		log.info("-- ApiController.IS002 called..");
		
		IS002Response IS002Response = new IS002Response();
		IS002Response.setSuccess(false);
		
		// 요청값 체크
		if (IS002Request == null) {
			IS002Response.setMessage("요청 데이터 오류");
			log.info("-- return: " + IS002Response.toString());
			
			return new ResponseEntity<>(IS002Response, HttpStatus.BAD_REQUEST);
		}
		
		// 요청한 회원 ID
		IS002Response.setMemberId(IS002Request.getMemberId());
		
		if (StringUtils.isEmpty(IS002Request.getSunKey())) {
			IS002Response.setMessage("SUN-KEY 요청 오류");
			log.info("-- return: " + IS002Response.toString());
			
			return new ResponseEntity<>(IS002Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS002Request.getBranchId())) {
			IS002Response.setMessage("지사ID 요청 오류");
			log.info("-- return: " + IS002Response.toString());
			
			return new ResponseEntity<>(IS002Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS002Request.getAgencyId())) {
			IS002Response.setMessage("대리점ID 요청 오류");
			log.info("-- return: " + IS002Response.toString());
			
			return new ResponseEntity<>(IS002Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS002Request.getAdminId())) {
			IS002Response.setMessage("관리자ID 요청 오류");
			log.info("-- return: " + IS002Response.toString());
			
			return new ResponseEntity<>(IS002Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS002Request.getMemberId())) {
			IS002Response.setMessage("생성회원ID 요청 오류");
			log.info("-- return: " + IS002Response.toString());
			
			return new ResponseEntity<>(IS002Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS002Request.getMemberPassword())) {
			IS002Response.setMessage("비밀번호 요청 오류");
			log.info("-- return: " + IS002Response.toString());
			
			return new ResponseEntity<>(IS002Response, HttpStatus.BAD_REQUEST);
		}
		
		
		// 대리점 회원 정보
		Member agencyMember = null;
		try {
			agencyMember = memberService.getMember(IS002Request.getAgencyId());
		} catch (NoSuchElementException e) {
			IS002Response.setMessage("대리점 정보 오류 ");
			log.info("-- return: " + IS002Response.toString());
			
			return new ResponseEntity<>(IS002Response, HttpStatus.OK);
		}
		
		// 대리점이 속해 있는 그룹 정보
		Group agencyGroup = agencyMember.getGroup();
		if (agencyGroup == null) {
			IS002Response.setMessage("대리점 그룹 정보 오류 ");
			log.info("-- return: " + IS002Response.toString());
			
			return new ResponseEntity<>(IS002Response, HttpStatus.OK);
		}
		
		// 연동키
		String privateKey = agencyGroup.getApiAgencyPrivateKey();
		// 연동키가 일치하지 않으면
		if (StringUtils.equals(privateKey, IS002Request.getSunKey()) == false) {
			IS002Response.setMessage("연동키 오류 ");
			log.info("-- return: " + IS002Response.toString());
			
			return new ResponseEntity<>(IS002Response, HttpStatus.OK);
		}
		
		
		// 지사 회원 정보
		Member branchMember = null;
		try {
			branchMember = memberService.getMember(IS002Request.getBranchId());
		} catch (NoSuchElementException e) {
			IS002Response.setMessage("지사 정보 오류 ");
			log.info("-- return: " + IS002Response.toString());
			
			return new ResponseEntity<>(IS002Response, HttpStatus.OK);
		}
		
		// 지사가 속해 있는 그룹 정보
		Group branchGroup = branchMember.getGroup();
		if (branchGroup == null) {
			IS002Response.setMessage("지사 그룹 정보 오류 ");
			log.info("-- return: " + IS002Response.toString());
			
			return new ResponseEntity<>(IS002Response, HttpStatus.OK);
		}
		
		// 지사-대리점 관계가 아니면
		if (branchGroup.getUid() != agencyGroup.getParentGroupUid()) {
			IS002Response.setMessage("상위 그룹 정보 오류 ");
			log.info("-- return: " + IS002Response.toString());
			
			return new ResponseEntity<>(IS002Response, HttpStatus.OK);
		}
		
		
		// 관리자 회원 정보
		Member adminMember = null;
		try {
			adminMember = memberService.getMember(IS002Request.getAdminId());
		} catch (NoSuchElementException e) {
			IS002Response.setMessage("관리자 정보 오류 ");
			log.info("-- return: " + IS002Response.toString());
			
			return new ResponseEntity<>(IS002Response, HttpStatus.OK);
		}
		
		if (adminMember == null) {
			IS002Response.setMessage("관리자 정보 오류 ");
			log.info("-- return: " + IS002Response.toString());
			
			return new ResponseEntity<>(IS002Response, HttpStatus.OK);
		}
		
		
		// 회원 조회
		Member member = null;
		try {
			member = memberService.getMember(IS002Request.getMemberId());
		} catch (NoSuchElementException e) {
			// 소속된 대리점ID
			String agencyId = IS002Request.getAgencyId();
			Member targetAgencyMember = memberService.getMember(agencyId);
			// 소속된 대리점UID
			Group targetAgencyGroup = targetAgencyMember.getGroup();
			
			
			// 회원 생성 처리
			Member apiMember = new Member();
			apiMember.setId(IS002Request.getMemberId());
			apiMember.setPassword(IS002Request.getMemberPassword());
			apiMember.setName("");
			apiMember.setEmail("");
			apiMember.setMobile("");
			apiMember.setApiBizNm(IS002Request.getBizNm());
			apiMember.setApiBizTypeCd(IS002Request.getBizTypeCd());
			apiMember.setApiFee(IS002Request.getFee());
			apiMember.setApiTransFee(IS002Request.getTransFee());
			apiMember.setGroup(targetAgencyGroup);
			
			try {
				memberService.createApiMember(apiMember);
			} catch (DuplicateKeyException e1) {
				IS002Response.setMessage("ID중복");
				log.info("-- return: " + IS002Response.toString());
				
				return new ResponseEntity<>(IS002Response, HttpStatus.OK);
			}
			
			// 정상처리
			IS002Response.setSuccess(true);
			IS002Response.setMessage("성공");
			log.info("-- return: " + IS002Response.toString());
			
			return new ResponseEntity<>(IS002Response, HttpStatus.OK);
		}
		
		if (StringUtils.isNotEmpty(member.getId())) {
			IS002Response.setMessage("ID중복");
			log.info("-- return: " + IS002Response.toString());
			
			return new ResponseEntity<>(IS002Response, HttpStatus.OK);
		}
		
		IS002Response.setMessage("오류");
		log.info("-- return: " + IS002Response.toString());
		
		return new ResponseEntity<>(IS002Response, HttpStatus.OK);
	}
	
	/**
	 * 회원상태요청 (2019-06-21:JAEROX)
	 * 회원의 상태를 요청한다.
	 * 
	 * @param IS003Request
	 * @return
	 */
	@PostMapping("/IS003")
	@ApiOperation(value="회원상태요청", notes="회원의 상태를 요청한다.")
	public ResponseEntity<Object> IS003(@RequestBody @Valid IS003Request IS003Request) {
		log.info("-- ApiController.IS003 called..");
		
		IS003Response IS003Response = new IS003Response();
		IS003Response.setSuccess(false);
		
		// 요청값 체크
		if (IS003Request == null) {
			IS003Response.setMessage("요청 데이터 오류");
			log.info("-- return: " + IS003Response.toString());
			
			return new ResponseEntity<>(IS003Response, HttpStatus.BAD_REQUEST);
		}
		
		// 요청한 회원 ID
		IS003Response.setMemberId(IS003Request.getMemberId());
		IS003Response.setServiceType("");
		IS003Response.setStatus("");
		
		if (StringUtils.isEmpty(IS003Request.getBranchId())) {
			IS003Response.setMessage("지사ID 요청 오류");
			log.info("-- return: " + IS003Response.toString());
			
			return new ResponseEntity<>(IS003Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS003Request.getSunKey())) {
			IS003Response.setMessage("SUN-KEY 요청 오류");
			log.info("-- return: " + IS003Response.toString());
			
			return new ResponseEntity<>(IS003Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS003Request.getAgencyId())) {
			IS003Response.setMessage("대리점ID 요청 오류");
			log.info("-- return: " + IS003Response.toString());
			
			return new ResponseEntity<>(IS003Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS003Request.getAdminId())) {
			IS003Response.setMessage("관리자ID 요청 오류");
			log.info("-- return: " + IS003Response.toString());
			
			return new ResponseEntity<>(IS003Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS003Request.getMemberId())) {
			IS003Response.setMessage("회원ID 요청 오류");
			log.info("-- return: " + IS003Response.toString());
			
			return new ResponseEntity<>(IS003Response, HttpStatus.BAD_REQUEST);
		}
		
		
		// 대리점 회원 정보
		Member agencyMember = null;
		try {
			agencyMember = memberService.getMember(IS003Request.getAgencyId());
		} catch (NoSuchElementException e) {
			IS003Response.setMessage("대리점 정보 오류 ");
			log.info("-- return: " + IS003Response.toString());
			
			return new ResponseEntity<>(IS003Response, HttpStatus.OK);
		}
		
		// 대리점이 속해 있는 그룹 정보
		Group agencyGroup = agencyMember.getGroup();
		if (agencyGroup == null) {
			IS003Response.setMessage("대리점 그룹 정보 오류 ");
			log.info("-- return: " + IS003Response.toString());
			
			return new ResponseEntity<>(IS003Response, HttpStatus.OK);
		}
		
		// 연동키
		String privateKey = agencyGroup.getApiAgencyPrivateKey();
		// 연동키가 일치하지 않으면
		if (StringUtils.equals(privateKey, IS003Request.getSunKey()) == false) {
			IS003Response.setMessage("연동키 오류 ");
			log.info("-- return: " + IS003Response.toString());
			
			return new ResponseEntity<>(IS003Response, HttpStatus.OK);
		}
		
		
		// 지사 회원 정보
		Member branchMember = null;
		try {
			branchMember = memberService.getMember(IS003Request.getBranchId());
		} catch (NoSuchElementException e) {
			IS003Response.setMessage("지사 정보 오류 ");
			log.info("-- return: " + IS003Response.toString());
			
			return new ResponseEntity<>(IS003Response, HttpStatus.OK);
		}
		
		// 지사가 속해 있는 그룹 정보
		Group branchGroup = branchMember.getGroup();
		if (branchGroup == null) {
			IS003Response.setMessage("지사 그룹 정보 오류 ");
			log.info("-- return: " + IS003Response.toString());
			
			return new ResponseEntity<>(IS003Response, HttpStatus.OK);
		}
		
		// 지사-대리점 관계가 아니면
		if (branchGroup.getUid() != agencyGroup.getParentGroupUid()) {
			IS003Response.setMessage("상위 그룹 정보 오류 ");
			log.info("-- return: " + IS003Response.toString());
			
			return new ResponseEntity<>(IS003Response, HttpStatus.OK);
		}
		
		
		// 관리자 회원 정보
		Member adminMember = null;
		try {
			adminMember = memberService.getMember(IS003Request.getAdminId());
		} catch (NoSuchElementException e) {
			IS003Response.setMessage("관리자 정보 오류 ");
			log.info("-- return: " + IS003Response.toString());
			
			return new ResponseEntity<>(IS003Response, HttpStatus.OK);
		}
		
		if (adminMember == null) {
			IS003Response.setMessage("관리자 정보 오류 ");
			log.info("-- return: " + IS003Response.toString());
			
			return new ResponseEntity<>(IS003Response, HttpStatus.OK);
		}
		
		
		// 회원 정보
		Member member = null;
		try {
			member = memberService.getMember(IS003Request.getMemberId());
		} catch (NoSuchElementException e) {
			IS003Response.setMessage("회원 정보 오류");
			IS003Response.setStatus("미가입");
			log.info("-- return: " + IS003Response.toString());
			
			return new ResponseEntity<>(IS003Response, HttpStatus.OK);
		}
		
		if (StringUtils.isEmpty(member.getId())) {
			IS003Response.setMessage("회원 정보 오류");
			IS003Response.setStatus("미가입");
			log.info("-- return: " + IS003Response.toString());
			
			return new ResponseEntity<>(IS003Response, HttpStatus.OK);
		}
		
		Store store = member.getStore();
		if (store == null) {
			IS003Response.setMessage("상점 정보 오류");
			log.info("-- return: " + IS003Response.toString());
			
			return new ResponseEntity<>(IS003Response, HttpStatus.OK);
		}
		
		// 상점ID 목록
		List<StoreId> storeIds = store.getStoreIds();
		if (storeIds == null || storeIds.size() == 0) {
			IS003Response.setMessage("상점ID 정보 오류");
			log.info("-- return: " + IS003Response.toString());
			
			return new ResponseEntity<>(IS003Response, HttpStatus.OK);
		}
		
		// 첫번째 상점ID
		StoreId storeId0 = storeIds.get(0);
		if (storeId0 == null) {
			IS003Response.setMessage("상점ID 상세 정보 오류");
			log.info("-- return: " + IS003Response.toString());
			
			return new ResponseEntity<>(IS003Response, HttpStatus.OK);
		}
		
		String serviceType = "일반정산";
		
		String serviceTypeCode0 = storeId0.getServiceTypeCode();
		Boolean activated0 = storeId0.getActivated();
		// 순간정산
		if (StringUtils.equals(serviceTypeCode0, "INSTANT") && activated0) {
			serviceType = "순간정산";
		}
		// 일반정산
		else {
			if (storeIds.size() > 1) {
				// 두번째 상점ID
				StoreId storeId1 = storeIds.get(1);
				if (storeId1 != null) {
					String serviceTypeCode1 = storeId1.getServiceTypeCode();
					Boolean activated1 = storeId1.getActivated();
					// 순간정산
					if (StringUtils.equals(serviceTypeCode1, "INSTANT") && activated1) {
						serviceType = "순간정산";
					}
				}
			}
		}
		
		// 정상처리
		IS003Response.setSuccess(true);
		IS003Response.setMessage("성공");
		IS003Response.setMemberId(member.getId());
		IS003Response.setStatus("가입완료");
		IS003Response.setServiceType(serviceType);
		IS003Response.setDeposit(store.getDeposit());
		log.info("-- return: " + IS003Response.toString());
		
		return new ResponseEntity<>(IS003Response, HttpStatus.OK);
	}
	
	/**
	 * 순간/일반 정산 전환 요청 (2019-07-01:JAEROX)
	 * 순간/일반 정산 전환을 요청한다.
	 * 
	 * @param IS004Request
	 * @return
	 */
	@PostMapping("/IS004")
	@ApiOperation(value="순간/일반 정산 전환 요청", notes="순간/일반 정산 전환을 요청한다.")
	public ResponseEntity<Object> IS004(@RequestBody @Valid IS004Request IS004Request) {
		log.info("-- ApiController.IS004 called..");
		
		IS004Response IS004Response = new IS004Response();
		IS004Response.setSuccess(false);
		
		// 요청값 체크
		if (IS004Request == null) {
			IS004Response.setMessage("요청 데이터 오류");
			log.info("-- return: " + IS004Response.toString());
			
			return new ResponseEntity<>(IS004Response, HttpStatus.BAD_REQUEST);
		}
		
		// 요청한 회원 ID
		IS004Response.setMemberId(IS004Request.getMemberId());
		IS004Response.setServiceType("");
		IS004Response.setStatus("");
		
		if (StringUtils.isEmpty(IS004Request.getSunKey())) {
			IS004Response.setMessage("SUN-KEY 요청 오류");
			log.info("-- return: " + IS004Response.toString());
			
			return new ResponseEntity<>(IS004Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS004Request.getBranchId())) {
			IS004Response.setMessage("지사ID 요청 오류");
			log.info("-- return: " + IS004Response.toString());
			
			return new ResponseEntity<>(IS004Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS004Request.getAgencyId())) {
			IS004Response.setMessage("대리점ID 요청 오류");
			log.info("-- return: " + IS004Response.toString());
			
			return new ResponseEntity<>(IS004Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS004Request.getAdminId())) {
			IS004Response.setMessage("관리자ID 요청 오류");
			log.info("-- return: " + IS004Response.toString());
			
			return new ResponseEntity<>(IS004Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS004Request.getMemberId())) {
			IS004Response.setMessage("회원ID 요청 오류");
			log.info("-- return: " + IS004Response.toString());
			
			return new ResponseEntity<>(IS004Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS004Request.getServiceType())) {
			IS004Response.setMessage("정산 전환 요청 오류");
			log.info("-- return: " + IS004Response.toString());
			
			return new ResponseEntity<>(IS004Response, HttpStatus.BAD_REQUEST);
		}
		
		
		// 대리점 회원 정보
		Member agencyMember = null;
		try {
			agencyMember = memberService.getMember(IS004Request.getAgencyId());
		} catch (NoSuchElementException e) {
			IS004Response.setMessage("대리점 정보 오류 ");
			log.info("-- return: " + IS004Response.toString());
			
			return new ResponseEntity<>(IS004Response, HttpStatus.OK);
		}
		
		// 대리점이 속해 있는 그룹 정보
		Group agencyGroup = agencyMember.getGroup();
		if (agencyGroup == null) {
			IS004Response.setMessage("대리점 그룹 정보 오류 ");
			log.info("-- return: " + IS004Response.toString());
			
			return new ResponseEntity<>(IS004Response, HttpStatus.OK);
		}
		
		// 연동키
		String privateKey = agencyGroup.getApiAgencyPrivateKey();
		// 연동키가 일치하지 않으면
		if (StringUtils.equals(privateKey, IS004Request.getSunKey()) == false) {
			IS004Response.setMessage("연동키 오류 ");
			log.info("-- return: " + IS004Response.toString());
			
			return new ResponseEntity<>(IS004Response, HttpStatus.OK);
		}
		
		
		// 지사 회원 정보
		Member branchMember = null;
		try {
			branchMember = memberService.getMember(IS004Request.getBranchId());
		} catch (NoSuchElementException e) {
			IS004Response.setMessage("지사 정보 오류 ");
			log.info("-- return: " + IS004Response.toString());
			
			return new ResponseEntity<>(IS004Response, HttpStatus.OK);
		}
		
		// 지사가 속해 있는 그룹 정보
		Group branchGroup = branchMember.getGroup();
		if (branchGroup == null) {
			IS004Response.setMessage("지사 그룹 정보 오류 ");
			log.info("-- return: " + IS004Response.toString());
			
			return new ResponseEntity<>(IS004Response, HttpStatus.OK);
		}
		
		// 지사-대리점 관계가 아니면
		if (branchGroup.getUid() != agencyGroup.getParentGroupUid()) {
			IS004Response.setMessage("상위 그룹 정보 오류 ");
			log.info("-- return: " + IS004Response.toString());
			
			return new ResponseEntity<>(IS004Response, HttpStatus.OK);
		}
		

		// 관리자 회원 정보
		Member adminMember = null;
		try {
			adminMember = memberService.getMember(IS004Request.getAdminId());
		} catch (NoSuchElementException e) {
			IS004Response.setMessage("관리자 정보 오류 ");
			log.info("-- return: " + IS004Response.toString());
			
			return new ResponseEntity<>(IS004Response, HttpStatus.OK);
		}
		
		if (adminMember == null) {
			IS004Response.setMessage("관리자 정보 오류 ");
			log.info("-- return: " + IS004Response.toString());
			
			return new ResponseEntity<>(IS004Response, HttpStatus.OK);
		}
		
		
		// 회원 정보
		Member member = null;
		try {
			member = memberService.getMember(IS004Request.getMemberId());
		} catch (NoSuchElementException e) {
			IS004Response.setMessage("회원 정보 오류");
			IS004Response.setStatus("미가입");
			log.info("-- return: " + IS004Response.toString());
			
			return new ResponseEntity<>(IS004Response, HttpStatus.OK);
		}
		
		if (StringUtils.isEmpty(member.getId())) {
			IS004Response.setMessage("요청 회원 정보 오류");
			IS004Response.setStatus("미가입");
			log.info("-- return: " + IS004Response.toString());
			
			return new ResponseEntity<>(IS004Response, HttpStatus.OK);
		}
		
		// 상점 정보
		Store requestStore = member.getStore();
		if (requestStore == null) {
			IS004Response.setMessage("요청 상점 정보 오류");
			log.info("-- return: " + IS004Response.toString());
			
			return new ResponseEntity<>(IS004Response, HttpStatus.OK);
		}
		
		// 상점 고유 번호
		int storeUid = requestStore.getUid();
		

		// 푸시 여부
		boolean sendPush = true;
		
		String requestServiceType = IS004Request.getServiceType();
		// 순간정산
		if (StringUtils.equals(requestServiceType, "순간정산")) {
			// 순간정산 전환 요청 처리
			storeService.instantOn(storeUid, sendPush);
		}
		// 일반정산
		else {
			// 일반정산 전환 요청 처리
			storeService.instantOff(storeUid, sendPush);
		}
		
		
		// 상점 정보 새로 로드
		Store store = member.getStore();
		if (store == null) {
			IS004Response.setMessage("상점 정보 오류");
			log.info("-- return: " + IS004Response.toString());
			
			return new ResponseEntity<>(IS004Response, HttpStatus.OK);
		}
		
		// 상점ID 목록
		List<StoreId> storeIds = store.getStoreIds();
		if (storeIds == null || storeIds.size() == 0) {
			IS004Response.setMessage("상점ID 정보 오류");
			log.info("-- return: " + IS004Response.toString());
			
			return new ResponseEntity<>(IS004Response, HttpStatus.OK);
		}
		
		// 첫번째 상점ID
		StoreId storeId0 = storeIds.get(0);
		if (storeId0 == null) {
			IS004Response.setMessage("상점ID 상세 정보 오류");
			log.info("-- return: " + IS004Response.toString());
			
			return new ResponseEntity<>(IS004Response, HttpStatus.OK);
		}
		
		String serviceType = "일반정산";
		
		String serviceTypeCode0 = storeId0.getServiceTypeCode();
		Boolean activated0 = storeId0.getActivated();
		// 순간정산
		if (StringUtils.equals(serviceTypeCode0, "INSTANT") && activated0) {
			serviceType = "순간정산";
		}
		// 일반정산
		else {
			if (storeIds.size() > 1) {
				// 두번째 상점ID
				StoreId storeId1 = storeIds.get(1);
				if (storeId1 != null) {
					String serviceTypeCode1 = storeId1.getServiceTypeCode();
					Boolean activated1 = storeId1.getActivated();
					// 순간정산
					if (StringUtils.equals(serviceTypeCode1, "INSTANT") && activated1) {
						serviceType = "순간정산";
					}
				}
			}
		}
		
		// 정상처리
		IS004Response.setSuccess(true);
		IS004Response.setMessage("성공");
		IS004Response.setMemberId(member.getId());
		IS004Response.setStatus("가입완료");
		IS004Response.setServiceType(serviceType);
		IS004Response.setDeposit(store.getDeposit());
		log.info("-- return: " + IS004Response.toString());
		
		return new ResponseEntity<>(IS004Response, HttpStatus.OK);
	}
	
	/**
	 * 결제 내역 요청 (2019-07-02:JAEROX)
	 * 결제 내역을 요청한다.
	 * 
	 * @param IS005Request
	 * @return
	 */
	@PostMapping("/IS005")
	@ApiOperation(value="결제 내역 요청", notes="결제 내역을 요청한다.")
	public ResponseEntity<Object> IS005(@RequestBody @Valid IS005Request IS005Request) {
		log.info("-- ApiController.IS005 called..");
		
		IS005Response IS005Response = new IS005Response();
		IS005Response.setSuccess(false);
		
		// 요청값 체크
		if (IS005Request == null) {
			IS005Response.setMessage("요청 데이터 오류");
			log.info("-- return: " + IS005Response.toString());
			
			return new ResponseEntity<>(IS005Response, HttpStatus.BAD_REQUEST);
		}
		
		// 요청한 회원 ID
		IS005Response.setMemberId(IS005Request.getMemberId());
		IS005Response.setServiceType("");
		IS005Response.setStatus("");
		
		if (StringUtils.isEmpty(IS005Request.getSunKey())) {
			IS005Response.setMessage("SUN-KEY 요청 오류");
			log.info("-- return: " + IS005Response.toString());
			
			return new ResponseEntity<>(IS005Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS005Request.getBranchId())) {
			IS005Response.setMessage("지사ID 요청 오류");
			log.info("-- return: " + IS005Response.toString());
			
			return new ResponseEntity<>(IS005Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS005Request.getAgencyId())) {
			IS005Response.setMessage("대리점ID 요청 오류");
			log.info("-- return: " + IS005Response.toString());
			
			return new ResponseEntity<>(IS005Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS005Request.getAdminId())) {
			IS005Response.setMessage("관리자ID 요청 오류");
			log.info("-- return: " + IS005Response.toString());
			
			return new ResponseEntity<>(IS005Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS005Request.getMemberId())) {
			IS005Response.setMessage("회원ID 요청 오류");
			log.info("-- return: " + IS005Response.toString());
			
			return new ResponseEntity<>(IS005Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS005Request.getPaymentDate())) {
			IS005Response.setMessage("결제 날짜 요청 오류");
			log.info("-- return: " + IS005Response.toString());
			
			return new ResponseEntity<>(IS005Response, HttpStatus.BAD_REQUEST);
		}
		
		
		// 대리점 회원 정보
		Member agencyMember = null;
		try {
			agencyMember = memberService.getMember(IS005Request.getAgencyId());
		} catch (NoSuchElementException e) {
			IS005Response.setMessage("대리점 정보 오류 ");
			log.info("-- return: " + IS005Response.toString());
			
			return new ResponseEntity<>(IS005Response, HttpStatus.OK);
		}
		
		// 대리점이 속해 있는 그룹 정보
		Group agencyGroup = agencyMember.getGroup();
		if (agencyGroup == null) {
			IS005Response.setMessage("대리점 그룹 정보 오류 ");
			log.info("-- return: " + IS005Response.toString());
			
			return new ResponseEntity<>(IS005Response, HttpStatus.OK);
		}
		
		// 연동키
		String privateKey = agencyGroup.getApiAgencyPrivateKey();
		// 연동키가 일치하지 않으면
		if (StringUtils.equals(privateKey, IS005Request.getSunKey()) == false) {
			IS005Response.setMessage("연동키 오류 ");
			log.info("-- return: " + IS005Response.toString());
			
			return new ResponseEntity<>(IS005Response, HttpStatus.OK);
		}
		
		
		// 지사 회원 정보
		Member branchMember = null;
		try {
			branchMember = memberService.getMember(IS005Request.getBranchId());
		} catch (NoSuchElementException e) {
			IS005Response.setMessage("지사 정보 오류 ");
			log.info("-- return: " + IS005Response.toString());
			
			return new ResponseEntity<>(IS005Response, HttpStatus.OK);
		}
		
		// 지사가 속해 있는 그룹 정보
		Group branchGroup = branchMember.getGroup();
		if (branchGroup == null) {
			IS005Response.setMessage("지사 그룹 정보 오류 ");
			log.info("-- return: " + IS005Response.toString());
			
			return new ResponseEntity<>(IS005Response, HttpStatus.OK);
		}
		
		// 지사-대리점 관계가 아니면
		if (branchGroup.getUid() != agencyGroup.getParentGroupUid()) {
			IS005Response.setMessage("상위 그룹 정보 오류 ");
			log.info("-- return: " + IS005Response.toString());
			
			return new ResponseEntity<>(IS005Response, HttpStatus.OK);
		}
		
		
		// 관리자 회원 정보
		Member adminMember = null;
		try {
			adminMember = memberService.getMember(IS005Request.getAdminId());
		} catch (NoSuchElementException e) {
			IS005Response.setMessage("관리자 정보 오류 ");
			log.info("-- return: " + IS005Response.toString());
			
			return new ResponseEntity<>(IS005Response, HttpStatus.OK);
		}
		
		if (adminMember == null) {
			IS005Response.setMessage("관리자 정보 오류 ");
			log.info("-- return: " + IS005Response.toString());
			
			return new ResponseEntity<>(IS005Response, HttpStatus.OK);
		}
		
		
		// 회원 정보
		Member member = null;
		try {
			member = memberService.getMember(IS005Request.getMemberId());
		} catch (NoSuchElementException e) {
			IS005Response.setMessage("회원 정보 오류");
			IS005Response.setStatus("미가입");
			log.info("-- return: " + IS005Response.toString());
			
			return new ResponseEntity<>(IS005Response, HttpStatus.OK);
		}
		
		if (StringUtils.isEmpty(member.getId())) {
			IS005Response.setMessage("요청 회원 정보 오류");
			IS005Response.setStatus("미가입");
			log.info("-- return: " + IS005Response.toString());
			
			return new ResponseEntity<>(IS005Response, HttpStatus.OK);
		}
		
		// 상점 정보
		Store store = member.getStore();
		if (store == null) {
			IS005Response.setMessage("요청 상점 정보 오류");
			log.info("-- return: " + IS005Response.toString());
			
			return new ResponseEntity<>(IS005Response, HttpStatus.OK);
		}
		
		
		// 정산구분코드
		List<Code> serviceTypeCodes = codeService.getCodes("SERVICE_TYPE");
		for (Iterator<Code> iterator = serviceTypeCodes.iterator(); iterator.hasNext();) {
			Code code = (Code) iterator.next();
			if (StringUtils.equals(code.getCode(), "INSTANT") == false) {
				code.setCodeName("일반정산");
			}
		}
		Map<String, String> serviceTypeCodesMap = serviceTypeCodes.stream().collect(Collectors.toMap(Code::getCode, Code::getCodeName));
		
		// 결제수단코드
		List<Code> paymethodCodes = codeService.getCodes("KSNET_PAYMETHOD");
		Map<String, String> paymethodCodesMap = paymethodCodes.stream().collect(Collectors.toMap(Code::getCode, Code::getCodeName, (oldValue, newValue) -> newValue));
		
		// 결제 내역 요청 날짜
		String paymentDate = IS005Request.getPaymentDate();
		
		// 결제수단 코드 리스트
		List<String> paymethods = new ArrayList<>(paymethodCodesMap.keySet());
		
		// 정산방법 코드 리스트
		List<String> paramServiceTypeCodes = new ArrayList<>(serviceTypeCodesMap.keySet());
		
		// 결제 내역
		List<PaymentListItem> paymentList = new ArrayList<PaymentListItem>();
		List<PaymentItem> paymentItems = paymentService.getPaymentItems(member, paymentDate, paymentDate, paymethods, paramServiceTypeCodes);
		for (PaymentItem paymentItem : paymentItems) {
			PaymentListItem paymentListItem = new PaymentListItem();
			// 날짜
			paymentListItem.setPaidDate(paymentItem.getPaidDate());
			// 결제금액
			paymentListItem.setAmount(paymentItem.getAmount());
			// 정산타입
			paymentListItem.setServiceType(serviceTypeCodesMap.get(paymentItem.getServiceTypeCode()));
			// 결제번호(영수증)
			paymentListItem.setTrNo(paymentItem.getTrNo());
			// 결제수단
			paymentListItem.setPaymethod(paymethodCodesMap.get(paymentItem.getPaymethodCode()));
			// 상품명
			paymentListItem.setGoodsName(paymentItem.getGoodsName());
			// 구매자
			paymentListItem.setOrderName(paymentItem.getOrderName());
			// 상점주문번호
			paymentListItem.setOrderNo(paymentItem.getOrderNo());
			
			paymentList.add(paymentListItem);
		}

		
		// 정상처리
		IS005Response.setSuccess(true);
		IS005Response.setMessage("성공");
		IS005Response.setMemberId(member.getId());
		IS005Response.setStatus("가입완료");
		IS005Response.setDeposit(store.getDeposit());
		IS005Response.setPaymentList(paymentList);
		log.info("-- return: " + IS005Response.toString());
		
		return new ResponseEntity<>(IS005Response, HttpStatus.OK);
	}
	
	/**
	 * 정산 내역 요청 (2019-07-03:JAEROX)
	 * 정산 내역을 요청한다.
	 * 
	 * @param IS006Request
	 * @return
	 */
	@PostMapping("/IS006")
	@ApiOperation(value="정산 내역 요청", notes="정산 내역을 요청한다.")
	public ResponseEntity<Object> IS006(@RequestBody @Valid IS006Request IS006Request) {
		log.info("-- ApiController.IS006 called..");
		
		IS006Response IS006Response = new IS006Response();
		IS006Response.setSuccess(false);
		
		// 요청값 체크
		if (IS006Request == null) {
			IS006Response.setMessage("요청 데이터 오류");
			log.info("-- return: " + IS006Response.toString());
			
			return new ResponseEntity<>(IS006Response, HttpStatus.BAD_REQUEST);
		}
		
		// 요청한 회원 ID
		IS006Response.setMemberId(IS006Request.getMemberId());
		IS006Response.setServiceType("");
		IS006Response.setStatus("");
		
		if (StringUtils.isEmpty(IS006Request.getSunKey())) {
			IS006Response.setMessage("SUN-KEY 요청 오류");
			log.info("-- return: " + IS006Response.toString());
			
			return new ResponseEntity<>(IS006Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS006Request.getBranchId())) {
			IS006Response.setMessage("지사ID 요청 오류");
			log.info("-- return: " + IS006Response.toString());
			
			return new ResponseEntity<>(IS006Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS006Request.getAgencyId())) {
			IS006Response.setMessage("대리점ID 요청 오류");
			log.info("-- return: " + IS006Response.toString());
			
			return new ResponseEntity<>(IS006Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS006Request.getAdminId())) {
			IS006Response.setMessage("관리자ID 요청 오류");
			log.info("-- return: " + IS006Response.toString());
			
			return new ResponseEntity<>(IS006Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS006Request.getMemberId())) {
			IS006Response.setMessage("회원ID 요청 오류");
			log.info("-- return: " + IS006Response.toString());
			
			return new ResponseEntity<>(IS006Response, HttpStatus.BAD_REQUEST);
		}
		
		if (StringUtils.isEmpty(IS006Request.getSettleMonth())) {
			IS006Response.setMessage("정산 월 요청 오류");
			log.info("-- return: " + IS006Response.toString());
			
			return new ResponseEntity<>(IS006Response, HttpStatus.BAD_REQUEST);
		}
		
		
		// 대리점 회원 정보
		Member agencyMember = null;
		try {
			agencyMember = memberService.getMember(IS006Request.getAgencyId());
		} catch (NoSuchElementException e) {
			IS006Response.setMessage("대리점 정보 오류 ");
			log.info("-- return: " + IS006Response.toString());
			
			return new ResponseEntity<>(IS006Response, HttpStatus.OK);
		}
		
		// 대리점이 속해 있는 그룹 정보
		Group agencyGroup = agencyMember.getGroup();
		if (agencyGroup == null) {
			IS006Response.setMessage("대리점 그룹 정보 오류 ");
			log.info("-- return: " + IS006Response.toString());
			
			return new ResponseEntity<>(IS006Response, HttpStatus.OK);
		}
		
		// 연동키
		String privateKey = agencyGroup.getApiAgencyPrivateKey();
		// 연동키가 일치하지 않으면
		if (StringUtils.equals(privateKey, IS006Request.getSunKey()) == false) {
			IS006Response.setMessage("연동키 오류 ");
			log.info("-- return: " + IS006Response.toString());
			
			return new ResponseEntity<>(IS006Response, HttpStatus.OK);
		}
		
		
		// 지사 회원 정보
		Member branchMember = null;
		try {
			branchMember = memberService.getMember(IS006Request.getBranchId());
		} catch (NoSuchElementException e) {
			IS006Response.setMessage("지사 정보 오류 ");
			log.info("-- return: " + IS006Response.toString());
			
			return new ResponseEntity<>(IS006Response, HttpStatus.OK);
		}
		
		// 지사가 속해 있는 그룹 정보
		Group branchGroup = branchMember.getGroup();
		if (branchGroup == null) {
			IS006Response.setMessage("지사 그룹 정보 오류 ");
			log.info("-- return: " + IS006Response.toString());
			
			return new ResponseEntity<>(IS006Response, HttpStatus.OK);
		}
		
		// 지사-대리점 관계가 아니면
		if (branchGroup.getUid() != agencyGroup.getParentGroupUid()) {
			IS006Response.setMessage("상위 그룹 정보 오류 ");
			log.info("-- return: " + IS006Response.toString());
			
			return new ResponseEntity<>(IS006Response, HttpStatus.OK);
		}
		
		
		// 관리자 회원 정보
		Member adminMember = null;
		try {
			adminMember = memberService.getMember(IS006Request.getAdminId());
		} catch (NoSuchElementException e) {
			IS006Response.setMessage("관리자 정보 오류 ");
			log.info("-- return: " + IS006Response.toString());
			
			return new ResponseEntity<>(IS006Response, HttpStatus.OK);
		}
		
		if (adminMember == null) {
			IS006Response.setMessage("관리자 정보 오류 ");
			log.info("-- return: " + IS006Response.toString());
			
			return new ResponseEntity<>(IS006Response, HttpStatus.OK);
		}
		
		
		// 회원 정보
		Member member = null;
		try {
			member = memberService.getMember(IS006Request.getMemberId());
		} catch (NoSuchElementException e) {
			IS006Response.setMessage("회원 정보 오류");
			IS006Response.setStatus("미가입");
			log.info("-- return: " + IS006Response.toString());
			
			return new ResponseEntity<>(IS006Response, HttpStatus.OK);
		}
		
		if (StringUtils.isEmpty(member.getId())) {
			IS006Response.setMessage("요청 회원 정보 오류");
			IS006Response.setStatus("미가입");
			log.info("-- return: " + IS006Response.toString());
			
			return new ResponseEntity<>(IS006Response, HttpStatus.OK);
		}
		
		// 상점 정보
		Store store = member.getStore();
		if (store == null) {
			IS006Response.setMessage("요청 상점 정보 오류");
			log.info("-- return: " + IS006Response.toString());
			
			return new ResponseEntity<>(IS006Response, HttpStatus.OK);
		}
		
		
		// 정산구분코드
		List<Code> serviceTypeCodes = codeService.getCodes("SERVICE_TYPE");
		for (Iterator<Code> iterator = serviceTypeCodes.iterator(); iterator.hasNext();) {
			Code code = (Code) iterator.next();
			if (StringUtils.equals(code.getCode(), "INSTANT") == false) {
				code.setCodeName("일반정산");
			}
		}
		Map<String, String> serviceTypeCodesMap = serviceTypeCodes.stream().collect(Collectors.toMap(Code::getCode, Code::getCodeName));
		
		// 결제수단코드
		List<Code> paymethodCodes = codeService.getCodes("KSNET_PAYMETHOD");
		Map<String, String> paymethodCodesMap = paymethodCodes.stream().collect(Collectors.toMap(Code::getCode, Code::getCodeName, (oldValue, newValue) -> newValue));
		
		// 정산 내역 요청 월
		String settleMonth = IS006Request.getSettleMonth();
		
		// 결제수단 코드 리스트
		List<String> paymethods = new ArrayList<>(paymethodCodesMap.keySet());
		
		// 정산방법 코드 리스트
		List<String> paramServiceTypeCodes = new ArrayList<>(serviceTypeCodesMap.keySet());
		
		// 정산 내역
		List<SettleListItem> settleList = new ArrayList<SettleListItem>();
		List<PaymentItem> paymentItems = paymentService.getPaymentItems(member, settleMonth+"01", settleMonth+"31", paymethods, paramServiceTypeCodes);
		for (PaymentItem paymentItem : paymentItems) {
			SettleListItem settleListItem = new SettleListItem();
			// 날짜
			settleListItem.setPaidDate(paymentItem.getPaidDate());
			// 소속레벨
			settleListItem.setGroupRoleName(paymentItem.getGroupRoleName());
			// 소속명
			settleListItem.setGroupBizName(paymentItem.getGroupBizName());
			// 상점명
			settleListItem.setStoreBizeName(paymentItem.getStoreBizeName());
			// 매출액
			settleListItem.setAmount(paymentItem.getAmount());
			// 정산타입
			settleListItem.setServiceType(serviceTypeCodesMap.get(paymentItem.getServiceTypeCode()));
			// 결제번호(영수증)
			settleListItem.setTrNo(paymentItem.getTrNo());
			// 결제수단
			settleListItem.setPaymethod(paymethodCodesMap.get(paymentItem.getPaymethodCode()));
			// 상품명
			settleListItem.setGoodsName(paymentItem.getGoodsName());
			// 구매자
			settleListItem.setOrderName(paymentItem.getOrderName());
			// 상점주문번호
			settleListItem.setOrderNo(paymentItem.getOrderNo());
			// 승인번호
			settleListItem.setAuthno(paymentItem.getAuthno());
			// 판매자
			settleListItem.setBizOwner(paymentItem.getBizOwner());
			// 판매자ID	
			settleListItem.setOwnerMemberId(paymentItem.getOwnerMemberId());
			// 판매자연락처
			settleListItem.setBizContact(paymentItem.getBizContact());
			// 구매자연락처
			settleListItem.setSndMobile(paymentItem.getSndMobile());
			// 할부
			settleListItem.setHalbu(paymentItem.getHalbu());
			// 발급사명
			settleListItem.setMsg1(paymentItem.getMsg1());
			// PG사수익
			settleListItem.setProfitPg(paymentItem.getProfitPg());
			// 본사수익
			settleListItem.setProfitHead(paymentItem.getProfitHead());
			// 지시수익
			settleListItem.setProfitBranch(paymentItem.getProfitBranch());
			// 대리점수익
			settleListItem.setProfitAgency(paymentItem.getProfitAgency());
			// 상점정산액
			settleListItem.setProfitStore(paymentItem.getProfitStore());
			// 상점정산액(환불전)	
			settleListItem.setBeforeRefundProfitStore(paymentItem.getBeforeRefundProfitStore());
			// 환불송금수수료
			settleListItem.setStoreDeduction(paymentItem.getStoreDeduction());
			// 취소예치금차감액
			settleListItem.setDepositDeduction(paymentItem.getDepositDeduction());
			// 환불완료일
			settleListItem.setRefundDateTime(paymentItem.getRefundDateTime());
			
			settleList.add(settleListItem);
		}
		

		// 정상처리
		IS006Response.setSuccess(true);
		IS006Response.setMessage("성공");
		IS006Response.setMemberId(member.getId());
		IS006Response.setStatus("가입완료");
		IS006Response.setDeposit(store.getDeposit());
		IS006Response.setSettleList(settleList);
		log.info("-- return: " + IS006Response.toString());
		
		return new ResponseEntity<>(IS006Response, HttpStatus.OK);
	}
	
}
