package kr.co.sunpay.api.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.co.sunpay.api.domain.KsnetPay;
import kr.co.sunpay.api.domain.KsnetPayResult;
import kr.co.sunpay.api.repository.KsnetPayRepository;
import kr.co.sunpay.api.repository.KsnetPayResultRepository;
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

	/**
	 * 결제데이터 받아서 저장 및 KSNet 통신 시작
	 * 
	 * @param ksnetPay
	 * @param model
	 */
	@RequestMapping("/init")
	public void init(KsnetPay ksnetPay, Model model) {
		log.info("-- KsnetWrapperController.init called...");
		KsnetPay newPay = ksnetPayRepo.save(ksnetPay);
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
		ksnetPayResult.setKsnetPay(ksnetPay);

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
			ksnetPayResult.setAmt(ipg.kspay_get_value("amt"));
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
		
		model.addAttribute("sndReply", ksnetPay.getSndReply());
		model.addAttribute("reCommConId", rcid);
		model.addAttribute("reCommType", request.getParameter("reCommType"));
		model.addAttribute("reHash", request.getParameter("reHash"));
		
	}
}