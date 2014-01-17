package edu.harvard.iq.dataverse.api;

import edu.harvard.iq.dataverse.DataFile;
import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.DatasetServiceBean;
import java.util.List;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("files")
public class Files {

    private static final Logger logger = Logger.getLogger(Files.class.getCanonicalName());

    @EJB
    DatasetServiceBean datasetService;

    @POST
    public String add(DataFile dataFile) {
        Dataset dataset;
        try {
            dataset = datasetService.find(dataFile.getOwner().getId());
        } catch (EJBException ex) {
            return Util.message2ApiError("Couldn't find dataset to save file to. File was " + dataFile);
        }
        List<DataFile> newListOfFiles = dataset.getFiles();
        newListOfFiles.add(dataFile);
        dataset.setFiles(newListOfFiles);
        try {
            datasetService.save(dataset);
            return "file " + dataFile.getName() + " created/updated with dataset " + dataset.getTitle() + " (and probably indexed, check server.log)\n";
        } catch (EJBException ex) {
            Throwable cause = ex;
            StringBuilder sb = new StringBuilder();
            sb.append(ex);
            while (cause.getCause() != null) {
                cause = cause.getCause();
                sb.append(cause.getClass().getCanonicalName() + " ");
                if (cause instanceof ConstraintViolationException) {
                    ConstraintViolationException constraintViolationException = (ConstraintViolationException) cause;
                    for (ConstraintViolation<?> violation : constraintViolationException.getConstraintViolations()) {
                        sb.append("(invalid value: <<<" + violation.getInvalidValue() + ">>> for " + violation.getPropertyPath() + " at " + violation.getLeafBean() + " - " + violation.getMessage() + ")");
                    }
                }
            }
            return Util.message2ApiError("POST failed: " + sb.toString());
        }
//        return "file " + dataFile.getName() + " indexed dataset " + dataFile.getName() + " files updated (and probably indexed, check server.log)\n";
    }

}
