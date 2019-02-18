package kr.co.sunpay.api.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import kr.co.sunpay.api.domain.Trade;

@RestController
@RequestMapping("/trade")
public class TradeController {

	@GetMapping("/{memberUid}")
	@ApiOperation(value="거래조회", notes="{memberUid} 권한으로 볼 수 있는 거래건 리턴")
	public List<Trade> getTrades() {
		List<Trade> trades = new ArrayList<Trade>();
		
		return trades;
	}
}
