/*
 * <p> <b>jFin, open source derivatives trade processing</b> </p>
 *
 * <p> Copyright (C) 2005, 2006, 2007 Morgan Brown Consultancy Ltd. </p>
 *
 * <p> This file is part of jFin. </p>
 *
 * <p> jFin is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. </p>
 *
 * <p> jFin is distributed in the hope that it will be useful, but <b>WITHOUT
 * ANY WARRANTY</b>; without even the implied warranty of <b>MERCHANTABILITY</b>
 * or <b>FITNESS FOR A PARTICULAR PURPOSE</b>. See the GNU General Public
 * License for more details. </p>
 *
 * <p> You should have received a copy of the GNU General Public License along
 * with jFin; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * St, Fifth Floor, Boston, MA 02110-1301 USA. </p>
 */

package org.jfin.common.test;

import java.util.Iterator;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.jfin.common.Iso3166Country;
import org.jfin.common.Iso4217Currency;

public class Iso4217CurrencyTest extends TestCase
{
	public void testCurrencies()
	{
		TreeMap<String, String> currencies = new TreeMap<String, String>();
		currencies.put("AED", "UAE Dirham (United Arab Emirates)");
		currencies.put("AFN", "Afghani (Afghanistan)");
		currencies.put("ALL", "Lek (Albania)");
		currencies.put("AMD", "Armenian Dram (Armenia)");
		currencies
				.put(
						"ANG",
						"Netherlands Antillian Guilder (Netherlands Antilles) <!--(assuming Guikder is a typo) (it surely is)-->");
		currencies.put("AOA", "Kwanza (Angola)");
		currencies.put("ARS", "Argentine Peso (Argentina)");
		currencies.put("AUD", "Australian Dollar (Australia)");
		currencies.put("AWG", "Aruban Guilder (Aruba)");
		currencies.put("AZN", "Azerbaijanian Manat (Azerbaijan)");
		currencies.put("BAM", "Convertible Marks (Bosnia and Herzegovina)");
		currencies.put("BBD", "Barbados Dollar (Barbados)");
		currencies.put("BDT", "Taka (Bangladesh)");
		currencies.put("BGN", "Bulgarian Lev (Bulgaria)");
		currencies.put("BHD", "Bahraini Dinar (Bahrain)");
		currencies.put("BIF", "Burundian Franc (Burundi)");
		currencies
				.put("BMD",
						"Bermudian Dollar (customarily known as Bermuda Dollar) (Bermuda)");
		currencies.put("BND", "Brunei Dollar (Brunei)");
		currencies.put("BOB", "Boliviano (Bolivia)");
		currencies.put("BOV", "Bolivian Mvdol (Funds code) (Bolivia)");
		currencies.put("BRL", "Brazilian Real (Brazil)");
		currencies.put("BSD", "Bahamian Dollar (Bahamas)");
		currencies.put("BTN", "Ngultrum (Bhutan)");
		currencies.put("BWP", "Pula (Botswana)");
		currencies.put("BYR", "Belarussian Ruble (Belarus)");
		currencies.put("BZD", "Belize Dollar (Belize)");
		currencies.put("CAD", "Canadian Dollar (Canada)");
		currencies
				.put("CDF", "Franc Congolais (Democratic Republic of Congo) ");
		currencies.put("CHF", "Swiss Franc (Switzerland)");
		currencies.put("CLF", "Unidades de formento (Funds code) (Chile)");
		currencies.put("CLP", "Chilean Peso (Chile)");
		currencies.put("CNY", "Yuan Renminbi (People's Republic of China)");
		currencies.put("COP", "Colombian Peso (Colombia)");
		currencies.put("COU", "Unidad de Valor Real (Colombia)");
		currencies.put("CRC", "Costa Rican Colon (Costa Rica)");
		currencies.put("CSD", "Serbian Dinar (Serbia)");
		currencies.put("CUP", "Cuban Peso (Cuba)");
		currencies.put("CVE", "Cape Verde Escudo (Cape Verde)");
		currencies.put("CYP", "Cyprus Pound (Cyprus)");
		currencies.put("CZK", "Czech Koruna (Czech Republic)");
		currencies.put("DJF", "Djibouti Franc (Djibouti)");
		currencies.put("DKK", "Danish Krone (Denmark, Faroe Islands)");
		currencies.put("DOP", "Dominican Peso (Dominican Republic)");
		currencies.put("DZD", "Algerian Dinar (Algeria)");
		currencies.put("EEK", "Kroon (Estonia)");
		currencies.put("EGP", "Egyptian Pound (Egypt)");
		currencies.put("ERN", "Nakfa (Eritrea)");
		currencies.put("ETB", "Ethiopian Birr (Ethiopia)");
		currencies
				.put(
						"EUR",
						"Euro (Andorra, Austria, Belgium, Finland, France, French Guiana, French Southern Territories, Germany	], Greece, Guadeloupe, Ireland, Italy, Luxembourg, Martinique, Mayotte, Monaco, Montenegro	], Netherlands, Portugal, Reunion, Saint Pierre and Miquelon, San Marino, Spain, Vatican ty	]) ");
		currencies.put("FJD", "Fiji Dollar (Fiji)");
		currencies.put("FKP", "Falkland Islands Pound (Falkland Islands)");
		currencies.put("GBP", "Pound Sterling (United Kingdom)");
		currencies.put("GEL", "Lari (Georgia (country)|Georgia)");
		currencies.put("GHC", "Cedi (Ghana)");
		currencies.put("GIP", "Gibraltar Pound (Gibraltar)");
		currencies.put("GMD", "Dalasi (Gambia)");
		currencies.put("GNF", "Guinea Franc (Guinea)");
		currencies.put("GTQ", "Guatemalan quetzal|Quetzal (Guatemala");
		currencies.put("GYD", "Guyana Dollar (Guyana)");
		currencies.put("HKD", "Hong Kong Dollar (Hong Kong)");
		currencies.put("HNL", "Lempira (Honduras)");
		currencies.put("HRK", "Croatian Kuna (Croatia)");
		currencies.put("HTG", "Haiti Gourde (Haiti)");
		currencies.put("HUF", "Forint (Hungary)");
		currencies.put("IDR", "Rupiah (Indonesia)");
		currencies.put("ILS", "New Israeli Shekel (Israel)");
		currencies.put("INR", "Indian Rupee (Bhutan, India)");
		currencies.put("IQD", "Iraqi Dinar (Iraq)");
		currencies.put("IRR", "Iranian Rial (Iran)");
		currencies.put("ISK", "Iceland Krona (Iceland)");
		currencies.put("JMD", "Jamaican Dollar (Jamaica)");
		currencies.put("JOD", "Jordanian Dinar (Jordan)");
		currencies.put("JPY", "Yen (Japan)");
		currencies.put("KES", "Kenyan Shilling (Kenya)");
		currencies.put("KGS", "Som (Kyrgyzstan)");
		currencies.put("KHR", "Riel (Cambodia)");
		currencies.put("KMF", "Comoro Franc (Comoros)");
		currencies.put("KPW", "North Korean Won (North Korea)");
		currencies.put("KRW", "Won (South Korea)");
		currencies.put("KWD", "Kuwaiti Dinar (Kuwait)");
		currencies.put("KYD", "Cayman Islands Dollar (Cayman Islands)");
		currencies.put("KZT", "Tenge (Kazakhstan)");
		currencies.put("LAK", "Kip (Laos)");
		currencies.put("LBP", "Lebanese Pound (Lebanon)");
		currencies.put("LKR", "Sri Lanka Rupee (Sri Lanka)");
		currencies.put("LRD", "Liberian Dollar (Liberia)");
		currencies.put("LSL", "Loti (Lesotho)");
		currencies.put("LTL", "Lithuanian Litas (Lithuania)");
		currencies.put("LVL", "Latvian Lats (Latvia)");
		currencies.put("LYD", "Libyan Dinar (Libya)");
		currencies.put("MAD", "Moroccan Dirham (Morocco, Western Sahara)");
		currencies.put("MDL", "Moldovan Leu (Moldova)");
		currencies.put("MGA", "Malagasy Ariary (Madagascar)");
		currencies.put("MKD", "Denar (Macedonia)");
		currencies.put("MMK", "Kyat (Myanmar)");
		currencies.put("MNT", "Tugrik (Mongolia)");
		currencies.put("MOP", "Pataca (Macau)");
		currencies.put("MRO", "Ouguiya (Mauritania)");
		currencies.put("MTL", "Maltese Lira (Malta)");
		currencies.put("MUR", "Mauritius Rupee (Mauritius)");
		currencies.put("MVR", "Rufiyaa (Maldives)");
		currencies.put("MWK", "Kwacha (Malawi)");
		currencies.put("MXN", "Mexican Peso (Mexico)");
		currencies.put("MXV",
				"Mexican Unidad de Inversion (UDI) (Funds code) (Mexico)");
		currencies.put("MYR", "Malaysian Ringgit (Malaysia)");
		currencies.put("MZM",
				"Metical (Mozambique) (assuming merical is a typo)");
		currencies.put("NAD", "Namibian Dollar (Namibia)");
		currencies.put("NGN", "Naira (Nigeria)");
		currencies.put("NIO", "Cordoba Oro (Nicaragua) ");
		currencies.put("NOK", "Norwegian Krone (Norway)");
		currencies.put("NPR", "Nepalese Rupee (Nepal)");
		currencies
				.put("NZD",
						"New Zealand Dollar (Cook Islands, New Zealand, Niue, Pitcairn, Tokelau)");
		currencies.put("OMR", "Rial Omani (Oman)");
		currencies.put("PAB", "Balboa (Panama)");
		currencies.put("PEN", "Nuevo Sol (Peru)");
		currencies.put("PGK", "Kina (Papua New Guinea)");
		currencies.put("PHP", "Philippine Peso (Philippines)");
		currencies.put("PKR", "Pakistan Rupee (Pakistan)");
		currencies.put("PLN", "Zloty (Poland)");
		currencies.put("PYG", "Guarani (Paraguay)");
		currencies.put("QAR", "Qatari Rial (Qatar)");
		currencies.put("RON", "New Leu (Romania)");
		currencies.put("RUB", "Russian Ruble (Russia)");
		currencies.put("RWF", "Rwanda Franc (Rwanda)");
		currencies.put("SAR", "Saudi Riyal (Saudi Arabia)");
		currencies.put("SBD", "Solomon Islands Dollar (Solomon Islands)");
		currencies.put("SCR", "Seychelles Rupee (Seychelles)");
		currencies.put("SDD", "Sudanese Dinar (Sudan)");
		currencies.put("SEK", "Swedish Krona (Sweden)");
		currencies.put("SGD", "Singapore Dollar (Singapore)");
		currencies.put("SHP", "Saint Helena Pound (Saint Helena)");
		currencies.put("SIT", "Tolar (Slovenia)");
		currencies.put("SKK", "Slovak Koruna (Slovakia)");
		currencies.put("SLL", "Leone (Sierra Leone)");
		currencies.put("SOS", "Somali Shilling (Somalia)");
		currencies.put("SRD", "Surinam Dollar (Suriname)");
		currencies.put("STD", "Dobra (São Tomé and Príncipe)");
		currencies.put("SYP", "Syrian Pound (Syria)");
		currencies.put("SZL", "Lilangeni (Swaziland)");
		currencies.put("THB", "Baht (Thailand)");
		currencies.put("TJS", "Somoni (Tajikistan)");
		currencies.put("TMM", "Manat (Turkmenistan)");
		currencies.put("TND", "Tunisian Dinar (Tunisia)");
		currencies.put("TOP", "Pa'anga (Tonga)");
		currencies.put("TRY", "New Turkish Lira (Turkey)");
		currencies.put("TTD",
				"Trinidad and Tobago Dollar (Trinidad and Tobago)");
		currencies.put("TWD", "New Taiwan Dollar (Taiwan)");
		currencies.put("TZS", "Tanzanian Shilling (Tanzania)");
		currencies.put("UAH", "Hryvnia (Ukraine)");
		currencies.put("UGX", "Uganda Shilling (Uganda)");
		currencies
				.put(
						"USD",
						"US Dollar (American Samoa, British Indian Ocean Territory, Ecuador, El Salvador, Guam, Haiti, Marshall	Islands, Micronesia, Northern Mariana Islands, Palau, Panama, Palau, East Timor, Turks d	Caicos Islands, United States, Virgin Islands, Western Samoa)");
		currencies.put("UYU", "Peso Uruguayo (Uruguay)");
		currencies.put("UZS", "Uzbekistan Som (Uzbekistan)");
		currencies.put("VEB", "Bolivar (Venezuela)");
		currencies.put("VND", "Dong (Vietnam)");
		currencies.put("VUV", "Vatu (Vanuatu)");
		currencies.put("WST", "Tala (Samoa)");
		currencies
				.put(
						"XAF",
						"CFA Franc BEAC (Cameroon, Central African Republic, Congo, Chad, Equatorial Guinea, Gabon)");
		currencies.put("XAG", "Silver (one Troy ounce)");
		currencies.put("XAU", "Gold (one Troy ounce)");
		currencies.put("XBA",
				"European Composite Unit (EURCO) (Bonds market unit)");
		currencies.put("XBB",
				"European Monetary Unit (E.M.U.-6) (Bonds market unit)");
		currencies.put("XBC",
				"European Unit of Account 9 (E.U.A.-9) (Bonds market unit)");
		currencies.put("XBD",
				"European Unit of Account 17 (E.U.A.-17) (Bonds market unit)");
		currencies
				.put(
						"XCD",
						"East Caribbean Dollar (Anguilla, Antigua and Barbuda, Dominica, Grenada, Montserrat, Saint Kitts d	Nevis, Saint Lucia, Saint Vincent and the Grenadines)");
		currencies.put("XDR",
				"Special Drawing Rights (International Monetary Fund|IMF)");
		currencies.put("XFO", "Gold-franc (Special settlement currency)");
		currencies.put("XFU", "UIC franc (Special settlement currency)");
		currencies
				.put(
						"XOF",
						"CFA Franc BCEAO (Benin, Burkina Faso, Côte d'Ivoire, Guinea-Bissau, Mali, Niger, Senegal, Togo	])");
		currencies.put("XPD", "Palladium (one Troy ounce)");
		currencies
				.put("XPF",
						"CFP franc (French Polynesia, New Caledonia, Wallis and Futuna)");
		currencies.put("XPT", "Platinum (one Troy ounce)");
		currencies.put("YER", "Yemeni Rial (Yemen)");
		currencies.put("ZAR", "Rand (Lesotho, Namibia, South Africa)");
		currencies.put("ZMK", "Kwacha (Zambia)");
		currencies.put("ZWD", "Zimbabwe Dollar (Zimbabwe)");

		Iterator<String> it = currencies.keySet().iterator();

		while (it.hasNext())
		{
			String ccy = it.next();
			String val = currencies.get(ccy);
			try
			{
				Iso4217Currency.valueOf(ccy);
			} catch (Exception e)
			{
				fail("Currency not found " + ccy + ": " + val);
			}
		}
	}

	public void testDEM() {
		Iso4217Currency dem = Iso4217Currency.DEM;

		assertEquals("Mark (Deutsche)",dem.getFullName());

		Iso3166Country country = dem.getCountries()[0];

		assertEquals( Iso3166Country.DE , country);
		assertEquals(Iso4217Currency.EUR, dem.getReplacement());
		assertTrue(dem.hasBeenReplaced());
		assertEquals("Formerly DDM in east germany",dem.getNotes());

	}
}
