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
	
	public void setMessage(String message){
		this.message = message;
	}
	
	protected String getMessage(){
		return message;
	}
	
	public void changeTransactionName(String newTransactionName){
		this.transactionName = newTransactionName;
	}
	
	protected ResultModelBase(String transactionName){
		this.transactionName = transactionName;
	}
	
	protected boolean getStatus(){
		return status;
	}
	
	public Exception getException(){
		return e;
	}
	
	protected String getTransacionName(){
		return transactionName;
	}
	
	public void setStatus(boolean status){
		this.status = status;
	}
	
	protected void setResponseTime(long rt){
		this.rt = rt;
	}
	
	public long getResponseTime(){
		return rt;
	}
	
	public void reportTransaction(boolean reportTransaction){
		this.reportTransaction = reportTransaction;
	}
	
	public boolean reportTransaction(){
		return reportTransaction;
	}
	public void transactionName(String transactionName){
		this.transactionName = transactionName;
	}
	
	protected void setException(Exception e){
		this.e = e;
	}
}
