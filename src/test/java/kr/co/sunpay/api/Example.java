package kr.co.sunpay.api;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import kr.co.sunpay.api.domain.KsnetPayResult;
import kr.co.sunpay.api.repository.KsnetPayResultRepository;
import kr.co.sunpay.api.repository.StoreRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Example {

	@Autowired
	StoreRepository storeRepo;
	
	@Autowired
	KsnetPayResultRepository ksnetPayResultRepo;
	
	@Test
	public void testQuery1() {
		storeRepo.findByBizNo("123").forEach(row -> System.out.println(Arrays.toString(row)));
	}
	
	@Test
	public void testQuery2() throws Exception {
		Optional<KsnetPayResult> oPayResult = ksnetPayResultRepo.findByTrnoAndStoreIdAndAuthyn("169630089300", "2999199999", "O");
		if (!oPayResult.isPresent()) {
			throw new Exception("결제 정보를 찾을 수 없음");
		}
		
		System.out.println("-- RESULT");
		System.out.println(oPayResult.get().getAmt());
	}
}
