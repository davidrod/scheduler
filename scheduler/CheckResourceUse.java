package es.uva.web.portal.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.mail.EmailException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.mail.CmsHtmlMail;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;

import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * This class launch from a Opencms Scheduler Task
 * will send an email if the resource is not linked from any resource
 * Created by davidrod
 */
public class CheckResourceUse extends CheckGeneric {
    private static final Log LOG = CmsLog.getLog(CheckResourceUse.class);

    protected void doCheck(CmsObject cms, CmsResource element) throws Exception {
        super.doCheck(cms, element);

        int type = element.getTypeId();
        String str_link = OpenCms.getLinkManager().getServerLink(cms, element.getRootPath());

        boolean hasTarget = false;
        if ((type == 2) || (type == 3)) { //Only images and docs
            List<CmsRelation> relaciones = cms.getRelationsForResource(element, CmsRelationFilter.ALL);
            Iterator<CmsRelation> j = relaciones.iterator();
            while (j.hasNext()) {
                CmsRelation relacion = (CmsRelation) j.next();
                String str_source = relacion.getSourcePath();
                String str_target = relacion.getTargetPath();
                LOG.debug("-SOURCE " + str_source);
                LOG.debug("-TARGET " + str_target);
                CmsResource target = relacion.getTarget(cms, CmsResourceFilter.ALL);
                hasTarget = true;
            }

            if (hasTarget == false) {
                CmsHtmlMail mail = new CmsHtmlMail();
                StringBuilder sb = new StringBuilder("<html><body>");
                sb.append("<h1>Recurso no usado</h1>");
                sb.append("<p>Estimado compa&ntilde;ero/a,</p>");
                sb.append("<p>Un proceso autom&aacute;tico ha detectado que en la ruta <a href=\"" + str_link + "\">" + str_link + "</a> existe un recurso que no está siendo usado desde ninguna p&aacute;gina</p>");
                sb.append("<p>Para cualquier duda puedes contactar con nosotros en la extensi&oacute;n 4771 o en el correo <a href=\"mailto:soporte-web@uva.es\">soporte-web@uva.es</a></p>");
                sb.append("</body></html>");
                try {
                    mail.setHtmlMsg(sb.toString());
                    mail.addTo("davidrod@uva.es");
                    mail.addBcc(cco);
                    mail.setSubject("Recurso no usado en la web");
                    String messageID = mail.send();
                } catch (EmailException e) {
                    LOG.error("Exception sending mail: " + e.getCause());
                }
            }
        }
    }

    @Override
    public String launch(CmsObject cms, Map<String, String> params)
            throws Exception {
        //Parámetros específicos

        super.launch(cms, params);

        return null;
    }
}
