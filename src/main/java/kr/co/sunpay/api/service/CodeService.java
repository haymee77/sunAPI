package kr.co.sunpay.api.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.sunpay.api.domain.Code;
import kr.co.sunpay.api.repository.CodeRepository;

@Service
public class CodeService {

	@Autowired
	CodeRepository repo;
	
	@PersistenceContext
	EntityManager entityManager;
	
	public List<Code> getCodes() {
		if (repo.count() < 1) {
			throw new EntityNotFoundException("코드가 존재하지 않습니다.");
		}
		return repo.findAll();
	}
	
	public List<Code> getCodes(String groupName) {
		
		if (repo.countByGroupName(groupName) < 1) {
			throw new EntityNotFoundException("해당 그룹이 존재하지 않습니다.");
		}
		return repo.findByGroupName(groupName);
	}
	
	public List<String> getGroupList() {
		
		List<String> list = repo.findGroupList();
		
		return list;
	}
}
