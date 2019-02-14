package kr.co.sunpay.api.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name="SP_CONFIG")
@Where(clause = "(DELETED IS NULL OR DELETED<>1)")
@SQLDelete(sql = "UPDATE SP_CONFIG SET DELETED=1 WHERE UID=?")
@ToString
public class Config extends BaseEntity {

	@Column(name="SITE_CD", length=15)
	private String siteCode;
	
	@Column(name="SITE_NM", length=50)
	private String siteName;
	
	@Column(name="FEE_PG", columnDefinition="DEFAULT 0.0")
	private Double feePg = 0.0;
	
	@Column(name="TRANS_FEE_PG", columnDefinition="DEFAULT 0")
	private Integer transFeePg = 0;
	
}
