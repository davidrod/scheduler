package es.uva.web.portal.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.mail.EmailException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.loader.CmsImageScaler;
import org.opencms.mail.CmsHtmlMail;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.Map;


/**
 * This class launch from a Opencms Scheduler Task
 * will send an email if the image is bigger than limit
 * Created by davidrod
 */
public class CheckImageSize extends CheckGeneric {

    private static final String PARAM_MAXHEIGHT = "max_height";
    private static final String PARAM_MAXWIDTH = "max_width";
    private static final String PARAM_MAXPIXELS = "max_pixels";

    private static final Log LOG = CmsLog.getLog(CheckImageSize.class);

    private int maxheight = 1024;
    private int maxwidth = 1024;
    private int maxpixels = 1048576;

    protected void doCheck(CmsObject cms, CmsResource element) throws Exception {
        super.doCheck(cms, element);

        int type = element.getTypeId();
        String str_link = OpenCms.getLinkManager().getServerLink(cms, element.getRootPath());

        if (type == 3) {
            CmsImageScaler scaler = new CmsImageScaler(cms, element);
            LOG.debug("IMAGE " + scaler.getHeight() + "x" + scaler.getWidth() + ": " + scaler.getPixelCount() + "<br/>");

            if ((scaler.getHeight() > maxheight) || (scaler.getWidth() > maxwidth) || (scaler.getPixelCount() > maxpixels)) {
                LOG.debug("-SIZE  EXCEED");
                LOG.debug("   " + scaler.getHeight() + " vs " + maxheight);
                LOG.debug("   " + scaler.getWidth() + " vs " + maxwidth);
                LOG.debug("   " + scaler.getPixelCount() + " vs " + maxpixels);

                //Send email to user
                CmsHtmlMail mail = new CmsHtmlMail();
                StringBuilder sb = new StringBuilder("<html><body>");
                sb.append("<h1>Imagen de gran tama&ntilde;</h1>");
                sb.append("<p>Estimado compa&ntilde;ero/a,</p>");
                sb.append("<p>Un proceso autom&aacute;tico ha detectado que en la ruta <a href=\"" + str_link + "\">" + str_link + "</a> existe una imagen con un tama&ntilde;o muy grande</p>");
                sb.append("<p>Te recomendamos que cambies el tama&ntilde;o de la imagen desde cualquier editor de im&aacute;genes</p>");
                sb.append("<p>Para cualquier duda puedes contactar con nosotros en la extensi&oacute;n 4771 o en el correo <a href=\"mailto:soporte-web@uva.es\">soporte-web@uva.es</a></p>");
                sb.append("</body></html>");
                try {
                    mail.setHtmlMsg(sb.toString());
                    mail.addTo("davidrod@uva.es");
                    mail.addBcc(cco);
                    mail.setSubject("Tamaño de imagen muy grande en la web");
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
        if (params.containsKey(PARAM_MAXHEIGHT)) maxheight = Integer.parseInt(params.get(PARAM_MAXHEIGHT));
        if (params.containsKey(PARAM_MAXWIDTH)) maxwidth = Integer.parseInt(params.get(PARAM_MAXWIDTH));
        if (params.containsKey(PARAM_MAXPIXELS)) maxpixels = Integer.parseInt(params.get(PARAM_MAXPIXELS));

        super.launch(cms, params);

        return null;
    }
}
