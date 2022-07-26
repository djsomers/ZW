package com.zincworks.bank;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.zincworks.assessment.AppProperties.Config;
import com.zincworks.db.DbException;
import com.zincworks.db.MySQLConnection;


public final class BankManager {

	// account authentication
    private class Account {
	    
		private String accountNumber;
		//private int pin;
		private String accessToken;
		
	}

    // store list of authenticated accounts
    private ArrayList<Account> accounts;
    
    // database configuration
	private Config config = null;
	
	public BankManager(Config config) {
	
		// initialise database configuration
		this.config = config;
		
		// initialise list of authenticated accounts
		accounts = new ArrayList<Account>();
		
	}
	
	/**
	 * Authenticate access token returning a matching account number 
	 * @param accessToken String token to test
	 * @return String account number
	 */
	private final String authenticateToken(final String accessToken) {

		String accountNumber = null;
		
		// get account number for valid access token
		for(int i=-1, size=accounts.size(); ++i<size;) {
		    
			if(accounts.get(i).accessToken.equals(accessToken)) {
		    	accountNumber = accounts.get(i).accountNumber;
		    }
		}
		
		return accountNumber;
	}
	
	/**
	 * Get an account id for matching account number
	 * @param accountNumber String account number
	 * @return account id
	 * @throws BankException
	 */
	private final int getAccountId(final String accountNumber) throws BankException {
		
		MySQLConnection dbConn = new MySQLConnection();
		
		int accountId = 0;
		
		try {
    	    
    		Connection c = dbConn.getConnection(config.dbHost, config.dbPort, config.dbName, config.dbUser, config.dbPassword);
    	
	    	// get account id for matching account number
    	    PreparedStatement ps = c.prepareStatement("SELECT id FROM bank_account WHERE account_number=?", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

    	    ps.setString(1, accountNumber);
    	    
    	    final ResultSet rs = ps.executeQuery();
    	    
    	    // check that account exists
    	    if(rs.isBeforeFirst()) {
    	    	
    	    	accountId = rs.getInt("id");
    	    }
    	    
    	} catch(SQLException | DbException e) {
    		throw new BankException(e);
    	}
    	
    	return accountId;
	}
	
	/**
	 * Check the bank deposits against a specified amount
	 * @param amount BigDecimal amount to check
	 * @return boolean whether bank has enough deposits to cover amount
	 * @throws BankException
	 */
	private final boolean isAmountAvailable(final BigDecimal amount) throws BankException {
		
		MySQLConnection dbConn = new MySQLConnection();
		
		BigDecimal depositAmount = BigDecimal.ZERO;
		
		try {
			
			Connection c = dbConn.getConnection(config.dbHost, config.dbPort, config.dbName, config.dbUser, config.dbPassword);
			
			PreparedStatement ps = c.prepareStatement("SELECT SUM(denomination*qty) AS total FROM zinc_bank.bank_deposit", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			
			final ResultSet rs = ps.executeQuery();
			
			while(rs.next()) {
				
				depositAmount = new BigDecimal(rs.getInt("total"));
			}
			
			if(depositAmount.compareTo(amount) > 0) {
				return true;
			}
			
			return false;
			
		} catch(SQLException | DbException e) {
		    throw new BankException(e);	
		}
	}
	
	/**
	 * Update bank deposits and an account balance
	 * @param accountNumber String account number of account to update
	 * @param denominations HashMap denominations and quantities
	 * @param balance BigDecimal new balance
	 * @param overdraft BigDecimal new overdraft amount
	 * @return boolean returns true when successful
	 * @throws BankException
	 */
	private final boolean updateDeposits(final String accountNumber, final HashMap<Integer, Integer> denominations, final BigDecimal balance, final BigDecimal overdraft) throws BankException {
		
		MySQLConnection dbConn = new MySQLConnection();
		
		int transactionIsolation = 0;
 		boolean autoCommit = false;
 		
 		Connection c = null;
 		Connection c2 = null;
 		
		try {

			c = dbConn.getConnection(config.dbHost, config.dbPort, config.dbName, config.dbUser, config.dbPassword);
			
			transactionIsolation = c.getTransactionIsolation();
	        c.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
	        
			autoCommit = c.getAutoCommit();
			
			c.setAutoCommit(false);
     		
			PreparedStatement ps = c.prepareStatement("UPDATE zinc_bank.bank_deposit SET denomination=?, qty=? WHERE denomination=?");
			
			for (Map.Entry<Integer, Integer> entry : denominations.entrySet()) {
			
				int denomination = entry.getKey();
				int qty = entry.getValue();
				
				ps.setInt(1, denomination);
				ps.setInt(2, qty);
				ps.setInt(3, denomination);
				
				ps.addBatch();
				
				ps.executeBatch();
				
				ps.clearBatch();
				
			}
			
			// get account id to update balances
			int accountId = this.getAccountId(accountNumber);
			
			if(accountId==0) {
				
				throw new BankException("Unable to find matching account id for account number: " +accountNumber);
				
			}
			
			c2 = dbConn.getConnection(config.dbHost, config.dbPort, config.dbName, config.dbUser, config.dbPassword);
			
	        c2.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
	        
			PreparedStatement ps2 = c2.prepareStatement("UPDATE zinc_bank.account_balance SET amount=?, overdraft=? WHERE id=?");
			
			ps2.setInt(1, balance.intValue());
			ps2.setInt(2, overdraft.intValue());
			ps2.setInt(3, accountId);
				
			ps2.addBatch();
				
			ps2.executeBatch();
			
			ps2.clearBatch();
			
			// commit transactions
			c.commit();
			c2.commit();
			
			// reset auto commit
			c.setAutoCommit(autoCommit);
			
			c.setTransactionIsolation(transactionIsolation);
			
			c.close();
			
			// reset auto commit
			c2.setAutoCommit(autoCommit);
			
			c2.setTransactionIsolation(transactionIsolation);
			
			c2.close();
			
		} catch(SQLException | DbException | BankException e) {
			
			// roll back failed transactions
			try {
				
				if(c!=null) {
				
					c.rollback();
				
				    c.setAutoCommit(autoCommit);
				    c.setTransactionIsolation(transactionIsolation);
				    
				    c.close();
				}
				
				if(c2!=null) {
					
					c2.rollback();
					
				    c2.setAutoCommit(autoCommit);
				    c2.setTransactionIsolation(transactionIsolation);
				    
				    c2.close();
				}
				    
			} catch (SQLException e1) {
				
				throw new BankException(e1);
				
			}	    	
    	}
		
		return true;

	}
	
	/**
	 * Retrieve denominations and their quantities available  
	 * @return LinkedHashMap denominations and quantities
	 * @throws BankException
	 */
	private final LinkedHashMap<Integer, Integer> getDenominations() throws BankException {
		
		MySQLConnection dbConn = new MySQLConnection();
		
		// LinkedHashMap retains order
		LinkedHashMap<Integer, Integer> denominations = new LinkedHashMap<Integer, Integer>();
		
		try {

			Connection c = dbConn.getConnection(config.dbHost, config.dbPort, config.dbName, config.dbUser, config.dbPassword);
			
			PreparedStatement ps = c.prepareStatement("SELECT denomination, qty FROM zinc_bank.bank_deposit ORDER BY denomination DESC", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			
			final ResultSet rs = ps.executeQuery();
			
			while(rs.next()) {
				
				denominations.put(rs.getInt("denomination"), rs.getInt("qty"));
				
			}
			
			return denominations;
			
		} catch(SQLException | DbException e) {
		    throw new BankException(e);	
		}
	}
	
	/**
	 * Authenticate account number and pin returning an access token, which must then be used to access account services
	 * @param accountNumber String
	 * @param pin integer
	 * @return String access token
	 */
    public final HashMap<String,Object> authenticatePin(final String accountNumber, final int pin) throws BankException {
    	
    	MySQLConnection dbConn = new MySQLConnection();
    	
    	HashMap<String,Object> auth = new HashMap<String,Object>();
    	
    	try {
    	    
    		Connection c = dbConn.getConnection(config.dbHost, config.dbPort, config.dbName, config.dbUser, config.dbPassword);
    	
	    	// get account details for matching account number and pin
    	    PreparedStatement ps = c.prepareStatement("SELECT id FROM bank_account WHERE account_number=? AND pin=?", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

    	    ps.setString(1, accountNumber);
    	    ps.setInt(2, pin);
    	    
    	    final ResultSet rs = ps.executeQuery();
    	    
    	    // check that account exists
    	    if(rs.isBeforeFirst()) {
    	    	
    	    	// generate access token
    	    	// FIXME: temporary, use proper encryption
    	    	String token = new StringBuilder(accountNumber).reverse().toString();
    	    	
    	    	// add account to list of authenticated accounts
    	    	Account acc = new Account();
    	    	
    	    	acc.accountNumber = accountNumber;
    	    	// acc.pin = pin;
    	    	acc.accessToken = token;
    	    	
    	    	accounts.add(acc);
    	    	
    	    	// set authentication token 
    	    	auth.put("token", token);
    	    }
    	    
    	} catch(SQLException | DbException e) {
    		throw new BankException(e);
    	}
    	
    	if(auth.isEmpty()) {
    		auth.put("Message", "Unauthorised Access is not permitted.");
		}
    	
    	// return access token
    	return auth;
    	
    }

	/**
	 * Get details of an authenticated account
	 * @param accessToken String token generated from validated account
	 * @return HashMap account details
	 * @throws BankException
	 */
	public final HashMap<String,Object> getAccountDetails(final String accessToken) throws BankException {
		
		MySQLConnection dbConn = new MySQLConnection();
		
		HashMap<String,Object> account = new HashMap<String,Object>();
		
		// authenticate access token
		String accountNumber = this.authenticateToken(accessToken);
		
		if(accountNumber != null) {
		
			try {
				
				Connection c = dbConn.getConnection(config.dbHost, config.dbPort, config.dbName, config.dbUser, config.dbPassword);
				
				// get account details
				PreparedStatement ps = c.prepareStatement("SELECT ab.amount AS balance, ab.overdraft AS overdraft FROM bank_account ba LEFT JOIN account_balance ab ON ab.account_id=ba.id WHERE ba.account_number=?", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				
				ps.setString(1, accountNumber);
				
				final ResultSet rs = ps.executeQuery();
						
				while(rs.next()) {
					
					account.put("balance", rs.getBigDecimal("balance"));
					account.put("overdraft", rs.getBigDecimal("overdraft"));
					
				}
				
			} catch(SQLException | DbException e) {
				throw new BankException(e);
			}
			
		}
		
		if(account.isEmpty()) {
			account.put("Message", "Unauthorised Access is not permitted.");
		}
		
		return account;
	}
	
	/**
	 * Make a withdrawal
	 * @param accessToken String authenticated access token 
	 * @param amount integer amount to withdraw
	 * @return denominations and quantity required
	 * @throws BankException
	 */
	public final HashMap<String,Object> makeWithdrawal(final String accessToken, final int amount) throws BankException {
		
        MySQLConnection dbConn = new MySQLConnection();
		
		HashMap<String,Object> withdrawal = new HashMap<String,Object>();
		
		// initialise the requested withdrawal amount 
		BigDecimal withdrawalAmount = new BigDecimal(amount);
		
		// authenticate access token
		String accountNumber = this.authenticateToken(accessToken);
		
		if(accountNumber != null) {
		
			// check if the bank has enough deposits to cover withdrawal amount
			if(!isAmountAvailable(withdrawalAmount)) {
				
				withdrawal.put("Message", "Sorry, unable to complete transaction - try withdrawing a smaller amount.");
				
			} else {
				
				// get account balance and overdraft amounts
				try {
					
					Connection c = dbConn.getConnection(config.dbHost, config.dbPort, config.dbName, config.dbUser, config.dbPassword);
					
					// get account details
					PreparedStatement ps = c.prepareStatement("SELECT ab.amount AS balance, ab.overdraft AS overdraft FROM bank_account ba LEFT JOIN account_balance ab ON ab.account_id=ba.id WHERE ba.account_number=?", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
					
					ps.setString(1, accountNumber);
					
					final ResultSet rs = ps.executeQuery();
					
					BigDecimal balance = null;
					BigDecimal overdraft = null;
							
					while(rs.next()) {
						
						balance = rs.getBigDecimal("balance");
						overdraft = rs.getBigDecimal("overdraft");
						
					}
					
					// initialise remaining balance and overdraft amounts
					BigDecimal remainingBalance = balance;
					BigDecimal remainingOverdraft = overdraft;
					
					// get total available funds
					BigDecimal totalAvailable = balance.add(overdraft);
						
					// check if amount is greater than the total
					if(withdrawalAmount.compareTo(totalAvailable) > 0) {
					
						withdrawal.put("Message", "Sorry, insufficient funds.");
						
					} else {
					
						// check if the amount is greater than the current balance
					    if(withdrawalAmount.compareTo(balance) > 0) {
						
					    	// calculate amount of overdraft required
					    	BigDecimal overdraftRequired = withdrawalAmount.subtract(balance);
					    	
					    	remainingOverdraft = remainingOverdraft.subtract(overdraftRequired);
					    	
					    	// all of balance was used
					    	remainingBalance = BigDecimal.ZERO;
					    						    
					    } else {
					    	
					    	// withdraw amount from balance
					    	remainingBalance = remainingBalance.subtract(withdrawalAmount);
					    	
					    }
					}
					
					// calculate denomination of notes required
					// FIXME: use industry standard algorithm instead of contrived logic !!!
					LinkedHashMap<Integer, Integer> denominations = this.getDenominations();
					
					HashMap<Integer, Integer> denominationsRequired = new HashMap<Integer,Integer>();
					
					BigDecimal remainder = withdrawalAmount;
					
					// check how many of each is required
					for (Map.Entry<Integer, Integer> entry : denominations.entrySet()) {
						
						// check if we still need to process further 
						if(remainder.compareTo(BigDecimal.ZERO) > 0) {
							
							int denomination = entry.getKey();
							int qty = entry.getValue();
							
							// get total value of the current denomination
							BigDecimal total = new BigDecimal(denomination * qty);
							
							// check if there is a remainder
							if(total.remainder(remainder).compareTo(BigDecimal.ZERO) == 0 ) {
								
								// find out what quantity of denomination is required
								Integer quantity = total.divide(remainder).intValue();
								
								denominationsRequired.put(denomination, quantity);
								
								remainder = BigDecimal.ZERO;
								
							} else {
								
								// find out what quantity of denomination is required
								Integer quantity = Math.round(total.divide(remainder).intValue());
								
								denominationsRequired.put(denomination, quantity);
								
								remainder = total.subtract(remainder);
							}
								
						}						
					}
					
					// decrement denominations
					if(!denominationsRequired.isEmpty()) {
						
						// update the deposit amounts
						if(this.updateDeposits(accountNumber,denominationsRequired,remainingBalance,remainingOverdraft)) {
						
					        // add the required denominations to the withdrawal info
					        withdrawal.put("denominations", denominationsRequired);
					        
						} else {
							
							withdrawal.put("Message", "Sorry, unable to complete transaction.");
						}
						
					} else {
						
				    	withdrawal.put("Message", "Sorry, unable to complete transaction.");
					}
					
				} catch(SQLException | DbException e) {
					throw new BankException(e);
				}
			}
		}
		
		if(withdrawal.isEmpty()) {
			withdrawal.put("Message", "Unauthorised Access is not permitted.");
		}
		
		return withdrawal;
	}
	
}
