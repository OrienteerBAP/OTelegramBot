package org.orienteer.telegram.component;

import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.IModel;
import org.orienteer.core.util.StartupPropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Upload user certificate file and contains path to it
 */
public class OWebHookCertificateUploadPanel extends FormComponentPanel<String> {

    private static final Logger LOG = LoggerFactory.getLogger(OWebHookCertificateUploadPanel.class);

    private FileUploadField uploadField;

    private static final String CERTIFICATE_PATH = StartupPropertiesLoader.retrieveProperties().getProperty("orienteer.telegram.certificates.path");

    public OWebHookCertificateUploadPanel(String id, IModel<String> model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(uploadField = new FileUploadField("upload"));
    }

    @Override
    public void convertInput() {
        Path result = uploadFile();
        if (result != null) {
            setConvertedInput(result.toAbsolutePath().toString());
        } else setConvertedInput(getModelObject());
    }
    
    private Path uploadFile() {
        Path result = null;
        FileUpload upload = uploadField.getFileUpload();
        if (upload != null) {
            try {
                result = createFile(upload.getClientFileName());
                InputStream in = upload.getInputStream();
                OutputStream out = Files.newOutputStream(result);
                byte[] buff = new byte[4096];
                int read;
                while ((read = in.read(buff)) != -1) {
                    out.write(buff, 0, read);
                }
                in.close();
                out.close();
            } catch (IOException e) {
                result = null;
                LOG.error("Can't write certificate file to file system: ", e);
            }
        }
        return result;
    }

    private Path createFile(String name) {
        Path file = null;
        try {
            file = getOrCreateFolder(CERTIFICATE_PATH).resolve(name);
            Files.deleteIfExists(file);
            Files.createFile(file);
        } catch (IOException e) {
            LOG.error("Can't create file with name '{}'!", name, e);
        }
        return file;
    }

    private Path getOrCreateFolder(String folder) throws IOException {
        Path path = Paths.get(folder);
        if (!Files.isDirectory(path)) {
            Files.createDirectory(path);
        }
        return path;
    }
}
