package kr.co.sunpay.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.sunpay.api.domain.ContactOto;

public interface ContactOtoRepository extends JpaRepository<ContactOto, Integer>{

	Optional<ContactOto> findByUid(int uid);
	List<ContactOto> findByCreatedDateBetween(LocalDateTime createdDateStart, LocalDateTime createdDateEnd);
	List<ContactOto> findByCreatedDateBetweenAndTypeCode(LocalDateTime createdDateStart, LocalDateTime createdDateEnd, String typeCode);
	List<ContactOto> findByCreatedDateBetweenAndWriterContaining(LocalDateTime createdDateStart, LocalDateTime createdDateEnd, String writer);
	List<ContactOto> findByCreatedDateBetweenAndWriterContainingAndTypeCode(LocalDateTime createdDateStart, LocalDateTime createdDateEnd, String writer, String typeCode);
}
