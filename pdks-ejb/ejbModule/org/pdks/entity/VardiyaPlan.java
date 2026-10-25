package org.pdks.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.persistence.Transient;

import org.apache.log4j.Logger;

public class VardiyaPlan extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3733637996158688725L;
	static Logger logger = Logger.getLogger(VardiyaPlan.class);

	private Personel personel;

	private VardiyaHafta vardiyaHafta1, vardiyaHafta2, vardiyaHafta3, vardiyaHafta4, vardiyaHafta5, vardiyaHafta6;

	private DepartmanDenklestirmeDonemi denklestirmeDonemi;

	private List<VardiyaHafta> vardiyaHaftaList = new ArrayList<VardiyaHafta>();

	private TreeMap<String, VardiyaGun> vardiyaGunMap = new TreeMap<String, VardiyaGun>();

	private List<PersonelDenklestirmeTasiyici> personelDenklestirmeList;

	private double hesaplananSure = 0.0d;
	
	private Double toplamCalismaSaati = 0.0d, toplamKusuratliArgeGun = 0.0d;

	private Integer toplamArgeGun = 0, toplamResmiTatil = 0, toplamHaftaTatil = 0, toplamArge = 0, toplamDgs = 0, argeGun = 0, calismaGun = 0, kalanDgs = 0;

	public VardiyaPlan() {
		super();

	}

	public VardiyaPlan(Personel personel) {
		super();
		this.personel = personel;
	}

	public void planVardiyaPlanSifirla() {
		personelDenklestirmeList = new ArrayList<PersonelDenklestirmeTasiyici>();
		for (int i = 0; i < 3; i++)
			personelDenklestirmeList.add(new PersonelDenklestirmeTasiyici());
	}

	public Personel getPersonel() {
		return personel;
	}

	public void setPersonel(Personel personel) {
		this.personel = personel;
	}

	public VardiyaHafta getVardiyaHafta1() {
		return vardiyaHafta1;
	}

	public void setVardiyaHafta1(VardiyaHafta vardiyaHafta1) {
		this.vardiyaHafta1 = vardiyaHafta1;
	}

	public VardiyaHafta getVardiyaHafta2() {
		return vardiyaHafta2;
	}

	public void setVardiyaHafta2(VardiyaHafta vardiyaHafta2) {
		this.vardiyaHafta2 = vardiyaHafta2;
	}

	public List<VardiyaHafta> getVardiyaHaftaList() {
		return vardiyaHaftaList;
	}

	public void setVardiyaHaftaList(List<VardiyaHafta> vardiyaHaftaList) {
		this.vardiyaHaftaList = vardiyaHaftaList;
	}

	public boolean isDurum() {
		return denklestirmeDonemi == null || denklestirmeDonemi.isAcik();
	}

	public double getHesaplananSure() {
		return hesaplananSure;
	}

	public void setHesaplananSure(double hesaplananSure) {
		this.hesaplananSure = hesaplananSure;
	}

	public DepartmanDenklestirmeDonemi getDenklestirmeDonemi() {
		return denklestirmeDonemi;
	}

	public void setDenklestirmeDonemi(DepartmanDenklestirmeDonemi denklestirmeDonemi) {
		this.denklestirmeDonemi = denklestirmeDonemi;
	}

	public TreeMap<String, VardiyaGun> getVardiyaGunMap() {
		return vardiyaGunMap;
	}

	public void setVardiyaGunMap(TreeMap<String, VardiyaGun> vardiyaGunMap) {
		this.vardiyaGunMap = vardiyaGunMap;
	}

	public String getSortKey() {
		return personel != null ? personel.getAdSoyad() + "_" + personel.getSicilNo() : "";
	}

	public Double getPersonelDenklestirme(int index) {
		Double denklestirmeTutar = null;
		try {
			if (index <= personelDenklestirmeList.size())
				denklestirmeTutar = personelDenklestirmeList.get(index).getCalisilanFark();
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		return denklestirmeTutar;
	}

	public Double getPersonelDenklestirmeToplam(int index) {
		Double denklestirmeTutar = null;
		try {
			if (index <= personelDenklestirmeList.size()) {
				denklestirmeTutar = 0D;
				for (int i = 0; i <= index; i++)
					denklestirmeTutar += personelDenklestirmeList.get(i).getCalisilanFark();
			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		return denklestirmeTutar;
	}

	public void setPersonelDenklestirme(PersonelDenklestirmeTasiyici denklestirme, int index) {
		try {
			personelDenklestirmeList.set(index, denklestirme);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
	}

	public List<PersonelDenklestirmeTasiyici> getPersonelDenklestirmeList() {
		return personelDenklestirmeList;
	}

	public void setPersonelDenklestirmeList(List<PersonelDenklestirmeTasiyici> personelDenklestirmeList) {
		this.personelDenklestirmeList = personelDenklestirmeList;
	}

	public VardiyaHafta getVardiyaHafta3() {
		return vardiyaHafta3;
	}

	public void setVardiyaHafta3(VardiyaHafta vardiyaHafta3) {
		this.vardiyaHafta3 = vardiyaHafta3;
	}

	public VardiyaHafta getVardiyaHafta4() {
		return vardiyaHafta4;
	}

	public void setVardiyaHafta4(VardiyaHafta vardiyaHafta4) {
		this.vardiyaHafta4 = vardiyaHafta4;
	}

	public VardiyaHafta getVardiyaHafta5() {
		return vardiyaHafta5;
	}

	public void setVardiyaHafta5(VardiyaHafta vardiyaHafta5) {
		this.vardiyaHafta5 = vardiyaHafta5;
	}

	public VardiyaHafta getVardiyaHafta6() {
		return vardiyaHafta6;
	}

	public void setVardiyaHafta6(VardiyaHafta vardiyaHafta6) {
		this.vardiyaHafta6 = vardiyaHafta6;
	}

	public Personel getPdksPersonel() {
		return personel;
	}

	public void entityRefresh() {

	}

	@Transient
	public String getTableName() {
		return null;
	}

	public Double getToplamCalismaSaati() {
		return toplamCalismaSaati;
	}

	public void setToplamCalismaSaati(Double toplamCalismaSaati) {
		this.toplamCalismaSaati = toplamCalismaSaati;
	}

	public Double getToplamKusuratliArgeGun() {
		return toplamKusuratliArgeGun;
	}

	public void setToplamKusuratliArgeGun(Double toplamKusuratliArgeGun) {
		this.toplamKusuratliArgeGun = toplamKusuratliArgeGun;
	}

	public Integer getToplamArgeGun() {
		return toplamArgeGun;
	}

	public void setToplamArgeGun(Integer toplamArgeGun) {
		this.toplamArgeGun = toplamArgeGun;
	}

	public Integer getToplamResmiTatil() {
		return toplamResmiTatil;
	}

	public void setToplamResmiTatil(Integer toplamResmiTatil) {
		this.toplamResmiTatil = toplamResmiTatil;
	}

	public Integer getToplamHaftaTatil() {
		return toplamHaftaTatil;
	}

	public void setToplamHaftaTatil(Integer toplamHaftaTatil) {
		this.toplamHaftaTatil = toplamHaftaTatil;
	}

	public Integer getToplamArge() {
		return toplamArge;
	}

	public void setToplamArge(Integer toplamArge) {
		this.toplamArge = toplamArge;
	}

	public Integer getToplamDgs() {
		return toplamDgs;
	}

	public void setToplamDgs(Integer toplamDgs) {
		this.toplamDgs = toplamDgs;
	}

	public Integer getArgeGun() {
		return argeGun;
	}

	public void setArgeGun(Integer argeGun) {
		this.argeGun = argeGun;
	}

	public Integer getCalismaGun() {
		return calismaGun;
	}

	public void setCalismaGun(Integer calismaGun) {
		this.calismaGun = calismaGun;
	}

	public Integer getKalanDgs() {
		return kalanDgs;
	}

	public void setKalanDgs(Integer kalanDgs) {
		this.kalanDgs = kalanDgs;
	}
}
