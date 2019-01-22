package kr.co.sunpay.api;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import kr.co.sunpay.api.repository.StoreRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Example {

	@Autowired
	StoreRepository storeRepo;
	
	@Test
	public void testQuery1() {
		storeRepo.findByBizNo("123").forEach(row -> System.out.println(Arrays.toString(row)));
	}
	
	@Test
	public void testQuery2() {
		storeRepo.findByBizOwner("박혜미").forEach(row -> System.out.println(Arrays.toString(row)));
	}
}
