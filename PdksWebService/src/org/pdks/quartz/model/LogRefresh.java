package org.pdks.quartz.model;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.pdks.dao.PdksDAO;
import org.pdks.dao.impl.BaseDAOHibernate;
import org.pdks.entity.ServiceData;
import org.pdks.genel.model.Constants;
import org.pdks.genel.model.PdksUtil;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.pdks.webService.PdksVeriOrtakAktar;

public final class LogRefresh extends QuartzJobBean {

	private Logger logger = Logger.getLogger(LogRefresh.class);

	// ERROR //
	protected void executeInternal(org.quartz.JobExecutionContext ctx) throws org.quartz.JobExecutionException {
		tesisRefresh(logger);
	}

	public void tesisRefresh(Logger logger) {
		PdksDAO pdksDAO = Constants.pdksDAO;
		try {
			PdksVeriOrtakAktar p = new PdksVeriOrtakAktar();
			String parameter = p.getParametreDeger("kgsMasterUpdate");
			if (parameter != null && parameter.equals("2")) {
				LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
				veriMap.put(BaseDAOHibernate.MAP_KEY_SELECT, "SP_GET_PDKS_SIRKET_ISLEM");
				pdksDAO.execSP(veriMap);
			}

		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		try {
			HashMap fields = new HashMap();
 			fields.put("d", PdksUtil.tariheAyEkleCikar(new Date(), -2));
			StringBuffer sb = new StringBuffer();
			sb.append("select V.* from " + ServiceData.TABLE_NAME + " V " + PdksVeriOrtakAktar.getSelectLOCK());
			sb.append(" where  V." + ServiceData.COLUMN_NAME_OLUSTURMA_TARIHI + " < :d  ");
			List dataList = pdksDAO.getNativeSQLList(fields, sb, ServiceData.class);
			if (dataList != null && !dataList.isEmpty())
				pdksDAO.deleteObjectList(dataList);
		} catch (Exception e) {

		}

	}

}
