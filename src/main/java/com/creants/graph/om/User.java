package com.creants.graph.om;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author LamHa
 *
 */
@JsonInclude(Include.NON_NULL)
public class User implements IUser {
	private long id;
	private String fullName;
	@JsonIgnore
	private String username;
	@JsonIgnore
	private String password;
	private String avatar;
	private int gender;
	private String location;
	private String birthday;
	private Long money;
	private String email;
	@JsonIgnore
	private String deviceId;


	public User() {
		location = "vn";
	}


	public long getUserId() {
		return id;
	}


	public void setUserId(long id) {
		this.id = id;
	}


	public String getFullName() {
		return fullName;
	}


	public void setFullName(String fullName) {
		this.fullName = fullName;
	}


	public String getAvatar() {
		return avatar;
	}


	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}


	public int getGender() {
		return gender;
	}


	public void setGender(int gender) {
		this.gender = gender;
	}


	public String getLocation() {
		return location;
	}


	public void setLocation(String location) {
		this.location = location;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public String getBirthday() {
		return birthday;
	}


	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public Long getMoney() {
		return money;
	}


	public void setMoney(Long money) {
		this.money = money;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getDeviceId() {
		return deviceId;
	}


	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

}
