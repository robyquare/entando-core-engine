/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.entando.entando.plugins.jacms.aps.system.init.portdb;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.entando.entando.aps.system.init.IDatabaseManager;
import org.entando.entando.aps.system.init.model.ExtendedColumnDefinition;
import org.entando.entando.aps.system.init.model.portdb.Category;

/**
 * @author E.Santoboni
 */
@DatabaseTable(tableName = WorkContentRelation.TABLE_NAME)
public class WorkContentRelation implements ExtendedColumnDefinition {
	
	public WorkContentRelation() {}
	
	@DatabaseField(foreign = true, columnName = "contentid", 
			width = 16, 
			canBeNull = false, index = true)
	private Content _content;
	
	@DatabaseField(foreign = true, columnName = "refcategory", 
			width = 30, index = true)
	private Category _category;
	
	@Override
	public String[] extensions(IDatabaseManager.DatabaseType type) {
		String tableName = TABLE_NAME;
		String contentTableName = Content.TABLE_NAME;
		if (IDatabaseManager.DatabaseType.MYSQL.equals(type)) {
			tableName = "`" + tableName + "`";
			contentTableName = "`" + contentTableName + "`";
		}
		return new String[]{"ALTER TABLE " + tableName + " " 
				+ "ADD CONSTRAINT " + TABLE_NAME + "_contentid_fkey FOREIGN KEY (contentid) "
				+ "REFERENCES " + contentTableName + " (contentid)"};
	}
	
	public static final String TABLE_NAME = "workcontentrelations";
	
}
/*
CREATE TABLE workcontentrelations
(
  contentid character varying(16) NOT NULL,
  refcategory character varying(30),
  CONSTRAINT workcontentrelations_contentid_fkey FOREIGN KEY (contentid)
      REFERENCES contents (contentid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
 */