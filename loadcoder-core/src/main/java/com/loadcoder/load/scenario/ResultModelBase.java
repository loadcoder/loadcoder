/*******************************************************************************
 * Copyright (C) 2018 Stefan Vahlgren at Loadcoder
 * 
 * This file is part of Loadcoder.
 * 
 * Loadcoder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Loadcoder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.loadcoder.load.scenario;

public class ResultModelBase {

	private String transactionName;
	private long rt;
	private boolean reportTransaction = true;
	private boolean status = true;
	private Exception e;
	private String message;

	/**
	 * Set a message for the transaction
	 * 
	 * @param message is a {@code String} message that can be logged as a part of
	 *                the transaction in the result log
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	protected String getMessage() {
		return message;
	}

	/**
	 * Change the name of the transaction to {@code newTransactionName}
	 * 
	 * @param newTransactionName is the new transaction name Default value is the
	 *                           name set in the load method
	 */
	public void changeTransactionName(String newTransactionName) {
		this.transactionName = newTransactionName;
	}

	protected ResultModelBase(String transactionName) {
		this.transactionName = transactionName;
	}

	protected boolean getStatus() {
		return status;
	}

	/**
	 * If the transaction throws an Exception, that Exception will be returned here
	 * 
	 * @return the potentially thrown Exception. Returns null if no exception was
	 *         thrown
	 */
	public Exception getException() {
		return e;
	}

	/**
	 * @return the name of the transaction
	 */
	protected String getTransactionName() {
		return transactionName;
	}

	/**
	 * Set the status for the transaction
	 * 
	 * @param status is the status that should be set for the transaction. Default
	 *               is true
	 */
	public void setStatus(boolean status) {
		this.status = status;
	}

	protected void setResponseTime(long rt) {
		this.rt = rt;
	}

	/**
	 * Get the response time of the transaction
	 * 
	 * @return the response time in milliseconds
	 */
	public long getResponseTime() {
		return rt;
	}

	/**
	 * Set whether the transaction should be reported of not. Being reported, this
	 * means that the transaction will both be logged and that it will be a part of
	 * the list of transactions that will be handled by the potential resultUser
	 * (for example RuntimeChart)
	 * 
	 * @param reportTransaction If true, the transaction will be reported. If false,
	 *                          the transaction will not be reported. Default value
	 *                          is true
	 * 
	 */
	public void setReportTransaction(boolean reportTransaction) {
		this.reportTransaction = reportTransaction;
	}
	
	
	/**
	 * Use setReportTransaction instead
	 * @param reportTransaction if the transaction shall be reported (logged and send to the runtime user) or not.
	 */
	@Deprecated
	public void reportTransaction(boolean reportTransaction) {
		this.reportTransaction = reportTransaction;
	}

	protected boolean reportTransaction() {
		return reportTransaction;
	}

	/**
	 * Use setTransactionName instead
	 * 
	 * @param transactionName is the value of the new transaction name
	 */
	@Deprecated
	public void transactionName(String transactionName) {
		setTransactionName(transactionName);
	}

	/**
	 * Changes the name of the executed transaction from the default one stated in
	 * the load method, to {@code transactionName}
	 * 
	 * @param transactionName is the value for the new transaction name
	 */
	public void setTransactionName(String transactionName) {
		this.transactionName = transactionName;
	}

	protected void setException(Exception e) {
		this.e = e;
	}
}
