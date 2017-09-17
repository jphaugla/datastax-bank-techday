package com.datastax.banking.model;

import java.util.Date;
import java.util.Set;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(keyspace = "bank", name = "account")
public class Account {
	
	@PartitionKey
	@Column(name="customer_id")
	private String customerId;

	@ClusteringColumn
	@Column(name="account_no")
	private String accountNo;
	
	@Column(name="account_type")
	private String accountType;
	
	private Set<String> customers;
	private String iban;
	private String currency;
	private double balance;
	
	@Column(name="last_updated_balance")	
	private Date lastUpdateBalance;

	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getAccountNo() {
		return accountNo;
	}
	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}
	public String getAccountType() {
		return accountType;
	}
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	public Set<String> getCustomers() {
		return customers;
	}
	public void setCustomers(Set<String> customers) {
		this.customers = customers;
	}
	public String getIban() {
		return iban;
	}
	public void setIban(String iban) {
		this.iban = iban;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public double getBalance() {
		return balance;
	}
	public void setBalance(double balance) {
		this.balance = balance;
	}
	public Date getLastUpdateBalance() {
		return lastUpdateBalance;
	}
	public void setLastUpdateBalance(Date lastUpdateBalance) {
		this.lastUpdateBalance = lastUpdateBalance;
	}	
}

