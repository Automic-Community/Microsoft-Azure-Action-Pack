package com.automic.azure.actions;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.exception.AzureException;
import com.automic.azure.util.CommonUtil;
import com.automic.azure.util.ConsoleWriter;

/**
 * This class will delete file from disk
 * 
 * 
 */

/**
 * 
 * @author kamalgarg
 * 
 */
public final class DeleteFileAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(DeleteFileAction.class);

    private String filePath;
    private boolean failMissing;
    private File file;
    private boolean isExist;
    private boolean isFile;

    public DeleteFileAction() {
        addOption("filepath", true, "File path to be deleted");
        addOption("failifmissing", false, "Flag to decide action behaviour");
    }

    /**
     * To delete a file
     * 
     */
    @Override
    public void execute() throws AzureException {
        initialize();

        boolean isDeleted = false;
        if (isExist) {
            if (!isFile) {
                String msg = String.format("Unable to delete file [%s]. Provided path is not pointing to a file",
                        filePath);
                LOGGER.error(msg);
                throw new AzureException(msg);
            }
            isDeleted = file.delete();
            if (!isDeleted) {
                String msg = String.format("Unable to delete file [%s]", filePath);
                LOGGER.error(msg);
                throw new AzureException(msg);
            }

        } else if (failMissing) {
            String msg = String.format("Unable to delete file [%s]. Provided file does not exist", filePath);
            LOGGER.error(msg);
            throw new AzureException(msg);
        }

        ConsoleWriter.writeln("File deleted [" + isDeleted + "]");

    }

    private void initialize() {
        filePath = getOptionValue("filepath");
        file = new File(filePath);
        isExist = file.exists();
        if (isExist) {
            isFile = file.isFile();
        }
        failMissing = CommonUtil.convert2Bool(getOptionValue("failifmissing"));

        ConsoleWriter.writeln("EXISTS [" + isExist + "] " + "FILE [" + isFile + "] ");
    }

    protected List<String> noLogging() {
        return Collections.emptyList();
    }

}
