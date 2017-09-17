package com.datastax.banking.model;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.datastax.driver.mapping.annotations.*;

@UDT(name = "email", keyspace = "bank")
class Email {
	private String email_type;
	private String email_address;
	private String email_status;
	public String getEmail_type() {
		return email_type;
	}
	public void setEmail_type(String email_type) {
		this.email_type = email_type;
	}
	public String getEmail_address() {
		return email_address;
	}
	public void setEmail_address(String email_address) {
		this.email_address = email_address;
	}
	public String getEmail_status() {
		return email_status;
	}
	public void setEmail_status(String email_status) {
		this.email_status = email_status;
	}
}
@UDT(name = "phone", keyspace = "bank")
class Phone {
	private String phone_type;
	private String phone_number;
	public String getPhone_type() {
		return phone_type;
	}
	public void setPhone_type(String phone_type) {
		this.phone_type = phone_type;
	}
	public String getPhone_number() {
		return phone_number;
	}
	public void setPhone_number(String phone_number) {
		this.phone_number = phone_number;
	}
}

@Table(keyspace = "bank", name = "customer")
public class Customer {
	@PartitionKey
	@Column(name="customer_id")
	private String customerId;
	private String address_line1;
	private String address_line2;
	private String address_type;
	private String bill_pay_enrolled;
	private String city;
	private String country_code;
	private String created_by;
	private String created_datetime;
	private String customer_nbr;
	private List<String> customer_id_list;
	private Set<String> customer_id_set;
	private String customer_origin_system;
	private String customer_status;
	private String customer_type;
	private String date_of_birth;
	@Frozen
	private List<Email> email_address;
	private String first;
	private String full_name;
	private String gender;
	private String government_id;
	private String government_id_type;
	private String last;
	private Date last_updated;
	private String last_updated_by;
	private String middle_name;
	@Frozen
    private List<Phone> phone_numbers;
	private String prefix;
	private String query_helper_column;
	private String state_abbreviation;
	private String zipcode;
	private String zipcode4;
	private Map<String,String> externalSystemIds;
	private Map<String,String> social;
	private Map<String,String> emails;
	private Map<String,String> phones;
	private Set<String> accounts;
	
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	public String getFirst() {
		return first;
	}
	public void setFirst(String first) {
		this.first = first;
	}
	public String getLast() {
		return last;
	}
	public void setLast(String last) {
		this.last = last;
	}
	public Map<String, String> getExternalSystemIds() {
		return externalSystemIds;
	}

	public Set<String> getAccounts() {
		return accounts;
	}
	public void setAccounts(Set<String> accounts) {
		this.accounts = accounts;
	}
	public List<Email> getEmails() {
		return email_address;
	}
	public void setEmails(List<Email> email_list) {
		this.email_address = email_list;
	}
	public List<Phone> getPhones() {
		return phone_numbers;
	}
	public void setPhones(List<Phone> phone_numbers) {
		this.phone_numbers = phone_numbers;
	}
	public void addPhone (String phone_number,String phone_type) {
		Phone phone = new Phone();
		phone.setPhone_number(phone_number);
		phone.setPhone_type(phone_type);
		this.phone_numbers.add(phone);
	}
	public void addEmail (String email_address,String email_type, String email_status) {
		Email email = new Email();
		email.setEmail_address(email_address);
		email.setEmail_type(email_type);
		email.setEmail_status(email_type);
		this.email_address.add(email);
	}
	public void addEmail (Email email) {
		this.email_address.add(email);
	}


}
