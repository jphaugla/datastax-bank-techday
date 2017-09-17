package com.datastax.banking.model;

import java.util.Date;
import java.util.Set;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(keyspace = "bank", name = "transaction")
public class Transaction {

	@PartitionKey
	@Column(name = "account_no")
	private String accountNo;

	@ClusteringColumn
	@Column(name = "transaction_time")
	private Date transactionTime;

	@Column(name = "transaction_id")
	private String transactionId;

	private String bucket = "1";
	private String location;
	private String merchant;
	private Double amount;
	private Set<String> customers;
	private Set<String> tags;

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public Date getTransactionTime() {
		return transactionTime;
	}

	public void setTransactionTime(Date transactionTime) {
		this.transactionTime = transactionTime;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getMerchant() {
		return merchant;
	}

	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Set<String> getCustomers() {
		return customers;
	}

	public void setCustomers(Set<String> customers) {
		this.customers = customers;
	}

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public enum Status {
		CHECK, APPROVED, DECLINED, CLIENT_APPROVED, CLIENT_DECLINED, CLIENT_APPROVAL, TIMEOUT
	}
}
