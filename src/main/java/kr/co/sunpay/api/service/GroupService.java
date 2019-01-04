package kr.co.sunpay.api.service;

import java.net.URI;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import kr.co.sunpay.api.domain.Group;
import kr.co.sunpay.api.repository.GroupRepository;

@Service
public class GroupService {

	@Autowired
	GroupRepository groupRepo;
	
	public List<Group> getGroups() {
		
		if (groupRepo.count() < 1) {
			throw new EntityNotFoundException("등록된 본사/지사/대리점이 없습니다.");
		}
		
		return groupRepo.findAll();
	}
	
	public ResponseEntity<Object> createGroup(Group group) {
		
		group.setDeleted(false);
		Group newGroup = groupRepo.save(group);
		
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uid}")
						.buildAndExpand(newGroup.getUid()).toUri();
		
		return ResponseEntity.created(location).build();
	}
}
