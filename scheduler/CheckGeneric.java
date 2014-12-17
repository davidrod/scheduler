package es.uva.web.portal.scheduler;

import org.apache.commons.logging.Log;
import org.opencms.file.*;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.util.CmsUUID;

import java.util.*;

/**
 * Created by davidrod on 17/12/14.
 */
public class CheckGeneric implements I_CmsScheduledJob {
    public static final String PARAM_USER = "user";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_MAILCCO = "mailcco";
    public static final String PARAM_ALLTIME = "alltime";

    private static final Log LOG = CmsLog.getLog(CheckGeneric.class);

    protected String login_user = "Admin";
    protected String login_password = "admin";
    protected String cco = "soporte-web@uva.es";
    protected Boolean alltime = false;

    protected void doCheck(CmsObject cms, CmsResource element) throws Exception {
        String name = element.getRootPath();
        int size = element.getLength();
        int type = element.getTypeId();
        String str_type = OpenCms.getResourceManager().getResourceType(element).getTypeName();
        CmsUUID id_userCreated = element.getUserCreated();
        CmsUser userCreated = cms.readUser(id_userCreated);
        String str_userCreated = userCreated.getEmail();
        CmsUUID id_userModified = element.getUserLastModified();
        CmsUser userModified = cms.readUser(id_userModified);
        String str_userModified = userModified.getEmail();
        String str_link = OpenCms.getLinkManager().getServerLink(cms, element.getRootPath());

        LOG.debug("-NAME " + name);
        LOG.debug("-LINK " + str_link);
        LOG.debug("-SIZE " + size);
        LOG.debug("-ID TYPE " + type);
        LOG.debug("-USER CREATED " + str_userCreated);
        LOG.debug("-USER LAST MODIFIED " + str_userModified);
    }

    @Override
    public String launch(CmsObject cms, Map<String, String> params)
            throws Exception {
        Date inicio = new Date();
        Date modificado = new Date();
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(modificado.getTime());
        cal.add(Calendar.DATE, -1); //Yesterday
        inicio.setTime(cal.getTimeInMillis());

        if (params.containsKey(PARAM_USER)) login_user = params.get(PARAM_USER);
        if (params.containsKey(PARAM_PASSWORD)) login_password = params.get(PARAM_PASSWORD);
        if (params.containsKey(PARAM_MAILCCO)) cco = params.get(PARAM_MAILCCO);
        if (params.containsKey(PARAM_ALLTIME)) alltime = Boolean.parseBoolean(params.get(PARAM_ALLTIME));

        //Nos autenticamos
        cms.loginUser(login_user, login_password);
        CmsRequestContext cmsContext = cms.getRequestContext();
        CmsProject curProject = cmsContext.currentProject();

        //Cambiamos al proyecto Offline
        if (curProject.isOnlineProject()) {
            CmsProject offlineProject = cms.readProject("Offline");
            cmsContext.setCurrentProject(offlineProject);
        }
        cms.getRequestContext().setSiteRoot("/");

        LOG.debug("Start to process resources");
        //Recorremos los recursos
        List<CmsResource> recursos = new ArrayList<CmsResource>();
        recursos = cms.readResources("/", CmsResourceFilter.ALL, true);
        Iterator<CmsResource> i = recursos.iterator();
        while (i.hasNext()) {
            CmsResource element = (CmsResource) i.next();
            if (alltime == false) {
                modificado.setTime(element.getDateLastModified());
                if (modificado.after(inicio)) {
                    doCheck(cms, element);
                }
            } else {
                doCheck(cms, element);
            }
        }
        LOG.debug("Finish process resources");
        return null;
    }
}
