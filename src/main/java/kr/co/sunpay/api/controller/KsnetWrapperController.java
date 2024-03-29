package kr.co.sunpay.api.controller;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.co.sunpay.api.domain.KsnetPay;
import kr.co.sunpay.api.domain.KsnetPayResult;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.domain.StoreId;
import kr.co.sunpay.api.model.DepositService;
import kr.co.sunpay.api.model.PaymentItem;
import kr.co.sunpay.api.repository.KsnetPayRepository;
import kr.co.sunpay.api.repository.KsnetPayResultRepository;
import kr.co.sunpay.api.repository.StoreIdRepository;
import kr.co.sunpay.api.service.MemberService;
import kr.co.sunpay.api.service.PushService;
import kr.co.sunpay.api.service.StoreService;
import lombok.extern.java.Log;
import springfox.documentation.annotations.ApiIgnore;

@Log
@ApiIgnore
@Controller
@RequestMapping("/ksnet")
public class KsnetWrapperController {

	@Autowired
	KsnetPayRepository ksnetPayRepo;

	@Autowired
	KsnetPayResultRepository ksnetPayResultRepo;
	
	@Autowired
	StoreService storeService;

	@Autowired
	StoreIdRepository storeIdRepo;
	
	@Autowired
	PushService pushService;
	
	@Autowired
	DepositService depositService;
	
	@Autowired
	MemberService memberService;

	/**
	 * 결제데이터 받아서 저장 및 KSNet 통신 시작
	 * 
	 * @param ksnetPay
	 * @param model
	 */
	@RequestMapping({"/init", "/m/init"})
	public void init(KsnetPay ksnetPay, Model model) {
		log.info("-- KsnetWrapperController.init called...");
		// ko id
		Member member= memberService.getMember(ksnetPay.getMemberId());  
		Store store = storeService.getStoreByStoreMember(member);	
		
		ksnetPay.setFeePg(store.getFeePg());
		ksnetPay.setFeeHead(store.getFeeHead());
		ksnetPay.setFeeBranch(store.getFeeBranch());
		ksnetPay.setFeeAgency(store.getFeeAgency());
		
		ksnetPay.setInstantFeePg(store.getInstantFeePg());
		ksnetPay.setInstantFeeHead(store.getInstantFeeHead());
		ksnetPay.setInstantFeeBranch(store.getInstantFeeBranch());
		ksnetPay.setInstantFeeAgency(store.getInstantFeeAgency());
		
		ksnetPay.setTransFeePg(store.getTransFeePg());
		ksnetPay.setTransFeeHead(store.getTransFeeHead());
		ksnetPay.setTransFeeBranch(store.getTransFeeBranch());
		ksnetPay.setTransFeeAgency(store.getTransFeeAgency());
		
		// 결제요청 저장
		KsnetPay newPay = ksnetPayRepo.save(ksnetPay);
		
		// ksnetPay.getSndStoreid() > 상점을 확인하기 위한 상점ID, 실제 결제는 현재 상점 Activated ID로 진행함
		// 단 예치금 부족하거나 결제한도 초과 시 일반정산으로 결제
		
		// 상점ID로 상점 검색하여 현재 Activated 상태인 상점ID 구함
		//Store store = storeService.getStoreByStoreId(ksnetPay.getSndStoreid());
		

		if (store == null) {
			model.addAttribute("err", "상점정보를 찾을 수 없습니다. 관리자에게 문의해주시기 바랍니다.[1]");
			return;
		}
		
		// 상점ID 미등록 상태
		if (store.getStoreIds() == null || store.getStoreIds().size() < 1) {
			model.addAttribute("err", "상점정보를 찾을 수 없습니다. 관리자에게 문의해주시기 바랍니다.[2]");
			return;
		}
		
		StoreId activatedId = null;
		
		for (StoreId sId : store.getStoreIds()) {
			if (sId.getActivated()) {
				activatedId = sId;
				//newPay.setSndStoreid(activatedId.getId());
				//newPay=ksnetPayRepo.save(newPay);
				break;
			}
		}
		
		// 활성화상태의 상점ID가 없음
		if (activatedId == null) {
			model.addAttribute("err", "상점정보를 찾을 수 없습니다. 관리자에게 문의해주시기 바랍니다.[3]");
			return;
		}
		
		if (activatedId.getServiceTypeCode().equals(StoreService.SERVICE_TYPE_INSTANT)) {
			// TODO: 순간결제라면 예치금, 결제한도 확인 후 진행
			// 예치금이 현재 상품 금액보다 적은 경우
			if (store.getDeposit() < ksnetPay.getSndAmount()) {
				boolean sendPush = false;
				
				// 순간정산 OFF
				storeService.instantOff(store.getUid(), sendPush);
				
				// 결제 상점 ID를 일반결제 ID로 전환
				
				//newPay.setSndStoreid(storeService.getActivatedId(store));			
				//newPay=ksnetPayRepo.save(newPay);
				activatedId = storeService.getActivatedId(store);
				
				depositService.pushDepositLack(store);
			}

		}
		newPay.setSndStoreid(activatedId.getId());	
		//newPay.setStoreIdsUidFk();	
		newPay=ksnetPayRepo.save(newPay);
		
		// 상점ID는 예치금 확인 후 변경된 ID로 적용
		//model.addAttribute("storeId", ksnetPay.getSndStoreid());
		// storeId 를 KSnet 등록된 형태로 전달한다.
		String realStoreId=newPay.getSndStoreid();
		String[] idArray = realStoreId.split("_");		
		String storeidForKsnet=idArray[0];		
		//ksnetPay.setSndStoreid(storeidForKsnet);
		
		//model.addAttribute("storeId", ksnetPay.getSndStoreid());
		model.addAttribute("storeId", storeidForKsnet);
		model.addAttribute("uid", newPay.getUid());
		
	}

	/**
	 * KSNet 중간단계, rctype 등 키값 받음
	 * 
	 * @param uid
	 * @param model
	 * @return
	 */
	@RequestMapping("/payment/{uid}")
	public String payment(@PathVariable int uid, Model model) {
		log.info("-- KsnetWrapperController.payment called...");
		KsnetPay ksnetPay = ksnetPayRepo.findByUid(uid);
        /*		
		String realStoreId=ksnetPay.getSndStoreid();
		String[] idArray = realStoreId.split("_");		
		String storeidForKsnet=idArray[0];		
		ksnetPay.setSndStoreid(storeidForKsnet);
		*/
		model.addAttribute("order", ksnetPay);

		return "ksnet/payment";
	}

	/**
	 * KSNet에서 받은 키값으로 결제 마무리 및 쇼핑몰 페이지로 돌아감
	 * 
	 * @param request
	 * @param model
	 */
	@RequestMapping("/finish")
	public void finish(HttpServletRequest request, Model model) {
		log.info("-- KsnetWrapperController.finish called...");

		KsnetPay ksnetPay = ksnetPayRepo.findByUid(Integer.parseInt(request.getParameter("uid")));
		KsnetPayResult ksnetPayResult = new KsnetPayResult();

		if (!storeIdRepo.findById(ksnetPay.getSndStoreid()).isPresent()) {
			throw new EntityNotFoundException("사용가능한 상점 ID가 없습니다.");
		}

		// Dashboard에 노출될 내용
		ksnetPayResult.setKsnetPay(ksnetPay);
		ksnetPayResult.setStoreId(ksnetPay.getSndStoreid());
		ksnetPayResult.setServiceTypeCd(
				storeIdRepo.findById(ksnetPay.getSndStoreid()).get().getServiceTypeCode());

		String rcid = request.getParameter("reCommConId");
		String authyn = "";
		String authno = "";

		ksnet.kspay.KSPayWebHostBean ipg = new ksnet.kspay.KSPayWebHostBean(rcid);

		// KSNET 결제결과 중 아래에 나타나지 않은 항목이 필요한 경우 Null 대신 필요한 항목명을 설정할 수 있습니다.
		if (ipg.kspay_send_msg("1")) {
			authyn = ipg.kspay_get_value("authyn");
			authno = ipg.kspay_get_value("authno");

			ksnetPayResult.setAuthyn(authyn); // 성공여부
			ksnetPayResult.setAuthno(authno);

			ksnetPayResult.setTrno(ipg.kspay_get_value("trno")); // 거래번호(KSNet 고유번호)
			ksnetPayResult.setTrddt(ipg.kspay_get_value("trddt"));
			ksnetPayResult.setTrdtm(ipg.kspay_get_value("trdtm"));
			ksnetPayResult.setAmt(Integer.parseInt(ipg.kspay_get_value("amt")));
			ksnetPayResult.setMsg1(ipg.kspay_get_value("msg1"));
			ksnetPayResult.setMsg2(ipg.kspay_get_value("msg2"));
			ksnetPayResult.setOrdno(ipg.kspay_get_value("ordno"));
			ksnetPayResult.setResult(ipg.kspay_get_value("result"));
			ksnetPayResult.setIsscd(ipg.kspay_get_value("isscd"));
			ksnetPayResult.setAqucd(ipg.kspay_get_value("aqucd"));
			ksnetPayResult.setHalbu(ipg.kspay_get_value("halbu"));
			ksnetPayResult.setCbtrno(ipg.kspay_get_value("cbtrno"));
			
			//fee 추가
			putProfitInto(ksnetPayResult);
		}

		ksnetPayResult = ksnetPayResultRepo.save(ksnetPayResult);

		// 결제 결과 PUSH 발송
		pushService.sendPush(ksnetPayResult);

		model.addAttribute("sndReply", ksnetPay.getSndReply());
		model.addAttribute("reCommConId", rcid);
		model.addAttribute("reCommType", request.getParameter("reCommType"));
		model.addAttribute("reHash", request.getParameter("reHash"));

	}
	
	/**
	 * KSNet에서 받은 키값으로 결제 마무리 및 쇼핑몰 페이지로 돌아감
	 * 
	 * @param request
	 * @param model
	 */
	@RequestMapping("/m/finish/{uid}")
	public String mFinish(@PathVariable int uid, HttpServletRequest request, Model model) {
		log.info("-- KsnetWrapperController.mFinish called...");

		KsnetPay ksnetPay = ksnetPayRepo.findByUid(uid);
		KsnetPayResult ksnetPayResult = new KsnetPayResult();

		if (!storeIdRepo.findById(ksnetPay.getSndStoreid()).isPresent()) {
			throw new EntityNotFoundException("사용가능한 상점 ID가 없습니다.");
		}

		// Dashboard에 노출될 내용
		ksnetPayResult.setKsnetPay(ksnetPay);
		ksnetPayResult.setStoreId(ksnetPay.getSndStoreid());
		ksnetPayResult.setServiceTypeCd(
				storeIdRepo.findById(ksnetPay.getSndStoreid()).get().getServiceTypeCode());

		String rcid = request.getParameter("reCommConId");
		String authyn = "";
		String authno = "";

		ksnet.kspay.KSPayWebHostMobileBean ipg = new ksnet.kspay.KSPayWebHostMobileBean(rcid);
		

		// KSNET 결제결과 중 아래에 나타나지 않은 항목이 필요한 경우 Null 대신 필요한 항목명을 설정할 수 있습니다.
		if (ipg.send_msg("1")) {
			authyn = ipg.getValue("authyn");
			authno = ipg.getValue("authno");

			ksnetPayResult.setAuthyn(authyn); // 성공여부
			ksnetPayResult.setAuthno(authno);

			ksnetPayResult.setTrno(ipg.getValue("trno")); // 거래번호(KSNet 고유번호)
			ksnetPayResult.setTrddt(ipg.getValue("trddt"));
			ksnetPayResult.setTrdtm(ipg.getValue("trdtm"));
			ksnetPayResult.setAmt(Integer.parseInt(ipg.getValue("amt")));
			ksnetPayResult.setMsg1(ipg.getValue("msg1"));
			ksnetPayResult.setMsg2(ipg.getValue("msg2"));
			ksnetPayResult.setOrdno(ipg.getValue("ordno"));
			ksnetPayResult.setResult(ipg.getValue("result"));
			ksnetPayResult.setIsscd(ipg.getValue("isscd"));
			ksnetPayResult.setAqucd(ipg.getValue("aqucd"));
			
			if (null != authyn && 1 == authyn.length()) {
//				ipg.send_msg("3");
			}
			
		}

		ksnetPayResult = ksnetPayResultRepo.save(ksnetPayResult);

		// 결제 결과 PUSH 발송
		pushService.sendPush(ksnetPayResult);

		model.addAttribute("sndReply", ksnetPay.getSndReply());
		model.addAttribute("reCommConId", rcid);
		model.addAttribute("reCommType", request.getParameter("reCommType"));
		model.addAttribute("reHash", request.getParameter("reHash"));

		return "ksnet/m/finish";
	}
	
	private void putProfitInto(KsnetPayResult ksnetPayResult) {							
		int profitPg=0 ; 
		int profitHead=0 ;
		int profitBranch=0 ;
		int profitAgency=0 ;
		int profitStore=0 ;
		
		int totalTransFee=0 ;
		
		KsnetPay ksnetPay=ksnetPayResult.getKsnetPay();
		int amt=ksnetPayResult.getAmt();
		
		if(StoreService.SERVICE_TYPE_INSTANT.equals(ksnetPayResult.getServiceTypeCd())) {//순간거래			
			int profitPg0=(int)( amt*ksnetPay.getInstantFeePg()*0.01 );
			int transFeePg=ksnetPay.getTransFeePg();
			profitPg= profitPg0+transFeePg;
			
			int profitHead0=(int)( amt*ksnetPay.getInstantFeeHead()*0.01 );
			int transFeeHead=ksnetPay.getTransFeeHead();
			profitHead= profitHead0+transFeeHead; //본사수익 : 매출액*본사순간정산수수료 + 본사순간송금수수료, 소숫점 이하 버림
			
			int profitBranch0= (int)( amt*ksnetPay.getInstantFeeBranch()*0.01 );; 
			int transFeeBranch=ksnetPay.getTransFeeBranch();
			profitBranch= profitBranch0+transFeeBranch;; 
			
			int profitAgency0= (int)( amt*ksnetPay.getInstantFeeAgency()*0.01 );; 
			int transFeeAgency=ksnetPay.getTransFeeAgency();
			profitAgency= profitAgency0+transFeeAgency; 
			
			int profit0= profitPg0+profitHead0+profitBranch0+profitAgency0;
			totalTransFee= transFeePg+transFeeHead+transFeeBranch+transFeeAgency;
			profitStore= (int)( amt-(profit0)*1.1-totalTransFee*1.1 ); //상점 정산금액 : 매출액 – ( 매출액 *(순간정산수수료)*1.1) – 순간송금수수료*1.1		
		}else { //일반거래
			int profitPg0=(int)( amt*ksnetPay.getFeePg()*0.01 );
			//int transFeePg=ksnetPay.getTransFeePg();
			profitPg= profitPg0;
			
			int profitHead0=(int)( amt*ksnetPay.getFeeHead()*0.01 );
			//int transFeeHead=ksnetPay.getTransFeeHead();
			profitHead= profitHead0; //본사수익 : 매출액*본사순간정산수수료 , 소숫점 이하 버림
			
			int profitBranch0= (int)( amt*ksnetPay.getFeeBranch()*0.01 );
			//int transFeeBranch=ksnetPay.getTransFeeBranch();
			profitBranch= profitBranch0;
			
			int profitAgency0= (int)( amt*ksnetPay.getFeeAgency()*0.01 );
			//int transFeeAgency=ksnetPay.getTransFeeAgency();
			profitAgency= profitAgency0;
			
			int profit0= profitPg0+profitHead0+profitBranch0+profitAgency0;
			totalTransFee= 0;
			profitStore= (int)( amt-(profit0)*1.1 ); //상점 정산금액 : 매출액 – ( 매출액 *(일반정산수수료)*1.1)				
		}
		
		ksnetPayResult.setProfitPg(profitPg);		
		ksnetPayResult.setProfitHead(profitHead);
		ksnetPayResult.setProfitBranch(profitBranch);
		ksnetPayResult.setProfitAgency(profitAgency);
		ksnetPayResult.setProfitStore(profitStore);
		
		ksnetPayResult.setTotalTransFee(totalTransFee);
	}	
		
	
}
