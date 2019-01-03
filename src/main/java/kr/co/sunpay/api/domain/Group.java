package kr.co.sunpay.api.domain;

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
@Table(name="SP_GROUPS")
@Where(clause="DELETED<>1")
@SQLDelete(sql="UPDATE SP_GROUPS SET DELETED=1 WHERE UID=?")
@ToString
public class Group extends BaseEntity {
	
}
