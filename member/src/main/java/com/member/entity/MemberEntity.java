package com.member.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
@Table(name="member")
public class MemberEntity {

	@Id
	@Column(name = "user_id")
	private String userId; //아이디
	
	@Column
	private String password; //패스워드
	
	@Column
	private String name; //이름
	
	@Column(name = "reg_no")
	private String regNo; //주민등록번호

	@Builder
	public MemberEntity(String userId, String password, String name, String regNo) {
		super();
		this.userId = userId;
		this.password = password;
		this.name = name;
		this.regNo = regNo;
	}
	
}
