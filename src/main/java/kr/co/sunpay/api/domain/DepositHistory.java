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
@Table(name="SP_DEPOSIT_HISTORY")
@ToString
public class DepositHistory {

	@ApiModelProperty(hidden=true)
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="UID")
	private int uid;
	
	@ApiModelProperty(hidden=true)
	@Column(name="CREATED_DT")
	@CreationTimestamp
	private LocalDateTime createdDate;
	
	@JsonBackReference(value="store")
	@ManyToOne
	@JoinColumn(name="STORE_UID_FK")
	private Store store;

	@ApiModelProperty(notes="입금요청 시 전송된 입금번호")
	@Column(name="INPUT_DEPOSIT_NO")
	private String inputDepositNo;
	
	@Column(name="DEPOSIT_NO")
	private String depositNo;
	
	@Column(name="TYPE_CD")
	private String typeCd;
	
	@Column(name="ORDER_NO")
	private String orderNo;
	
	@Column(name="AMOUNT")
	private int amt;
	
}
