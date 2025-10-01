package org.pdks.dao;

/**
 * @author Hasan Sayar Genel objelerin veritabani islemleri.
 */
public interface PdksDAO extends BaseDAO {

	/**
	 * @param name
	 * @param type
	throws Exception * @return
	 */
	public boolean isExisObject(String name, String type) throws Exception;

}
