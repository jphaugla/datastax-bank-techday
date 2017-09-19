package com.datastax.banking.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.util.concurrent.ListenableFuture;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.banking.data.BankGenerator;
import com.datastax.banking.model.Account;
import com.datastax.banking.model.Customer;
import com.datastax.banking.model.Transaction;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.Mapper.Option;

import static com.datastax.driver.mapping.Mapper.Option.saveNullFields;
import static com.datastax.driver.mapping.Mapper.Option.tracing;

/**
 * Inserts into 2 tables
 * 
 * @author patrickcallaghan
 *
 */
public class BankDao {

	private static Logger logger = LoggerFactory.getLogger(BankDao.class);
	private Session session;

	private static String keyspaceName = "bank";

	private static String transactionTable = keyspaceName + ".transaction";
	private static String accountsTable = keyspaceName + ".account";

	private static final String GET_CUSTOMER_ACCOUNTS = "select * from " + accountsTable + " where customer_id = ?";
	private static final String GET_TRANSACTIONS_BY_ID = "select * from " + transactionTable + " where account_no = ? and bucket = ?";
	private static final String GET_TRANSACTIONS_BY_TIMES = "select * from " + transactionTable
			+ " where account_no = ? and bucket = ? and transaction_time >= ? and transaction_time < ?";
	private static final String GET_TRANSACTIONS_SINCE = "select * from " + transactionTable
			+ " where account_no = ? and bucket = ? and transaction_time >= ?";
	private static final String GET_ALL_ACCOUNT_CUSTOMERS = "select * from " + accountsTable;
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
	
	private PreparedStatement getTransactionByAccountId;
	private PreparedStatement getTransactionBetweenTimes;
	private PreparedStatement getTransactionSinceTime;
	private PreparedStatement getLatestTransactionByCCno;
	private PreparedStatement getCustomerAccounts;
	

	private AtomicLong count = new AtomicLong(0);
	private Mapper<Customer> customerMapper;
	private Mapper<Account> accountMapper;
	private Mapper<Transaction> transactionMapper;
	
	private PreparedStatement getAccountCustomers;
	

	public BankDao(String[] contactPoints) {

		Cluster cluster = Cluster.builder().addContactPoints(contactPoints).build();

		this.session = cluster.connect();

		this.getTransactionByAccountId = session.prepare(GET_TRANSACTIONS_BY_ID);
		this.getTransactionBetweenTimes = session.prepare(GET_TRANSACTIONS_BY_TIMES);
		this.getTransactionSinceTime = session.prepare(GET_TRANSACTIONS_SINCE);
		this.getAccountCustomers = session.prepare(GET_ALL_ACCOUNT_CUSTOMERS);
		this.getCustomerAccounts = session.prepare(GET_CUSTOMER_ACCOUNTS);
		
		customerMapper = new MappingManager(this.session).mapper(Customer.class);
		customerMapper.setDefaultSaveOptions(saveNullFields(false));
		accountMapper = new MappingManager(this.session).mapper(Account.class);
		accountMapper.setDefaultSaveOptions(saveNullFields(false));
		transactionMapper = new MappingManager(this.session).mapper(Transaction.class);
		transactionMapper.setDefaultSaveOptions(saveNullFields(false));
	}
	
	public Map<String, Set<String>> getAccountCustomers(){
		ResultSet rs = this.session.execute(this.getAccountCustomers.bind());
		Map<String, Set<String>> accountCustomers = new HashMap<String, Set<String>>();
		Iterator<Row> iterator = rs.iterator();
		
		while (iterator.hasNext()){
			Row row = iterator.next();
		
			accountCustomers.put(row.getString("account_no"), row.getSet("customers", String.class));
			
			if (accountCustomers.size() % 10000==0){
				logger.info(accountCustomers.size() + " loaded.");
			}
		}
		return accountCustomers;
	}

	public void saveTransaction(Transaction transaction) {
		insertTransactionAsync(transaction);
	}

	public void insertTransactionsAsync(List<Transaction> transactions) {

		
		for (Transaction transaction : transactions) {
			checkBucketForTransaction(transaction);
			transactionMapper.save(transaction);

			long total = count.incrementAndGet();
			if (total % 10000 == 0) {
				logger.info("Total transactions processed : " + total);
			}
		}
	}

	public void insertTransactionAsync(Transaction transaction) {

		checkBucketForTransaction(transaction);
		
		transactionMapper.save(transaction);
		
		long total = count.incrementAndGet();

		if (total % 10000 == 0) {
			logger.info("Total transactions processed : " + total);
		}
	}

	private void checkBucketForTransaction(Transaction transaction) {
		if (BankGenerator.whiteList.contains(transaction.getAccountNo())){
			transaction.setBucket(formatter.format(new DateTime(transaction.getTransactionTime()).toDate()));
		}		
	}

	public List<Transaction> getTransactions(String accountId) {

		ResultSetFuture rs = this.session.executeAsync(this.getTransactionByAccountId.bind(accountId, "1"));
		
		return this.processResultSet(rs.getUninterruptibly(), null);
	}

	public void insertAccount(Account account) {
		accountMapper.saveAsync(account);
	}

	public void insertCustomer(Customer customer) {		
		customerMapper.saveAsync(customer);
		
		long total = count.incrementAndGet();

		if (total % 10000 == 0) {
			logger.info("Total customers processed : " + total);
		}
	}
	
	public Customer getCustomer(String customerId){
		return customerMapper.get(customerId);
	}

	private Transaction rowToTransaction(Row row) {

		Transaction t = new Transaction();

		t.setAmount(row.getDouble("amount"));
		t.setAccountNo(row.getString("account_no"));
		t.setMerchant(row.getString("merchant"));
		t.setLocation(row.getString("location"));
		t.setTransactionId(row.getString("transaction_id"));
		t.setTransactionTime(row.getTimestamp("transaction_time"));
		t.setTags(row.getSet("tags", String.class));

		return t;
	}

	public List<Transaction> getLatestTransactionsForCCNoTagsAndDate(String acountNo, Set<String> tags, DateTime from,
			DateTime to) {
		ResultSet resultSet = this.session.execute(getLatestTransactionByCCno.bind(acountNo, from.toDate(), to.toDate()));
		return processResultSet(resultSet, tags);
	}

	public List<Transaction> getTransactionsForCCNoTagsAndDate(String acountNo, Set<String> tags, DateTime from, DateTime to) {
		ResultSet resultSet = this.session.execute(getTransactionBetweenTimes.bind(acountNo, from.toDate(), to.toDate()));

		return processResultSet(resultSet, tags);
	}

	public List<Transaction> getTransactionsSinceTime(String acountNo, DateTime from) {
		ResultSet resultSet = this.session.execute(getTransactionSinceTime.bind(acountNo, from.toDate()));

		return processResultSet(resultSet, null);
	}

	private List<Transaction> processResultSet(ResultSet resultSet, Set<String> tags) {
		List<Row> rows = resultSet.all();
		List<Transaction> transactions = new ArrayList<Transaction>();

		for (Row row : rows) {

			Transaction transaction = rowToTransaction(row);

			if (tags != null && tags.size() != 0) {
				Iterator<String> iter = tags.iterator();

				// Check to see if any of the search tags are in the tags of the
				// transaction.
				while (iter.hasNext()) {
					String tag = iter.next();

					if (transaction.getTags().contains(tag)) {
						transactions.add(transaction);
						break;
					}
				}
			} else {
				transactions.add(transaction);
			}
		}
		return transactions;
	}

	public List<Account> getCustomerAccounts(String customerId) {
		ResultSet results = session.execute(getCustomerAccounts.bind(customerId));
		
		Result<Account> accounts = accountMapper.map(results);
		
		return accounts.all();
	}
}
