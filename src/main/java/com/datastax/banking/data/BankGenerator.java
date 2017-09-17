package com.datastax.banking.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.banking.model.Account;
import com.datastax.banking.model.Customer;
import com.datastax.banking.model.Transaction;
import com.datastax.banking.service.BankService;

public class BankGenerator {

	private static final Logger logger = LoggerFactory.getLogger(BankGenerator.class);
	private static final int BASE = 1000000;
	private static final int DAY_MILLIS = 1000 * 60 *60 * 24;
	private static AtomicInteger customerIdGenerator = new AtomicInteger(1);
	private static List<String> accountTypes = Arrays.asList("Current", "Joint Current", "Saving", "Mortgage", "E-Saving", "Deposit");
	private static List<String> accountIds = new ArrayList<String>();
	private static Map<String, List<Account>> accountsMap = new HashMap<String, List<Account>>();
	
	//We can change this from the Main
	public static DateTime date = new DateTime().minusDays(180).withTimeAtStartOfDay();
	
	public static List<String> whiteList = new ArrayList<String>();
			

	public static String getRandomCustomerId(int noOfCustomers){
		return BASE + new Double(Math.random()*noOfCustomers).intValue() + "";
	}
	
	public static Customer createRandomCustomer(int noOfCustomers) {
		
		String customerId = BASE + customerIdGenerator.getAndIncrement() + "";
		Map<String, String> emails = new HashMap<String, String>();
		Map<String, String> social = new HashMap<String, String>();

		emails.put("work", customerId + "@gmail.com");
		social.put("twitter", "Tweet" + customerId);
		
		Customer customer = new Customer();
		customer.setCustomerId(customerId);
		customer.setFirst("First-" + customerId);
		customer.setLast("Last-" + customerId);
		
		return customer;
	}
	
	public static List<Account> createRandomAccountsForCustomer(String customerId, int noOfCustomers) {
		
		int noOfAccounts = Math.random() < .1 ? 4 : 3;
		
		List<Account> accounts = new ArrayList<Account>();
		Set<String> customerIds = new HashSet<String>();
		customerIds.add(customerId);
		
		for (int i = 0; i < noOfAccounts; i++){
			
			Account account = new Account();
			account.setCustomerId(customerId);
			account.setAccountNo(UUID.randomUUID().toString());
			account.setAccountType(accountTypes.get(i));
			account.setBalance(Math.random() * 1000);
			account.setCurrency("EUR");
			account.setIban(new Double(Math.random() * 100000000d).intValue()	  + "");
			account.setLastUpdateBalance(new Date());
			
			if (i == 3){
				//add Joint account
				customerIds.add(getRandomCustomerId(noOfCustomers));
			}
			account.setCustomers(customerIds);
			accounts.add(account);
			
			//Keep a list of all Account Nos to create the transactions
			accountIds.add(account.getAccountNo());
		}
		
		return accounts;
	}	
	
	public static Transaction createRandomTransaction(int noOfDays, int noOfCustomers, BankService bankService) {

		int noOfMillis = noOfDays * DAY_MILLIS;
		// create time by adding a random no of millis 
		DateTime newDate = date.plusMillis(new Double(Math.random() * noOfMillis).intValue() + 1);
		
		return createRandomTransaction(newDate, noOfCustomers, bankService);
	}

	public static Transaction createRandomTransaction(DateTime newDate, int noOfCustomers, BankService bankService) {

		//Random account	
		String customerId = getRandomCustomerId(noOfCustomers);

		List<Account> accounts;
		if (accountsMap.containsKey(customerId)){
			accounts = accountsMap.get(customerId);
		}else{
			accounts = bankService.getAccounts(customerId);
			accountsMap.put(customerId, accounts);
		}

		if (accounts.size() == 0){
			return null;
		}

		Account account = accounts.get(new Double(Math.random() * accounts.size()).intValue());
			
		int noOfItems = new Double(Math.ceil(Math.random() * 3)).intValue();
		String location = locations.get(new Double(Math.random() * locations.size()).intValue());

		int randomLocation = new Double(Math.random() * issuers.size()).intValue();
		String issuer = issuers.get(randomLocation);
		String note = notes.get(randomLocation);
		String tag = tagList.get(randomLocation);
		Set<String> tags = new HashSet<String>();
		tags.add(note);
		tags.add(tag);

		Transaction transaction = new Transaction();
		createItemsAndAmount(noOfItems, transaction);
		transaction.setAccountNo(account.getAccountNo());
		transaction.setCustomers(account.getCustomers());
		transaction.setMerchant(issuer);
		transaction.setTransactionId(UUID.randomUUID().toString());
		transaction.setTransactionTime(newDate.toDate());
		transaction.setLocation(location);
		transaction.setTags(tags);
		return transaction;
	}
	
	/**
	 * Creates a random transaction with some skew for some accounts.
	 * @param noOfCreditCards
	 * @return
	 */
	
	private static void createItemsAndAmount(int noOfItems, Transaction transaction) {
		Map<String, Double> items = new HashMap<String, Double>();
		double totalAmount = 0;

		for (int i = 0; i < noOfItems; i++) {

			double amount = new Double(Math.random() * 100);
			items.put("item" + i, amount);

			totalAmount += amount;
		}
		transaction.setAmount(totalAmount);
	}
	
	public static List<String> locations = Arrays.asList("London", "Manchester", "Liverpool", "Glasgow", "Dundee",
			"Birmingham", "Dublin", "Devon");

	public static List<String> issuers = Arrays.asList("Tesco", "Sainsbury", "Asda Wal-Mart Stores", "Morrisons",
			"Marks & Spencer", "Boots", "John Lewis", "Waitrose", "Argos", "Co-op", "Currys", "PC World", "B&Q",
			"Somerfield", "Next", "Spar", "Amazon", "Costa", "Starbucks", "BestBuy", "Wickes", "TFL", "National Rail",
			"Pizza Hut", "Local Pub");

	public static List<String> notes = Arrays.asList("Shopping", "Shopping", "Shopping", "Shopping", "Shopping",
			"Pharmacy", "HouseHold", "Shopping", "Household", "Shopping", "Tech", "Tech", "Diy", "Shopping", "Clothes",
			"Shopping", "Amazon", "Coffee", "Coffee", "Tech", "Diy", "Travel", "Travel", "Eating out", "Eating out");

	public static List<String> tagList = Arrays.asList("Home", "Home", "Home", "Home", "Home", "Home", "Home", "Home",
			"Work", "Work", "Work", "Home", "Home", "Home", "Work", "Work", "Home", "Work", "Work", "Work", "Work",
			"Work", "Work", "Work", "Work", "Expenses", "Luxury", "Entertaining", "School");

}
