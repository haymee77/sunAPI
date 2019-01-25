package kr.co.sunpay.api.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name="SP_DEPOSIT_LOGS")
@ToString
public class DepositLog {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="UID")
	private int uid;
	
	@Column(name="CREATED_DT")
	@CreationTimestamp
	private LocalDateTime createdDate;
	
	@JsonBackReference(value="store")
	@ManyToOne
	@JoinColumn(name="STORE_UID_FK")
	private Store store;

	@ApiModelProperty(notes="입금요청 시 전송된 입금번호")
	@Column(name="ORIGINAL_DEPOSIT_NO")
	private String originalDepositNo;
	
	@Column(name="DEPOSIT_NO")
	private String depositNo;
	
	@Column(name="TR_NO")
	private String trNo;
	
	@Column(name="TYPE_CD")
	private String typeCd;
	
	@Column(name="STATUS_CD")
	private String statusCd;
	
	@Column(name="AMOUNT")
	private int amt;
	
	@Column(name="TOTAL")
	private int total;
	
	public DepositLog(Store store, String originalDepositNo, String depositNo, String typeCd, String trNo, String statusCd, int amt, int total) {
		this.store = store;
		this.originalDepositNo = originalDepositNo;
		this.depositNo = depositNo;
		this.typeCd = typeCd;
		this.trNo = trNo;
		this.statusCd = statusCd;
		this.amt = amt;
		this.total = total;
	}
	
}
