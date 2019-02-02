package kr.co.sunpay.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
