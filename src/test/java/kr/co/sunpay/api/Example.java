package kr.co.sunpay.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import kr.co.sunpay.api.domain.Group;
import kr.co.sunpay.api.domain.KsnetPayResult;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.domain.StoreId;
import kr.co.sunpay.api.repository.GroupRepository;
import kr.co.sunpay.api.repository.KsnetPayResultRepository;
import kr.co.sunpay.api.repository.StoreRepository;
import kr.co.sunpay.api.service.GroupService;
import kr.co.sunpay.api.service.PushService;
import kr.co.sunpay.api.service.StoreService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Example {

	@Autowired
	GroupService groupService;
	
	@Autowired
	StoreService storeService;

	@Autowired
	PasswordEncoder pwEncoder;
	
	@Autowired
	GroupRepository groupRepo;

	@Autowired
	StoreRepository storeRepo;

	@Autowired
	KsnetPayResultRepository ksnetPayResultRepo;
	
	@Autowired
	PushService pushService;
	
	@Test
	public void pushTest() {
		// 결제 성공 시에만 PUSH 발솔
			Map<String, String> msg = new HashMap<String, String>();
			msg.put("cate", "paid");
			msg.put("isDisplay", "Y");
			msg.put("title", "상품 결제 완료");
			msg.put("message", "테스트..");
			
			pushService.push(msg);
	}

	@Test
	public void createStore() {
		Store store = new Store();
		
		store.setGroup(groupRepo.findByRoleCode(GroupService.ROLE_HEAD).get());
		store.setBizName("본사상점190201005");
		store.setBizOwner("가나마");
		
		List<StoreId> ids = new ArrayList<StoreId>();
		StoreId id = new StoreId();
		id.setServiceTypeCode("INSTANT");
		id.setActivated(true);
		ids.add(id);
		
		List<Member> members = new ArrayList<Member>();
		Member member = new Member();
		member.setId("htest190201005");
		member.setName("가나마");
		member.setPassword(pwEncoder.encode("test1234"));
		members.add(member);
		store.setMembers(members);
		
		store.setStoreIds(ids);
		storeRepo.save(store);
	}

	@Test
	public void testQuery1() {
		storeRepo.findByBizNo("123").forEach(row -> System.out.println(Arrays.toString(row)));
	}

	@Test
	public void testQuery2() throws Exception {
		Optional<KsnetPayResult> oPayResult = ksnetPayResultRepo.findByTrnoAndStoreIdAndAuthyn("169630089300",
				"2999199999", "O");
		if (!oPayResult.isPresent()) {
			throw new Exception("결제 정보를 찾을 수 없음");
		}

		System.out.println("-- RESULT");
		System.out.println(oPayResult.get().getAmt());
	}
}
