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
import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.domain.StoreId;
import kr.co.sunpay.api.model.DepositService;
import kr.co.sunpay.api.repository.KsnetPayRepository;
import kr.co.sunpay.api.repository.KsnetPayResultRepository;
import kr.co.sunpay.api.repository.StoreIdRepository;
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

	/**
	 * 결제데이터 받아서 저장 및 KSNet 통신 시작
	 * 
	 * @param ksnetPay
	 * @param model
	 */
	@RequestMapping("/init")
	public void init(KsnetPay ksnetPay, Model model) {
		log.info("-- KsnetWrapperController.init called...");
		// 결제요청 저장
		KsnetPay newPay = ksnetPayRepo.save(ksnetPay);
		
		// ksnetPay.getSndStoreid() > 상점을 확인하기 위한 상점ID, 실제 결제는 현재 상점 Activated ID로 진행함
		// 단 예치금 부족하거나 결제한도 초과 시 일반정산으로 결제
		
		// 상점ID로 상점 검색하여 현재 Activated 상태인 상점ID 구함
		Store store = storeService.getStoreByStoreId(ksnetPay.getSndStoreid());
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
				newPay.setSndStoreid(activatedId.getId());
				ksnetPayRepo.save(newPay);
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
//				storeService.offInstantService(store);
//				depositService.pushDepositLack(activatedId);
			}

		}
		
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
		}

		ksnetPayResult = ksnetPayResultRepo.save(ksnetPayResult);

		// 결제 결과 PUSH 발송
		pushService.sendPush(ksnetPayResult);

		model.addAttribute("sndReply", ksnetPay.getSndReply());
		model.addAttribute("reCommConId", rcid);
		model.addAttribute("reCommType", request.getParameter("reCommType"));
		model.addAttribute("reHash", request.getParameter("reHash"));

	}
}
