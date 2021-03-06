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
package org.search.nibrs.model.codes;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Enum for NIBRS OffenseSegment Codes.
 *
 */
public enum OffenseCode {
	
	_720("720", "Animal Cruelty", "A"),
	/**
	 * Arson
	 */
	_200("200", "Arson", "A"),
	_13A("13A", "Aggravated Assault", "A"),
	_13B("13B", "Simple Assault", "A"),
	_13C("13C", "Intimidation", "A"),
	_510("510", "Bribery", "A"),
	_220("220", "Burglary/Breaking & Entering", "A"),
	_250("250", "Counterfeiting/Forgery", "A"),
	_290("290", "Destruction/Damage/Vandalism of Property", "A"),
	_35A("35A", "Drug/Narcotic Violations", "A"),
	_35B("35B", "Drug Equipment Violations", "A"),
	_270("270", "Embezzlement", "A"),
	_210("210", "Extortion/Blackmail", "A"),
	_26A("26A", "False Pretenses/Swindle/Confidence Game", "A"),
	_26B("26B", "Credit Card/Automated Teller Machine Fraud", "A"),
	_26C("26C", "Impersonation", "A"),
	_26D("26D", "Welfare Fraud", "A"),
	_26E("26E", "Wire Fraud", "A"),
	_26F("26F", "Identity Theft", "A"),
	_26G("26G", "Fraud Offenses-Hacking/Computer Invasion", "A"),
	_39A("39A", "Betting/Wagering", "A"),
	_39B("39B", "Operating/Promoting/Assisting Gambling", "A"),
	_39C("39C", "Gambling Equipment Violations", "A"),
	_39D("39D", "Sports Tampering", "A"),
	_09A("09A", "Murder & Non-negligent Manslaughter", "A"),
	_09B("09B", "Negligent Manslaughter", "A"),
	/**
	 * Justifiable Homicide
	 */
	_09C("09C", "Justifiable Homicide", "A"),
	_64A("64A", "Human Trafficking, Commercial Sex Acts", "A"),
	_64B("64B", "Human Trafficking, Involuntary Servitude", "A"),
	_100("100", "Kidnapping/Abduction Person", "A"),
	_23A("23A", "Pocket-picking", "A"),
	_23B("23B", "Purse-snatching ", "A"),
	_23C("23C", "Shoplifting ", "A"),
	_23D("23D", "Theft From Building", "A"),
	_23E("23E", "Theft From Coin-Operated Machine or Device ", "A"),
	_23F("23F", "Theft From Motor Vehicle", "A"),
	_23G("23G", "Theft of Motor Vehicle Parts or Accessories ", "A"),
	_23H("23H", "All Other Larceny", "A"),
	_240("240", "Motor Vehicle Theft", "A"),
	_370("370", "Pornography/Obscene Material ", "A"),
	_40A("40A", "Prostitution ", "A"),
	_40B("40B", "Assisting or Promoting Prostitution ", "A"),
	_40C("40C", "Purchasing Prostitution ", "A"),
	_120("120", "Robbery ", "A"),
	_11A("11A", "Rape ", "A"),
	_11B("11B", "Sodomy ", "A"),
	_11C("11C", "Sexual Assault With An Object", "A"),
	_11D("11D", "Fondling ", "A"),
	_36A("36A", "Incest ", "A"),
	_36B("36B", "Statutory Rape ", "A"),
	_280("280", "Stolen PropertySegment Offenses ", "A"),
	_520("520", "Weapon Law Violations", "A"),
	_90A("90A", "Bad Checks", "B"),
	_90B("90B", "Curfew/Loitering/Vagrancy Violations", "B"),
	_90C("90C", "Disorderly Conduct", "B"),
	_90D("90D", "Driving Under the Influence", "B"),
	_90E("90E", "Drunkenness", "B"),
	_90F("90F", "Family Offenses, Nonviolent", "B"),
	_90G("90G", "Liquor Law Violations", "B"),
	_90H("90H", "Peeping Tom", "B"),
	_90I("90I", "Runaway", "B"),
	_90J("90J", "Trespass of Real Property", "B"),
	_90Z("90Z", "All Other Offenses", "B")
	;

	public String code;
	public String description;
	public String group;
	
	private OffenseCode(String code, String description, String group) {
		this.code = code;
		this.description = description;
		this.group = group;
	}
	
	public static final Set<OffenseCode> asSet() {
		return EnumSet.allOf(OffenseCode.class);
	}

	public static final Set<String> codeSet() {
		Set<String> ret = new HashSet<>();
		for (OffenseCode v : values()) {
			ret.add(v.code);
		}
		return ret;
	}
	
	public static final OffenseCode forCode(String code) {
		OffenseCode ret = null;
		for(OffenseCode c : asSet()) {
			if (c.code.equals(code)) {
				ret = c;
				break;
			}
		}
		return ret;
	}
	
	public static final boolean isCrimeAgainstPersonCode(String code) {
		return Arrays.asList(_13A.code, _13B.code, _13C.code, 
		_09A.code, _09B.code, _09C.code, _64A.code, _64B.code, _100.code,
		_11A.code, _11B.code, _11C.code, _11D.code, _36A.code,
		_36B.code).contains(code);
	}
	
	public static final boolean containsCrimeAgainstPersonCode(Collection<String> codes) {
		return codes.stream().anyMatch(code -> isCrimeAgainstPersonCode(code));
	}

	public static final boolean isCrimeAgainstSocietyCode(String code) {
		return Arrays.asList(_720.code,
		_35A.code, _35B.code, _39A.code, _39B.code,
		_39C.code, _39D.code, _370.code, _40A.code,
		_40B.code, _40C.code, _520.code, _90A.code, 
		_90B.code, _90C.code, _90D.code, _90E.code,
		_90F.code, _90G.code, _90H.code, _90J.code,
		_90Z.code).contains(code);
	}
	
	public static final boolean containsCrimeAgainstSocietyCode(Collection<String> codes) {
		return codes.stream().anyMatch(code -> isCrimeAgainstSocietyCode(code));
	}

	public static final boolean isCrimeAgainstPropertyCode(String code) {
		return Arrays.asList(_200.code,
				_510.code, _220.code, _250.code,
				_290.code, _270.code, _210.code,
				_26A.code, _26B.code, _26C.code,
				_26D.code, _26E.code, _26F.code,
				_26G.code, _23A.code, _23B.code,
				_23C.code, _23D.code, _23E.code,
				_23F.code, _23G.code, _23H.code,
				_240.code, _120.code, _280.code					
		).contains(code);
	}
	
	public static final boolean isCrimeAgainstStolenVehiclePropertyCode(String code) {
		return Arrays.asList(
				_510.code, _220.code, 
				_270.code, _210.code,
				_26A.code, _26B.code, _26C.code,
				_26D.code, _26E.code, _26F.code,
				_26G.code, _23A.code, _23B.code,
				_23C.code, _23D.code, _23E.code,
				_23F.code, _23G.code, _23H.code,
				_240.code, _120.code					
				).contains(code);
	}
	
	public static final boolean isCrimeAllowingLocationTypeCyberspace(String code) {
		return Arrays.asList(
				_210.code, _250.code, _270.code, 
				_280.code, _290.code, _370.code,  
				_510.code, _26A.code, _26B.code,  
				_26C.code, _26D.code, _26E.code, 
				_26F.code, _26G.code, _39A.code, 
				_39B.code, _39C.code, _13C.code, 
				_35A.code, _35B.code, _520.code, 
				_64A.code, _64B.code, _40A.code,  
				_40B.code, _40C.code					
				).contains(code);
	}
	
	public static final boolean isDrugNarcoticOffense(String code) {
		return Arrays.asList(_35A.code, _35B.code).contains(code);
	}
	
	public static final boolean isAggravatedAssaultHomicideCircumstancesOffense(String code) {
		return Arrays.asList(_09A.code, _09B.code, _09C.code, _13A.code).contains(code);
	}
	
	public static final boolean containsCrimeAgainstPropertyCode(Collection<String> codes) {
		return codes.stream().anyMatch(code -> isCrimeAgainstPropertyCode(code));
	}

	public static final boolean containsCrimeRequirePropertySegement(Collection<String> codes) {
		return codes.stream().anyMatch(OffenseCode::isCrimeRequirePropertySegement);
	}
	
	public static final boolean isCrimeRequirePropertySegement(String code) {
		return isCrimeAgainstPropertyCode(code)
				|| _100.code.equals(code)
				|| isGamblingOffenseCode(code)
				|| isDrugNarcoticOffense(code);
	}
	
	public static final boolean isCrimeRequireIncidentHour(String code) {
		return Arrays.asList(_09A.code, _13A.code, _13B.code, _13C.code).contains(code);
	}
	
	public static final boolean isCrimeRequiringTypeOfWeaponForceInvolved(String code) {
		return Arrays.asList(
				_09A.code,
				_09B.code,
				_09C.code,
				_100.code,
				_11A.code,
				_11B.code,
				_11C.code,
				_11D.code,
				_120.code,
				_13A.code,
				_13B.code,
				_210.code,
				_520.code,
				_64A.code,
				_64B.code).contains(code);
	}
	
	public static final boolean isGamblingOffenseCode(String code) {
		return codeMatchesRegex(code, "39[ABCD]");
	}
	public static final boolean isReturnARapeCode(String code) {
		return codeMatchesRegex(code, "11[ABC]");
	}

	private static boolean codeMatchesRegex(String code, String regex) {
		return code!= null && Pattern.compile(regex).matcher(code).matches();
	}
	
	public static final boolean containsGamblingOffenseCode(Collection<String> codes) {
		return codes.stream().anyMatch(code -> isGamblingOffenseCode(code));
	}

	public static final boolean isLarcenyOffenseCode(String code) {
		return codeMatchesRegex(code, "23[ABCDEFGH]");
	}
	
	public static final boolean containsLarcenyOffenseCode(Collection<String> codes) {
		return codes.stream().anyMatch(code -> isLarcenyOffenseCode(code));
	}
	
	public static final boolean isOffenseHavingIllogicalPropertyDescriptions(String code){
		return Arrays.asList(
				_220.code, _240.code, _23A.code, _23B.code,
				_23C.code, _23D.code, _23E.code,
				_23F.code, _23G.code, _23H.code
		).contains(code);

	}

}