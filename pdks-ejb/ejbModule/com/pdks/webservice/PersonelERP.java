package com.pdks.webservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for personelERP complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="personelERP">
 *   &lt;complexContent>
 *     &lt;extension base="{http://webService.pdks.com/}baseERPObject">
 *       &lt;sequence>
 *         &lt;element name="adi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bolumAdi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bolumKodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bordroAltAlanAdi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bordroAltAlanKodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cinsiyetKodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cinsiyeti" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="departmanAdi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="departmanKodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dogumTarihi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="gorevKodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="gorevi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="iseGirisTarihi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="istenAyrilmaTarihi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="kidemTarihi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="masrafYeriAdi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="masrafYeriKodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="personelNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="personelTipi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="personelTipiKodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sanalPersonel" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="sirketAdi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sirketKodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="soyadi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tesisAdi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tesisKodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="yoneticiPerNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="grubaGirisTarihi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="yonetici2PerNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="digerTanimAlan01" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="digerTanimAlan01Kodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="digerTanimAlan02" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="digerTanimAlan02Kodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="digerTanimAlan03" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="digerTanimAlan03Kodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="digerTanimAlan04" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="digerTanimAlan04Kodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="digerTanimAlan05" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="digerTanimAlan05Kodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="digerTanimAlan06" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="digerTanimAlan06Kodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="digerTanimAlan07" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="digerTanimAlan07Kodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="digerTanimAlan08" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="digerTanimAlan08Kodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="digerTanimAlan09" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="digerTanimAlan09Kodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="digerTanimAlan10" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="digerTanimAlan10Kodu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "personelERP", propOrder = { "adi", "bolumAdi", "bolumKodu", "bordroAltAlanAdi", "bordroAltAlanKodu", "cinsiyetKodu", "cinsiyeti", "departmanAdi", "departmanKodu", "dogumTarihi", "gorevKodu", "gorevi", "iseGirisTarihi", "istenAyrilmaTarihi", "kidemTarihi", "masrafYeriAdi",
		"masrafYeriKodu", "personelNo", "personelTipi", "personelTipiKodu", "sanalPersonel", "sirketAdi", "sirketKodu", "soyadi", "tesisAdi", "tesisKodu", "yoneticiPerNo", "grubaGirisTarihi", "yonetici2PerNo", "digerTanimAlan01", "digerTanimAlan01Kodu", "digerTanimAlan02", "digerTanimAlan02Kodu",
		"digerTanimAlan03", "digerTanimAlan03Kodu", "digerTanimAlan04", "digerTanimAlan04Kodu", "digerTanimAlan05", "digerTanimAlan05Kodu", "digerTanimAlan06", "digerTanimAlan06Kodu", "digerTanimAlan07", "digerTanimAlan07Kodu", "digerTanimAlan08", "digerTanimAlan08Kodu", "digerTanimAlan09",
		"digerTanimAlan09Kodu", "digerTanimAlan10", "digerTanimAlan10Kodu" })
public class PersonelERP extends BaseERPObject {

	protected String adi;
	protected String bolumAdi;
	protected String bolumKodu;
	protected String bordroAltAlanAdi;
	protected String bordroAltAlanKodu;
	protected String cinsiyetKodu;
	protected String cinsiyeti;
	protected String departmanAdi;
	protected String departmanKodu;
	protected String dogumTarihi;
	protected String gorevKodu;
	protected String gorevi;
	protected String iseGirisTarihi;
	protected String istenAyrilmaTarihi;
	protected String kidemTarihi;
	protected String masrafYeriAdi;
	protected String masrafYeriKodu;
	protected String personelNo;
	protected String personelTipi;
	protected String personelTipiKodu;
	protected Boolean sanalPersonel;
	protected String sirketAdi;
	protected String sirketKodu;
	protected String soyadi;
	protected String tesisAdi;
	protected String tesisKodu;
	protected String yoneticiPerNo;
	protected String grubaGirisTarihi;
	protected String yonetici2PerNo;
	protected String digerTanimAlan01;
	protected String digerTanimAlan01Kodu;
	protected String digerTanimAlan02;
	protected String digerTanimAlan02Kodu;
	protected String digerTanimAlan03;
	protected String digerTanimAlan03Kodu;
	protected String digerTanimAlan04;
	protected String digerTanimAlan04Kodu;
	protected String digerTanimAlan05;
	protected String digerTanimAlan05Kodu;
	protected String digerTanimAlan06;
	protected String digerTanimAlan06Kodu;
	protected String digerTanimAlan07;
	protected String digerTanimAlan07Kodu;
	protected String digerTanimAlan08;
	protected String digerTanimAlan08Kodu;
	protected String digerTanimAlan09;
	protected String digerTanimAlan09Kodu;
	protected String digerTanimAlan10;
	protected String digerTanimAlan10Kodu;

	/**
	 * Gets the value of the adi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getAdi() {
		return adi;
	}

	/**
	 * Sets the value of the adi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setAdi(String value) {
		this.adi = value;
	}

	/**
	 * Gets the value of the bolumAdi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getBolumAdi() {
		return bolumAdi;
	}

	/**
	 * Sets the value of the bolumAdi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setBolumAdi(String value) {
		this.bolumAdi = value;
	}

	/**
	 * Gets the value of the bolumKodu property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getBolumKodu() {
		return bolumKodu;
	}

	/**
	 * Sets the value of the bolumKodu property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setBolumKodu(String value) {
		this.bolumKodu = value;
	}

	/**
	 * Gets the value of the bordroAltAlanAdi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getBordroAltAlanAdi() {
		return bordroAltAlanAdi;
	}

	/**
	 * Sets the value of the bordroAltAlanAdi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setBordroAltAlanAdi(String value) {
		this.bordroAltAlanAdi = value;
	}

	/**
	 * Gets the value of the bordroAltAlanKodu property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getBordroAltAlanKodu() {
		return bordroAltAlanKodu;
	}

	/**
	 * Sets the value of the bordroAltAlanKodu property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setBordroAltAlanKodu(String value) {
		this.bordroAltAlanKodu = value;
	}

	/**
	 * Gets the value of the cinsiyetKodu property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCinsiyetKodu() {
		return cinsiyetKodu;
	}

	/**
	 * Sets the value of the cinsiyetKodu property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCinsiyetKodu(String value) {
		this.cinsiyetKodu = value;
	}

	/**
	 * Gets the value of the cinsiyeti property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCinsiyeti() {
		return cinsiyeti;
	}

	/**
	 * Sets the value of the cinsiyeti property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCinsiyeti(String value) {
		this.cinsiyeti = value;
	}

	/**
	 * Gets the value of the departmanAdi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDepartmanAdi() {
		return departmanAdi;
	}

	/**
	 * Sets the value of the departmanAdi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDepartmanAdi(String value) {
		this.departmanAdi = value;
	}

	/**
	 * Gets the value of the departmanKodu property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDepartmanKodu() {
		return departmanKodu;
	}

	/**
	 * Sets the value of the departmanKodu property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDepartmanKodu(String value) {
		this.departmanKodu = value;
	}

	/**
	 * Gets the value of the dogumTarihi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDogumTarihi() {
		return dogumTarihi;
	}

	/**
	 * Sets the value of the dogumTarihi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDogumTarihi(String value) {
		this.dogumTarihi = value;
	}

	/**
	 * Gets the value of the gorevKodu property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getGorevKodu() {
		return gorevKodu;
	}

	/**
	 * Sets the value of the gorevKodu property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setGorevKodu(String value) {
		this.gorevKodu = value;
	}

	/**
	 * Gets the value of the gorevi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getGorevi() {
		return gorevi;
	}

	/**
	 * Sets the value of the gorevi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setGorevi(String value) {
		this.gorevi = value;
	}

	/**
	 * Gets the value of the iseGirisTarihi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getIseGirisTarihi() {
		return iseGirisTarihi;
	}

	/**
	 * Sets the value of the iseGirisTarihi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setIseGirisTarihi(String value) {
		this.iseGirisTarihi = value;
	}

	/**
	 * Gets the value of the istenAyrilmaTarihi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getIstenAyrilmaTarihi() {
		return istenAyrilmaTarihi;
	}

	/**
	 * Sets the value of the istenAyrilmaTarihi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setIstenAyrilmaTarihi(String value) {
		this.istenAyrilmaTarihi = value;
	}

	/**
	 * Gets the value of the kidemTarihi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getKidemTarihi() {
		return kidemTarihi;
	}

	/**
	 * Sets the value of the kidemTarihi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setKidemTarihi(String value) {
		this.kidemTarihi = value;
	}

	/**
	 * Gets the value of the masrafYeriAdi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getMasrafYeriAdi() {
		return masrafYeriAdi;
	}

	/**
	 * Sets the value of the masrafYeriAdi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setMasrafYeriAdi(String value) {
		this.masrafYeriAdi = value;
	}

	/**
	 * Gets the value of the masrafYeriKodu property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getMasrafYeriKodu() {
		return masrafYeriKodu;
	}

	/**
	 * Sets the value of the masrafYeriKodu property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setMasrafYeriKodu(String value) {
		this.masrafYeriKodu = value;
	}

	/**
	 * Gets the value of the personelNo property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPersonelNo() {
		return personelNo;
	}

	/**
	 * Sets the value of the personelNo property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPersonelNo(String value) {
		this.personelNo = value;
	}

	/**
	 * Gets the value of the personelTipi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPersonelTipi() {
		return personelTipi;
	}

	/**
	 * Sets the value of the personelTipi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPersonelTipi(String value) {
		this.personelTipi = value;
	}

	/**
	 * Gets the value of the personelTipiKodu property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPersonelTipiKodu() {
		return personelTipiKodu;
	}

	/**
	 * Sets the value of the personelTipiKodu property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPersonelTipiKodu(String value) {
		this.personelTipiKodu = value;
	}

	/**
	 * Gets the value of the sanalPersonel property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean getSanalPersonel() {
		return sanalPersonel;
	}

	/**
	 * Sets the value of the sanalPersonel property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setSanalPersonel(Boolean value) {
		this.sanalPersonel = value;
	}

	/**
	 * Gets the value of the sirketAdi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSirketAdi() {
		return sirketAdi;
	}

	/**
	 * Sets the value of the sirketAdi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSirketAdi(String value) {
		this.sirketAdi = value;
	}

	/**
	 * Gets the value of the sirketKodu property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSirketKodu() {
		return sirketKodu;
	}

	/**
	 * Sets the value of the sirketKodu property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSirketKodu(String value) {
		this.sirketKodu = value;
	}

	/**
	 * Gets the value of the soyadi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSoyadi() {
		return soyadi;
	}

	/**
	 * Sets the value of the soyadi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSoyadi(String value) {
		this.soyadi = value;
	}

	/**
	 * Gets the value of the tesisAdi property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getTesisAdi() {
		return tesisAdi;
	}

	/**
	 * Sets the value of the tesisAdi property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setTesisAdi(String value) {
		this.tesisAdi = value;
	}

	/**
	 * Gets the value of the tesisKodu property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getTesisKodu() {
		return tesisKodu;
	}

	/**
	 * Sets the value of the tesisKodu property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setTesisKodu(String value) {
		this.tesisKodu = value;
	}

	/**
	 * Gets the value of the yoneticiPerNo property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getYoneticiPerNo() {
		return yoneticiPerNo;
	}

	/**
	 * Sets the value of the yoneticiPerNo property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setYoneticiPerNo(String value) {
		this.yoneticiPerNo = value;
	}

	/**
	 * Gets the value of the grubaGirisTarihi property.
	 * 
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public String getGrubaGirisTarihi() {
		return grubaGirisTarihi;
	}

	/**
	 * Sets the value of the grubaGirisTarihi property.
	 * 
	 * @param value
	 *            allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public void setGrubaGirisTarihi(String value) {
		this.grubaGirisTarihi = value;
	}

	/**
	 * Gets the value of the yonetici2PerNo property.
	 * 
	 * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public String getYonetici2PerNo() {
		return yonetici2PerNo;
	}

	/**
	 * Sets the value of the yonetici2PerNo property.
	 * 
	 * @param value
	 *            allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	public void setYonetici2PerNo(String value) {
		this.yonetici2PerNo = value;
	}

	public String getDigerTanimAlan01() {
		return digerTanimAlan01;
	}

	public void setDigerTanimAlan01(String digerTanimAlan01) {
		this.digerTanimAlan01 = digerTanimAlan01;
	}

	public String getDigerTanimAlan02() {
		return digerTanimAlan02;
	}

	public void setDigerTanimAlan02(String digerTanimAlan02) {
		this.digerTanimAlan02 = digerTanimAlan02;
	}

	public String getDigerTanimAlan03() {
		return digerTanimAlan03;
	}

	public void setDigerTanimAlan03(String digerTanimAlan03) {
		this.digerTanimAlan03 = digerTanimAlan03;
	}

	public String getDigerTanimAlan04() {
		return digerTanimAlan04;
	}

	public void setDigerTanimAlan04(String digerTanimAlan04) {
		this.digerTanimAlan04 = digerTanimAlan04;
	}

	public String getDigerTanimAlan05() {
		return digerTanimAlan05;
	}

	public void setDigerTanimAlan05(String digerTanimAlan05) {
		this.digerTanimAlan05 = digerTanimAlan05;
	}

	public String getDigerTanimAlan06() {
		return digerTanimAlan06;
	}

	public void setDigerTanimAlan06(String digerTanimAlan06) {
		this.digerTanimAlan06 = digerTanimAlan06;
	}

	public String getDigerTanimAlan07() {
		return digerTanimAlan07;
	}

	public void setDigerTanimAlan07(String digerTanimAlan07) {
		this.digerTanimAlan07 = digerTanimAlan07;
	}

	public String getDigerTanimAlan08() {
		return digerTanimAlan08;
	}

	public void setDigerTanimAlan08(String digerTanimAlan08) {
		this.digerTanimAlan08 = digerTanimAlan08;
	}

	public String getDigerTanimAlan09() {
		return digerTanimAlan09;
	}

	public void setDigerTanimAlan09(String digerTanimAlan09) {
		this.digerTanimAlan09 = digerTanimAlan09;
	}

	public String getDigerTanimAlan10() {
		return digerTanimAlan10;
	}

	public void setDigerTanimAlan10(String digerTanimAlan10) {
		this.digerTanimAlan10 = digerTanimAlan10;
	}

	public String getDigerTanimAlan01Kodu() {
		return digerTanimAlan01Kodu;
	}

	public void setDigerTanimAlan01Kodu(String digerTanimAlan01Kodu) {
		this.digerTanimAlan01Kodu = digerTanimAlan01Kodu;
	}

	public String getDigerTanimAlan02Kodu() {
		return digerTanimAlan02Kodu;
	}

	public void setDigerTanimAlan02Kodu(String digerTanimAlan02Kodu) {
		this.digerTanimAlan02Kodu = digerTanimAlan02Kodu;
	}

	public String getDigerTanimAlan03Kodu() {
		return digerTanimAlan03Kodu;
	}

	public void setDigerTanimAlan03Kodu(String digerTanimAlan03Kodu) {
		this.digerTanimAlan03Kodu = digerTanimAlan03Kodu;
	}

	public String getDigerTanimAlan04Kodu() {
		return digerTanimAlan04Kodu;
	}

	public void setDigerTanimAlan04Kodu(String digerTanimAlan04Kodu) {
		this.digerTanimAlan04Kodu = digerTanimAlan04Kodu;
	}

	public String getDigerTanimAlan05Kodu() {
		return digerTanimAlan05Kodu;
	}

	public void setDigerTanimAlan05Kodu(String digerTanimAlan05Kodu) {
		this.digerTanimAlan05Kodu = digerTanimAlan05Kodu;
	}

	public String getDigerTanimAlan06Kodu() {
		return digerTanimAlan06Kodu;
	}

	public void setDigerTanimAlan06Kodu(String digerTanimAlan06Kodu) {
		this.digerTanimAlan06Kodu = digerTanimAlan06Kodu;
	}

	public String getDigerTanimAlan07Kodu() {
		return digerTanimAlan07Kodu;
	}

	public void setDigerTanimAlan07Kodu(String digerTanimAlan07Kodu) {
		this.digerTanimAlan07Kodu = digerTanimAlan07Kodu;
	}

	public String getDigerTanimAlan08Kodu() {
		return digerTanimAlan08Kodu;
	}

	public void setDigerTanimAlan08Kodu(String digerTanimAlan08Kodu) {
		this.digerTanimAlan08Kodu = digerTanimAlan08Kodu;
	}

	public String getDigerTanimAlan09Kodu() {
		return digerTanimAlan09Kodu;
	}

	public void setDigerTanimAlan09Kodu(String digerTanimAlan09Kodu) {
		this.digerTanimAlan09Kodu = digerTanimAlan09Kodu;
	}

	public String getDigerTanimAlan10Kodu() {
		return digerTanimAlan10Kodu;
	}

	public void setDigerTanimAlan10Kodu(String digerTanimAlan10Kodu) {
		this.digerTanimAlan10Kodu = digerTanimAlan10Kodu;
	}

}
