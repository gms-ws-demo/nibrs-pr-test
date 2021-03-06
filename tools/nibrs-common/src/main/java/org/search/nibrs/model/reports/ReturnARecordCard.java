/*
 * Copyright 2016 SEARCH-The National Consortium for Justice Information and Statistics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.search.nibrs.model.reports;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.search.nibrs.model.reports.arson.ArsonRow;
import org.search.nibrs.model.reports.humantrafficking.HumanTraffickingFormRow;

public class ReturnARecordCard implements Serializable{
	private static final long serialVersionUID = -2340194364781985383L;
	private ReturnARecordCardRow[] rows = new ReturnARecordCardRow[ReturnARecordCardRowName.values().length];
	private ReturnAFormRow[] returnAFormRows = new ReturnAFormRow[ReturnARecordCardRowName.values().length];
	private ReturnARecordCardRow arsonRow;
	private ReturnARecordCardRow[] humanTraffickingFormRows = new ReturnARecordCardRow[2]; 
	private String ori; 
	private String agencyName; 
	private String stateName;
	private String stateCode;
	private Integer population;
	private int year; 
	
	public ReturnARecordCard() {
		super();
		for (int i = 0; i < getRows().length; i++){
			getRows()[i] = new ReturnARecordCardRow(); 
			getReturnAFormRows()[i] = new ReturnAFormRow(); 
		}
		
		arsonRow = new ReturnARecordCardRow(); 
		for (int i = 0; i < 2; i++) {
			humanTraffickingFormRows[i] = new ReturnARecordCardRow();
		}
	}
	
	public ReturnARecordCard(String ori, int year) {
		this();
		this.ori = ori; 
		this.year = year; 
	}
	
	public ReturnARecordCard(int year) {
		this();
		this.year = year; 
	}
	
	public ReturnARecordCardRow[] getRows() {
		return rows;
	}

	public void setRows(ReturnARecordCardRow[] returnARecordCardRows) {
		this.rows = returnARecordCardRows;
	}

	public String getOri() {
		return ori;
	}

	public void setOri(String ori) {
		this.ori = ori;
	}

	public String getAgencyName() {
		return agencyName;
	}

	public void setAgencyName(String agencyName) {
		this.agencyName = agencyName;
	}

	public String getStateName() {
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	
	public String toString(){
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	public String getStateCode() {
		return stateCode;
	}

	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}

	public Integer getPopulation() {
		return population;
	}

	public String getPopulationString() {
		return population == null? "" : String.valueOf(population);
	}
	
	public void setPopulation(Integer population) {
		this.population = population;
	}

	public ReturnAFormRow[] getReturnAFormRows() {
		return returnAFormRows;
	}

	public void setReturnAFormRows(ReturnAFormRow[] returnAFormRows) {
		this.returnAFormRows = returnAFormRows;
	}

	public ReturnARecordCardRow getArsonRow() {
		return arsonRow;
	}

	public void setArsonRow(ReturnARecordCardRow arsonRow) {
		this.arsonRow = arsonRow;
	}

	public ReturnARecordCardRow[] getHumanTraffickingFormRows() {
		return humanTraffickingFormRows;
	}

	public void setHumanTraffickingFormRows(ReturnARecordCardRow[] humanTraffickingFormRows) {
		this.humanTraffickingFormRows = humanTraffickingFormRows;
	}

}