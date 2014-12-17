package es.uva.web.portal.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.mail.EmailException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.mail.CmsHtmlMail;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.Map;


/**
 * This class launch from a Opencms Scheduler Task
 * will send an email if the size of the file exceed a limit
 * Created by davidrod
 */
public class CheckFileSize extends CheckGeneric {

    private static final String PARAM_MAXSIZE = "max_size";

    private static final Log LOG = CmsLog.getLog(CheckFileSize.class);

    private int maxsize = 16000000;

    protected void doCheck(CmsObject cms, CmsResource element) throws Exception {
        super.doCheck(cms, element);
        int size = element.getLength();
        String str_link = OpenCms.getLinkManager().getServerLink(cms, element.getRootPath());

        if (size > maxsize) {
            LOG.debug("-TAM " + maxsize + " EXCEED");
            //Send email to user
            CmsHtmlMail mail = new CmsHtmlMail();
            StringBuilder sb = new StringBuilder("<html><body>");
            sb.append("<h1>Tama&ntilde;o de fichero permitido excedido</h1>");
            sb.append("<p>Estimado compa&ntilde;ero/a,</p>");
            sb.append("<p>Un proceso autom&aacute;tico ha detectado que en la ruta <a href=\"" + str_link + "\">" + str_link + "</a> existe un fichero que excede el tama&ntilde;o m&aacute;ximo permitido</p>");
            sb.append("<p>Para cualquier duda puedes contactar con nosotros en la extensi&oacute;n 4771 o en el correo <a href=\"mailto:soporte-web@uva.es\">soporte-web@uva.es</a></p>");
            sb.append("</body></html>");
            try {
                mail.setHtmlMsg(sb.toString());
                mail.addTo("davidrod@uva.es");
                mail.addBcc(cco);
                mail.setSubject("Tamaño de fichero permitido excedido en la web");
                String messageID = mail.send();
            } catch (EmailException e) {
                LOG.error("Exception sending mail: " + e.getCause());
            }
        }

    }

    @Override
    public String launch(CmsObject cms, Map<String, String> params)
            throws Exception {
        //Parámetros específicos
        if (params.containsKey(PARAM_MAXSIZE)) maxsize = Integer.parseInt(params.get(PARAM_MAXSIZE));

        super.launch(cms, params);

        return null;
    }
}
