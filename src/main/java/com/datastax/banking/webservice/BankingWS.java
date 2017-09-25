package com.datastax.banking.webservice;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.banking.model.Account;
import com.datastax.banking.model.Customer;
import com.datastax.banking.model.Transaction;
import com.datastax.banking.service.BankService;

@WebService
@Path("/")
public class BankingWS {

	private Logger logger = LoggerFactory.getLogger(BankingWS.class);
	private SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyyMMdd");

	//Service Layer.
	private BankService bankService = BankService.getInstance();

	
	@GET
	@Path("/get/customer/{customerid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCustomer(@PathParam("customerid") String customerId) {
		
		Customer customer = bankService.getCustomer(customerId);
		
		return Response.status(Status.OK).entity(customer).build();
	}

	@GET
	@Path("/get/customerByPhone/{phoneString}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCustomerByPhone(@PathParam("phoneString") String phoneString) {

		List<Customer> customers = bankService.getCustomerByPhone(phoneString);

		return Response.status(Status.OK).entity(customers).build();
	}
	@GET
	@Path("/get/getcctransactions/{creditcardno}/{from}/{to}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCCTransactions(@PathParam("creditcardno") String ccNo, @PathParam("from") String fromDate,
	@PathParam("to") String toDate) {
		DateTime from = DateTime.now();
		DateTime to = DateTime.now();
		try {
			from = new DateTime(inputDateFormat.parse(fromDate));
			to = new DateTime(inputDateFormat.parse(toDate));
		} catch (ParseException e) {
			String error = "Caught exception parsing dates " + fromDate + "-" + toDate;

			logger.error(error);
			return Response.status(Status.BAD_REQUEST).entity(error).build();
		}

		List<Transaction> result = bankService.getTransactionsForCCNoDateSolr(ccNo, null, from, to);
		logger.info("Returned response");
		return Response.status(Status.OK).entity(result).build();
	}



	@GET
	@Path("/get/customerByFullNamePhone/{fullName}/{phoneString}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCustomerByFullNamePhone(@PathParam("fullName") String fullName,
											   @PathParam("phoneString") String phoneString) {

		List<Customer> customers = bankService.getCustomerByFullNamePhone(fullName,phoneString);

		return Response.status(Status.OK).entity(customers).build();
	}

	@GET
	@Path("/get/customerByEmail/{emailString}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCustomerByEmail(@PathParam("emailString") String emailString) {

		List<Customer> customers = bankService.getCustomerByEmail(emailString);

		return Response.status(Status.OK).entity(customers).build();
	}
	@GET
	@Path("/get/accounts/{customerid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAccounts(@PathParam("customerid") String customerId) {
		
		List<Account> accounts = bankService.getAccounts(customerId);
		
		return Response.status(Status.OK).entity(accounts).build();
	}
	
	@GET
	@Path("/get/transactions/{accountid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTransactions(@PathParam("accountid") String accountId) {
		
		List<Transaction> transactions = bankService.getTransactions(accountId);
		
		return Response.status(Status.OK).entity(transactions).build();
	}
	@GET
	@Path("/get/categorydescrip/{mrchntctgdesc}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTransactionsCTGDESC(@PathParam("mrchntctgdesc") String mrchntctgdesc) {

		List<Transaction> transactions = bankService.getTransactionsCTGDESC(mrchntctgdesc);

		return Response.status(Status.OK).entity(transactions).build();
	}

	
}
