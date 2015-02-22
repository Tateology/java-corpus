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

package org.jfin.common;

public enum Iso4217Currency
{
	AFN("Afghani", new Iso3166Country[] {Iso3166Country.AF}),
	ARA("Austral", new Iso3166Country[] {Iso3166Country.AR}),
	THB("Baht", new Iso3166Country[] {Iso3166Country.TH}),
	PAB("Balboa", new Iso3166Country[] {Iso3166Country.PA}),
	ETB("Birr", new Iso3166Country[] {Iso3166Country.ER, Iso3166Country.ET}),
	VEB("Bolivar", new Iso3166Country[] {Iso3166Country.VE}),
	BOB("Boliviano", new Iso3166Country[] {Iso3166Country.BO}),
	GHC("Cedi", new Iso3166Country[] {Iso3166Country.GH}),
	CRC("Colón (Costa Rican)", new Iso3166Country[] {Iso3166Country.CR}),
	SVC("Colón (El Salvadorian)", new Iso3166Country[] {Iso3166Country.SV}),
	BAM("Convertible Mark", new Iso3166Country[] {Iso3166Country.BA}),
	NIO("Córdoba", new Iso3166Country[] {Iso3166Country.NI}),
	BRL("Brazilian Real", new Iso3166Country[] {Iso3166Country.BR}),
	BRR("Cruzeiro Real", new Iso3166Country[] {Iso3166Country.BR}, Iso4217Currency.BRL),
	GMD("Dalasi", new Iso3166Country[] {Iso3166Country.GM}),
	DZD("Dinar (Algerian)", new Iso3166Country[] {Iso3166Country.DZ}),
	BHD("Dinar (Bahraini)", new Iso3166Country[] {Iso3166Country.BH}),
	HRD("Dinar (Croatian)", new Iso3166Country[] {Iso3166Country.HR}),
	IQD("Dinar (Iraqi)", new Iso3166Country[] {Iso3166Country.IQ}),
	JOD("Dinar (Jordanian)", new Iso3166Country[] {Iso3166Country.JO}),
	KWD("Dinar (Kuwaiti)", new Iso3166Country[] {Iso3166Country.KW}),
	LYD("Dinar (Libyan)", new Iso3166Country[] {Iso3166Country.LY}),
	MKD("Dinar (Macedonian)", new Iso3166Country[] {Iso3166Country.MK}),
	CSD("Dinar (Serbian)", new Iso3166Country[] {Iso3166Country.CS}),
	YER("Riyal (Yemeni)", new Iso3166Country[] {Iso3166Country.YE}),
	YDD("Dinar (South Yemeni)", Iso4217Currency.YER),
	SDD("Dinar (Sudanese)", new Iso3166Country[] {Iso3166Country.SD}),
	TND("Dinar (Tunisian)", new Iso3166Country[] {Iso3166Country.TN}),
	MAD("Dirham (Moroccan)", new Iso3166Country[] {Iso3166Country.MA, Iso3166Country.EH}),
	AED("Dirham (UAE)", new Iso3166Country[] {Iso3166Country.AE}),
	STD("Dobra", new Iso3166Country[] {Iso3166Country.ST}),
	AUD("Dollar (Australian)", new Iso3166Country[] {Iso3166Country.AU, Iso3166Country.CX, Iso3166Country.CC, Iso3166Country.HM, Iso3166Country.KI, Iso3166Country.NR, Iso3166Country.NF, Iso3166Country.TV}),
	BSD("Dollar (Bahamian)", new Iso3166Country[] {Iso3166Country.BS}),
	BBD("Dollar (Barbados)", new Iso3166Country[] {Iso3166Country.BB}),
	BZD("Dollar (Belize)", new Iso3166Country[] {Iso3166Country.BZ}),
	BMD("Dollar (Bermudian)", new Iso3166Country[] {Iso3166Country.BM}),
	BND("Dollar (Brunei)", new Iso3166Country[] {Iso3166Country.BN}),
	CAD("Dollar (Canadian)", new Iso3166Country[] {Iso3166Country.CA}),
	KYD("Dollar (Cayman Islands)", new Iso3166Country[] {Iso3166Country.KY}),
	XCD("Dollar (East Caribbean)", new Iso3166Country[] {Iso3166Country.AI, Iso3166Country.AG, Iso3166Country.VG, Iso3166Country.DM, Iso3166Country.GD, Iso3166Country.MS, Iso3166Country.KN, Iso3166Country.LC, Iso3166Country.VC}),
	FJD("Dollar (Fiji)", new Iso3166Country[] {Iso3166Country.FJ}),
	GYD("Dollar (Guyana)", new Iso3166Country[] {Iso3166Country.GY}),
	HKD("Dollar (Hong Kong)", new Iso3166Country[] {Iso3166Country.HK}),
	JMD("Dollar (Jamaican)", new Iso3166Country[] {Iso3166Country.JM}),
	LRD("Dollar (Liberian)", new Iso3166Country[] {Iso3166Country.LR}),
	MYR("Dollar (Malaysian)", new Iso3166Country[] {Iso3166Country.MY},"Synonym Ringgit"), // Synonym Ringgit
	NAD("Dollar (Namibian)", new Iso3166Country[] {Iso3166Country.NA}),
	NZD("Dollar (New Zealand)", new Iso3166Country[] {Iso3166Country.CK, Iso3166Country.NZ, Iso3166Country.NU, Iso3166Country.PN, Iso3166Country.TK}),
	SGD("Dollar (Singapore)", new Iso3166Country[] {Iso3166Country.SG}),
	SBD("Dollar (Solomon Islands)", new Iso3166Country[] {Iso3166Country.SB}),
	TWD("Dollar (Taiwan, New)", new Iso3166Country[] {Iso3166Country.TW}),
	TTD("Dollar (Trinidad and Tobago)", new Iso3166Country[] {Iso3166Country.TT}),
	ZWD("Dollar (Zimbabwe)", new Iso3166Country[] {Iso3166Country.ZW}),
	VND("Dông", new Iso3166Country[] {Iso3166Country.VN}),
	GRD("Drachma", new Iso3166Country[] {Iso3166Country.GR}),
	AMD("Dram", new Iso3166Country[] {Iso3166Country.AM}),
	EUR("Euro", new Iso3166Country[] {Iso3166Country.AT, Iso3166Country.BE, Iso3166Country.FI, Iso3166Country.FR, Iso3166Country.DE, Iso3166Country.GR, Iso3166Country.IE, Iso3166Country.IT, Iso3166Country.LU, Iso3166Country.NL, Iso3166Country.PT, Iso3166Country.ES, Iso3166Country.MC, Iso3166Country.VA, Iso3166Country.CS, Iso3166Country.SM, Iso3166Country.SI}), //  Also Serbian province of Kosovo and various French dependencies.
	XEU("ECU", Iso4217Currency.EUR),
	GQE("Ekwele", new Iso3166Country[] {Iso3166Country.GQ}),
	CVE("Escudo (Caboverdiano)", new Iso3166Country[] {Iso3166Country.CV}),
	PTE("Escudo (Portuguese)", new Iso3166Country[] {Iso3166Country.PT}, Iso4217Currency.EUR),
	TPE("Escudo (Timorian)", "TODO: Confirm country"), //TODO: Confirm
	FLORIN("Florin", Iso4217Currency.EUR,"TODO: Find old ISO 4217 Code for Florin"), // TODO: Find old ISO 4217 Code for Florin
	HUF("Forint", new Iso3166Country[] {Iso3166Country.HU}),
	BEF("Franc (Belgian)", new Iso3166Country[] {Iso3166Country.BE}, Iso4217Currency.EUR, "Synonym Frank"), // Synonym Frank
	BEC("Franc (Belgian, Convertible)", new Iso3166Country[] {Iso3166Country.BE}, Iso4217Currency.EUR),
	BEL("Franc (Belgian, Financial)", new Iso3166Country[] {Iso3166Country.BE}, Iso4217Currency.EUR),
	BIF("Franc (Burundi)", new Iso3166Country[] {Iso3166Country.BI}),
	KMF("Franc (Comorian)", new Iso3166Country[] {Iso3166Country.KM}),
	XAF("Franc (de la Communauté financière africaine [Central African Franc])", new Iso3166Country[] {Iso3166Country.BJ, Iso3166Country.BF, Iso3166Country.CM, Iso3166Country.CF, Iso3166Country.TD, Iso3166Country.CG, Iso3166Country.CI, Iso3166Country.GQ, Iso3166Country.GA, Iso3166Country.GW, Iso3166Country.ML, Iso3166Country.NE, Iso3166Country.SN, Iso3166Country.TG}),
	XPF("Franc (des Comptoirs français du Pacifique [Pacific Franc])", new Iso3166Country[] {Iso3166Country.PF, Iso3166Country.NC, Iso3166Country.WF}),
	DJF("Franc (Djibouti)", new Iso3166Country[] {Iso3166Country.DJ}),
	FRF("Franc (French)", new Iso3166Country[] {Iso3166Country.AD, Iso3166Country.FR, Iso3166Country.GF, Iso3166Country.TF, Iso3166Country.GP, Iso3166Country.MQ, Iso3166Country.YT, Iso3166Country.MC, Iso3166Country.RE, Iso3166Country.PM},Iso4217Currency.EUR), // TODO: Check  Iso3166Country.FX
	GNS("Syli (Guinea)", new Iso3166Country[] {Iso3166Country.GN}),
	GNF("Franc (Guinea)", new Iso3166Country[] {Iso3166Country.GN}, "// TODO: Check which is valid, GNS or GNF, or if codes are true synonyms"), // TODO: Check which is valid, GNS or GNF, or if codes are true synonyms
	LUF("Franc (Luxembourg)", new Iso3166Country[] {Iso3166Country.LU}, Iso4217Currency.EUR),
	MGA("Ariary (Malagasy)", new Iso3166Country[] {Iso3166Country.MG}),
	MGF("Franc (Malagasy)", new Iso3166Country[] {Iso3166Country.MG}, Iso4217Currency.MGA),
	MLF("Franc (Malian)", new Iso3166Country[] {Iso3166Country.ML}),
	RWF("Franc (Rwanda)", new Iso3166Country[] {Iso3166Country.RW}),
	CHF("Franc (Swiss)", new Iso3166Country[] {Iso3166Country.LI, Iso3166Country.CH}),
	XOF("Franc (West African)", new Iso3166Country[] {Iso3166Country.NE, Iso3166Country.SN}),
	HTG("Gourde", new Iso3166Country[] {Iso3166Country.HT}),
	PYG("Guarani", new Iso3166Country[] {Iso3166Country.PY}),
	AWG("Guilder (Aruban)", new Iso3166Country[] {Iso3166Country.AW}),
	NLG("Guilder (Dutch)", new Iso3166Country[] {Iso3166Country.NL}, Iso4217Currency.EUR, "Synonym Gulden"), // Synonym Gulden
	ANG("Guilder (Netherlands Antilles)", new Iso3166Country[] {Iso3166Country.AN}),
	SRD("Dollar (Surinam)", new Iso3166Country[] {Iso3166Country.SR}),
	SRG("Guilder (Surinam)", new Iso3166Country[] {Iso3166Country.SR}, Iso4217Currency.SRD),
	UAH("Hryvna", new Iso3166Country[] {Iso3166Country.UA}),
	PEI("Inti", new Iso3166Country[] {Iso3166Country.PE}),
	UAK("Karbovanet", new Iso3166Country[] {Iso3166Country.UA}),
	PGK("Kina", new Iso3166Country[] {Iso3166Country.PG}),
	LAK("Kip", new Iso3166Country[] {Iso3166Country.LA}),
	CZK("Koruna (Czech)", new Iso3166Country[] {Iso3166Country.CZ}),
	CSK("Koruna (Czech)", new Iso3166Country[] {Iso3166Country.CZ}, Iso4217Currency.CZK,"Misuse"),
	SKK("Koruna (Slovak)", new Iso3166Country[] {Iso3166Country.SK}),
	ISK("Króna (Icelandic)", new Iso3166Country[] {Iso3166Country.IS}),
	SEK("Krona (Swedish)", new Iso3166Country[] {Iso3166Country.SE}),
	DKK("Krone (Danish)", new Iso3166Country[] {Iso3166Country.DK, Iso3166Country.FO, Iso3166Country.GL}),
	NOK("Krone (Norwegian)", new Iso3166Country[] {Iso3166Country.AQ, Iso3166Country.BV, Iso3166Country.NO, Iso3166Country.SJ}),
	EEK("Kroon", new Iso3166Country[] {Iso3166Country.EE}),
	HRK("Kuna", new Iso3166Country[] {Iso3166Country.HR}),
	MWK("Kwacha (Malawian)", new Iso3166Country[] {Iso3166Country.MW}),
	ZMK("Kwacha (Zambian)", new Iso3166Country[] {Iso3166Country.ZM}),
	MMK("Kyat", new Iso3166Country[] {Iso3166Country.MM}, "Formerly BUK"), // Formerly BUK
	GEL("Lari", new Iso3166Country[] {Iso3166Country.GE}),
	LVL("Lats", new Iso3166Country[] {Iso3166Country.LA}),
	ALL("Lek", new Iso3166Country[] {Iso3166Country.AL}),
	HNL("Lempira", new Iso3166Country[] {Iso3166Country.HN}),
	SLL("Leone", new Iso3166Country[] {Iso3166Country.SL}),
	MDL("Leu (Moldavian)", new Iso3166Country[] {Iso3166Country.MD}),
	RON("Leu (New Romanian)", new Iso3166Country[] {Iso3166Country.RO}),
	ROL("Leu (Romanian)", new Iso3166Country[] {Iso3166Country.RO}, Iso4217Currency.RON),
	BGN("Lev", new Iso3166Country[] {Iso3166Country.BG}),
	BGL("Lev", new Iso3166Country[] {Iso3166Country.BG}, Iso4217Currency.BGN),
	SZL("Lilangeni", new Iso3166Country[] {Iso3166Country.SZ}),
	ITL("Lira (Italian)", new Iso3166Country[] {Iso3166Country.VA, Iso3166Country.IT, Iso3166Country.SM}, Iso4217Currency.EUR),
	MTL("Lira (Maltese)", new Iso3166Country[] {Iso3166Country.MT}),
	TRY("Lira (New Turkish)", new Iso3166Country[] {Iso3166Country.TR}),
	TRL("Lira (Turkish)", new Iso3166Country[] {Iso3166Country.TR}, Iso4217Currency.TRY),
	LTL("Litas", new Iso3166Country[] {Iso3166Country.LT}),
	LSL("Loti", new Iso3166Country[] {Iso3166Country.LS}),
	LSM("Maloti", new Iso3166Country[] {Iso3166Country.LS}),
	AZN("Manat (Azerbaijani)", new Iso3166Country[] {Iso3166Country.AZ}),
	AZM("Manat (Azerbaijani)", new Iso3166Country[] {Iso3166Country.AZ}, "TODO: Check is true synonym"), // TODO: Check is true synonym
	TMM("Manat (Turkmenistani)", new Iso3166Country[] {Iso3166Country.TM}),
	DEM("Mark (Deutsche)", new Iso3166Country[] {Iso3166Country.DE}, Iso4217Currency.EUR, "Formerly DDM in east germany"), // Formerly DDM in east germany
	FIM("Markka", new Iso3166Country[] {Iso3166Country.FI}, Iso4217Currency.EUR),
	MZM("Metical", new Iso3166Country[] {Iso3166Country.MZ}),
	NGN("Naira", new Iso3166Country[] {Iso3166Country.NG}),
	ERN("Nakfa", new Iso3166Country[] {Iso3166Country.ER}),
	AOA("Kwanza", new Iso3166Country[] {Iso3166Country.AO}, "Replaced AOK"), // Replaced AOK
	MXN("New Peso (Mexican)", new Iso3166Country[] {Iso3166Country.MX}, "Replaced MXP"), // Replaced MXP
	MXV("Mexican Unidad de Inversion", new Iso3166Country[] {Iso3166Country.MX}, "UDI Funds Code"), // UDI Funds Code
	UYU("New Peso (Uruguayan)", new Iso3166Country[] {Iso3166Country.UY}, "Replaced UYP"), // Replaced UYP
	PEN("New Sol", new Iso3166Country[] {Iso3166Country.PE}, "Replaced PES"), // Replaced PES
	PLN("New Zloty", new Iso3166Country[] {Iso3166Country.PL}, "Replaced PLZ"), // Replaced PLZ
	BTN("Ngultrum", new Iso3166Country[] {Iso3166Country.BT}),
	ARS("Nuevo Peso", new Iso3166Country[] {Iso3166Country.AR}, "Replaced ARP"), // Replaced ARP
	MRO("Ouguiya", new Iso3166Country[] {Iso3166Country.MR, Iso3166Country.EH}),
	TOP("Pa'anga", new Iso3166Country[] {Iso3166Country.TO}),
	MOP("Pataca", new Iso3166Country[] {Iso3166Country.MO}),
	ADP("Peseta (Andorran)", new Iso3166Country[] {Iso3166Country.AD}, Iso4217Currency.EUR),
	ESP("Peseta (Spanish)", new Iso3166Country[] {Iso3166Country.AD, Iso3166Country.ES, Iso3166Country.EH}, Iso4217Currency.EUR),
	// TODO: Check ISO 4217 Code for Argentinian Peso (replaced by Nuevo Peso)
	BOP("Peso (Bolivian)", new Iso3166Country[] {Iso3166Country.BO}),
	BOV("Mvdol (Bolivian)", new Iso3166Country[] {Iso3166Country.BO}, "Funds code"), // Funds code
	CLP("Peso (Chilean)", new Iso3166Country[] {Iso3166Country.CL}),
	COP("Peso (Colombian)", new Iso3166Country[] {Iso3166Country.CO}),
	COU("Unidad de Valor Real", new Iso3166Country[] {Iso3166Country.CO}, "TODO: Verify the correct/legacy Columbian currency"), // TODO: Verify the correct/legacy Columbian currency
	CUP("Peso (Cuban)", new Iso3166Country[] {Iso3166Country.CU}),
	DOP("Peso (Dominican Republic)", new Iso3166Country[] {Iso3166Country.DO}),
	GWP("Peso (Guinea-Bissau)", new Iso3166Country[] {Iso3166Country.GW}),
	// TODO: Check ISO 4217 Code for Mexican Peso (replaced by Mexican New Peso)
	PHP("Peso (Philippines)", new Iso3166Country[] {Iso3166Country.PH}),
	// TODO: Check ISO 4217 Code for Uruguayan Peso (Replaced by Uruguayan New Peso)
	CYP("Pound (Cypriot)", new Iso3166Country[] {Iso3166Country.CY}),
	EGP("Pound (Egytian)", new Iso3166Country[] {Iso3166Country.EG}),
	FKP("Pound (Falkland)", new Iso3166Country[] {Iso3166Country.FK}),
	GIP("Pound (Gibraltar)", new Iso3166Country[] {Iso3166Country.GI}),
	LBP("Pound (Lebanese)", new Iso3166Country[] {Iso3166Country.LB}),
	// TODO: Check ISO 4217 Code for Maltese Pound (replaced by Maltese Lira)
	SHP("Pound (St Helena)", new Iso3166Country[] {Iso3166Country.SH}),
	GBP("Pound (Sterling)", new Iso3166Country[] {Iso3166Country.IO, Iso3166Country.VG, Iso3166Country.GS, Iso3166Country.GB}),
	SDP("Pound (Sudanese)", new Iso3166Country[] {Iso3166Country.SD}),
	SYP("Pound (Syrian)", new Iso3166Country[] {Iso3166Country.SY}),
	BWP("Pula", new Iso3166Country[] {Iso3166Country.BW}),
	IEP("Punt", new Iso3166Country[] {Iso3166Country.IE}, Iso4217Currency.EUR),
	GTQ("Quetzal", new Iso3166Country[] {Iso3166Country.GT}),
	ZAR("Rand", new Iso3166Country[] {Iso3166Country.LS, Iso3166Country.NA, Iso3166Country.ZA}),
	ZAL("Rand (financial)", new Iso3166Country[] {Iso3166Country.ZA}),
	IRR("Rial (Iranian)", new Iso3166Country[] {Iso3166Country.IR}),
	OMR("Rial (Omani)", new Iso3166Country[] {Iso3166Country.OM}),
	KHR("Riel", new Iso3166Country[] {Iso3166Country.KH}),
	QAR("Riyal (Qatari)", new Iso3166Country[] {Iso3166Country.QA}),
	SAR("Riyal (Saudi)", new Iso3166Country[] {Iso3166Country.SA}),
	BYR("Rouble (Belarussian)", new Iso3166Country[] {Iso3166Country.BY}),
	RUB("Rouble (Russian Federation)", new Iso3166Country[] {Iso3166Country.RU}, "Formerly RUR"), // Formerly RUR
	TJR("Rouble (Tajik)", new Iso3166Country[] {Iso3166Country.TJ}),
	TJS("Somoni", new Iso3166Country[] {Iso3166Country.TJ}, "TODO: Check which is correct, TJR/TJS"), // TODO: Check which is correct, TJR/TJS
	// SUR("Rouble (USSR)", new Iso3166Country[] {Iso3166Country.SU}), TODO: Find out how this relates to RUR and whether SU is a real ISO 3166 Country
	MVR("Rufiyaa", new Iso3166Country[] {Iso3166Country.MV}),
	INR("Rupee (Indian)", new Iso3166Country[] {Iso3166Country.BT, Iso3166Country.IN}),
	MUR("Rupee (Mauritius)", new Iso3166Country[] {Iso3166Country.MU}),
	NPR("Rupee (Nepalese)", new Iso3166Country[] {Iso3166Country.NP}),
	PKR("Rupee (Pakistani)", new Iso3166Country[] {Iso3166Country.PK}),
	SCR("Rupee (Seychelles)", new Iso3166Country[] {Iso3166Country.IO, Iso3166Country.SC}),
	LKR("Rupee (Sri Lankan)", new Iso3166Country[] {Iso3166Country.LK}),
	IDR("Rupiah", new Iso3166Country[] {Iso3166Country.ID}),
	ATS("Schilling", new Iso3166Country[] {Iso3166Country.AT}, Iso4217Currency.EUR),
	ILS("Shekel", new Iso3166Country[] {Iso3166Country.IL}),
	KES("Shilling (Kenyan)", new Iso3166Country[] {Iso3166Country.KE}),
	SOS("Shilling (Somali)", new Iso3166Country[] {Iso3166Country.SO}),
	TZS("Shilling (Tanzanian)", new Iso3166Country[] {Iso3166Country.TZ}),
	UGX("Shilling (Ugandan)", new Iso3166Country[] {Iso3166Country.UG}),
	UGS("Shilling (Ugandan)", new Iso3166Country[] {Iso3166Country.UG}, Iso4217Currency.UGX),
	// TODO: Check ISO 4217 Code for Sol (replaced by New Sol)
	KGS("Som (Kyrgyzstani)", new Iso3166Country[] {Iso3166Country.KG}),
	UZS("Som (Uzbekistani)", new Iso3166Country[] {Iso3166Country.UZ}),
	USD("US Dollar", new Iso3166Country[] {Iso3166Country.AS, Iso3166Country.VG, Iso3166Country.EC, Iso3166Country.FM, Iso3166Country.GU, Iso3166Country.MH, Iso3166Country.PW, Iso3166Country.PA, Iso3166Country.PR, Iso3166Country.TC, Iso3166Country.US, Iso3166Country.UM, Iso3166Country.VI}),
	ECS("Sucre", new Iso3166Country[] {Iso3166Country.EC}, Iso4217Currency.USD),
    BDT("Taka", new Iso3166Country[] {Iso3166Country.BD}),
	WST("Tala", new Iso3166Country[] {Iso3166Country.WS}),
	KZT("Tenge", new Iso3166Country[] {Iso3166Country.KZ}),
	SIT("Tolar", new Iso3166Country[] {Iso3166Country.SI},Iso4217Currency.EUR), // Replaced by EUR 2006/10/30
	MNT("Tugrik", new Iso3166Country[] {Iso3166Country.MN}),
	CLF("Unidades de Fomento", new Iso3166Country[] {Iso3166Country.CL}),
	VUV("Vatu", new Iso3166Country[] {Iso3166Country.VU}),
	KPW("Won (North Korean)", new Iso3166Country[] {Iso3166Country.KP}),
	KRW("Won (South Korean)", new Iso3166Country[] {Iso3166Country.KR}),
	JPY("Yen", new Iso3166Country[] {Iso3166Country.JP}),
	CNY("Yuan Renminbi", new Iso3166Country[] {Iso3166Country.CN}),
	CDF("Franc (Congolaise)", new Iso3166Country[] {Iso3166Country.CD}),
	CDZ("Zaïre (New)", new Iso3166Country[] {Iso3166Country.CD}, Iso4217Currency.CDF, "Formerly ZRZ"), // Formerly ZRZ
	// TODO: Find ISO 4217 Code for Zloty (replaced by New Zloty)


	// Precious metals
	XPD("Palladium (one Troy ounce)"),
	XPT("Platinum (one Troy ounce)"),
	XAG("Silver (one Troy ounce)"),
	XAU("Gold (one Troy ounce)"),

	// Special EUA, EURCA and EMU units
	XBA("European Composite Unit (EURCO) (Bonds market unit)"),
	XBB("European Monetary Unit (E.M.U.-6) (Bonds market unit)"),
	XBC("European Unit of Account 9 (E.U.A.-9) (Bonds market unit)"),
	XBD("European Unit of Account 17 (E.U.A.-17) (Bonds market unit)"),
	XDR("Special Drawing Rights (International Monetary Fund)"),

	// Special settlement currencies
	XFO("Gold-franc (Special settlement currency)"),
	XFU("UIC franc (Special settlement currency)");

	private String fullName;
	private Iso4217Currency replacement;
	private Iso3166Country[] countries;
	private String notes;

	/**
	 * @param fullName
	 */
	private Iso4217Currency(String fullName)
	{
		this.fullName = fullName;
	}

	/**
	 * @param fullName
	 * @param notes
	 */
	private Iso4217Currency(String fullName, String notes)
	{
		this.fullName = fullName;
		this.notes = notes;
	}

	/**
	 * @param fullName
	 * @param countries
	 */
	private Iso4217Currency(String fullName, Iso3166Country[] countries)
	{
		this.fullName = fullName;
		this.countries = countries;
	}

	/**
	 * @param fullName
	 * @param countries
	 * @param notes
	 */
	private Iso4217Currency(String fullName, Iso3166Country[] countries, String notes)
	{
		this.fullName = fullName;
		this.countries = countries;
		this.notes = notes;
	}

	/**
	 * @param fullName
	 * @param countries
	 */
	private Iso4217Currency(String fullName, Iso3166Country[] countries, Iso4217Currency replacement)
	{
		this.fullName = fullName;
		this.countries = countries;
		this.replacement = replacement;
	}

	/**
	 * @param fullName
	 * @param countries
	 * @param notes
	 */
	private Iso4217Currency(String fullName, Iso3166Country[] countries, Iso4217Currency replacement, String notes)
	{
		this.fullName = fullName;
		this.countries = countries;
		this.replacement = replacement;
		this.notes = notes;
	}

	/**
	 * @param fullName
	 * @param replacement
	 */
	private Iso4217Currency(String fullName, Iso4217Currency replacement)
	{
		this.fullName = fullName;
		this.replacement = replacement;
	}

	/**
	 * @param fullName
	 * @param replacement
	 * @param notes
	 */
	private Iso4217Currency(String fullName, Iso4217Currency replacement, String notes)
	{
		this.fullName = fullName;
		this.replacement = replacement;
		this.notes = notes;
	}

	/**
	 * @return Returns the fullName.
	 */
	public String getFullName()
	{
		return fullName;
	}

	/**
	 * @return Returns the countries.
	 */
	public Iso3166Country[] getCountries()
	{
		return countries;
	}

	/**
	 * @return Returns the replacement.
	 */
	public Iso4217Currency getReplacement()
	{
		return replacement;
	}

	/**
	 * Evaluate whether the currency has been replaced
	 * @return true if the currency has been replaced
	 */
	public boolean hasBeenReplaced() {
		return getReplacement()!=null;
	}

	/**
	 * @return Returns the notes.
	 */
	public String getNotes()
	{
		return notes;
	}




}
