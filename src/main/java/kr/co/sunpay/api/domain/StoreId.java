package kr.co.sunpay.api.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "SP_STORE_IDS")
@Where(clause = "DELETED<>1")
@SQLDelete(sql = "UPDATE SP_STORE_IDS SET DELETED=1 WHERE UID=?")
public class StoreId extends BaseEntity {

	@ApiModelProperty(notes="KSNet 상점 ID")
	@Column(name="ID", length=20)
	private String id;
	
	@ApiModelProperty(notes="정산 타입 코드")
	@Column(name="SERVICE_TYPE_CD", length=20)
	private String serviceTypeCode;
	
	@ApiModelProperty(notes="활성화 여부(true: 사용중, false: 미사용)")
	@Column(name="ACTIVATED")
	private Boolean activated;
}
