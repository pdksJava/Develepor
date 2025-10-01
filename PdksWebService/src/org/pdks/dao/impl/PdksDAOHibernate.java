package org.pdks.dao.impl;

import java.util.HashMap;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.pdks.dao.PdksDAO;

import com.pdks.webService.PdksVeriOrtakAktar;

/**
 * @author Hasan Sayar Genel objelerin veritabani islemleri.
 */
public class PdksDAOHibernate extends BaseDAOHibernate implements PdksDAO {

	@Override
	public boolean isExisObject(String name, String type) throws Exception {
		Session session = getHibernateCurrentSession();
		boolean durum = false;
		StringBuffer sb = new StringBuffer();
		sb.append("select name, object_id from sys.objects " + PdksVeriOrtakAktar.getSelectLOCK());
		sb.append(" where name = :k and type = :t");
		HashMap fields = new HashMap();
		fields.put("k", name);
		fields.put("t", type);
		List listNew = null;
		try {
			SQLQuery query = session.createSQLQuery(sb.toString());
			query.setParameter("k", name);
			query.setParameter("t", type);
			listNew = query.list();

		} catch (Exception e) {

			e.printStackTrace();

		}
		durum = listNew != null && listNew.size() == 1;
		return durum;

	}

}
