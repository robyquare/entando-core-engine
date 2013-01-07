/*
*
* Copyright 2012 Entando S.r.l. (http://www.entando.com) All rights reserved.
*
* This file is part of Entando software.
* Entando is a free software; 
* you can redistribute it and/or modify it
* under the terms of the GNU General Public License (GPL) as published by the Free Software Foundation; version 2.
* 
* See the file License for the specific language governing permissions   
* and limitations under the License
* 
* 
* 
* Copyright 2012 Entando S.r.l. (http://www.entando.com) All rights reserved.
*
*/
package org.entando.entando.aps.system.init;

import org.entando.entando.aps.system.init.model.ComponentEnvinroment;
import org.entando.entando.aps.system.init.model.Component;
import com.agiletec.aps.system.ApsSystemUtils;
import com.agiletec.aps.system.exception.ApsSystemException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import org.entando.entando.aps.system.init.model.ComponentInstallationReport;

import org.entando.entando.aps.system.init.model.SystemInstallationReport;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;

/**
 * @author E.Santoboni
 */
public class InitializerManager extends AbstractInitializerManager {
	
	public void init() throws Exception {
		if (!this.isCheckOnStartup()) {
			ApsSystemUtils.getLogger().config(this.getClass().getName() + ": short init executed");
			return;
		}
		SystemInstallationReport report = null;
		try {
			report = this.extractReport();
			report = ((IDatabaseInstallerManager) this.getDatabaseManager()).installDatabase(report);
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "init", "Error while initializating Db Installer");
			throw new Exception("Error while initializating Db Installer", t);
		} finally {
			if (null != report && report.isUpdated()) {
				this.saveReport(report);
			}
		}
		ApsSystemUtils.getLogger().config(this.getClass().getName() + ": initializated");
	}
	
	protected void executePostInitProcesses() throws BeansException {
		if (!this.isCheckOnStartup()) {
			return;
		}
		System.out.println("*******************************");
		SystemInstallationReport report = null;
		try {
			report = this.extractReport();
			List<Component> components = this.getComponentManager().getCurrentComponents();
			for (int i = 0; i < components.size(); i++) {
				Component component = components.get(i);
				ComponentInstallationReport componentReport = report.getComponentReport(component.getCode(), false);
				SystemInstallationReport.Status postProcessStatus = componentReport.getPostProcessStatus();
				if (!postProcessStatus.equals(SystemInstallationReport.Status.INIT)) {
					continue;
				}
				ComponentEnvinroment componentEnvinroment = (null != component.getEnvironments()) ? 
						component.getEnvironments().get(this.getEnvironment().toString()) :
						null;
				List<IPostProcessor> postProcesses = (null != componentEnvinroment) ? componentEnvinroment.getPostProcesses() : null;
				if (null != postProcesses && !postProcesses.isEmpty()) {
					//TODO
					componentReport.setPostProcessStatus(SystemInstallationReport.Status.OK);
				} else {
					componentReport.setPostProcessStatus(SystemInstallationReport.Status.NOT_AVAILABLE);
				}
				report.setUpdated();
			}
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "executePostInitProcesses", "Error while executing post processes");
			throw new FatalBeanException("Error while executing post processes", t);
		} finally {
			if (null != report && report.isUpdated()) {
				this.saveReport(report);
			}
		}
		System.out.println("POST PROCESS");
		System.out.println("*******************************");
	}
	
	//-------------------- REPORT -------- START
	
	private void saveReport(SystemInstallationReport report) throws BeansException {
		if (null == report || report.getReports().isEmpty()) {
			return;
		}
		try {
			InstallationReportDAO dao = new InstallationReportDAO();
			DataSource dataSource = (DataSource) this.getBeanFactory().getBean("portDataSource");
			dao.setDataSource(dataSource);
			dao.saveConfigItem(report.toXml(), this.getConfigVersion());
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "saveReport");
			throw new FatalBeanException("Error saving report", t);
		}
	}
	
	protected boolean isCheckOnStartup() {
		return _checkOnStartup;
	}
	public void setCheckOnStartup(boolean checkOnStartup) {
		this._checkOnStartup = checkOnStartup;
	}
	
	public Map<String, IPostProcessor> getPostProcessors() {
		return _postProcessors;
	}
	public void setPostProcessors(Map<String, IPostProcessor> postProcessors) {
		this._postProcessors = postProcessors;
	}
	
	protected IComponentManager getComponentManager() {
		return _componentManager;
	}
	public void setComponentManager(IComponentManager componentManager) {
		this._componentManager = componentManager;
	}
	
	protected IDatabaseManager getDatabaseManager() {
		return _databaseManager;
	}
	public void setDatabaseManager(IDatabaseManager databaseManager) {
		this._databaseManager = databaseManager;
	}
	
	private boolean _checkOnStartup;
	
	private Map<String, IPostProcessor> _postProcessors;
	
	private IComponentManager _componentManager;
	private IDatabaseManager _databaseManager;
	
	public static final String REPORT_CONFIG_ITEM = "entandoComponentsReport";
	
}