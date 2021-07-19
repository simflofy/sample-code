package com.simflofy.sample;

import com.simflofy.core.common.EditPropertyFactory;
import com.simflofy.core.integration.document.RepositoryDocument;
import com.simflofy.core.integration.task.JobTaskStatus;
import com.simflofy.tasks.processors.AbstractTask;
import org.apache.log4j.Logger;
import org.apache.tika.io.IOUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author S. Arnold
 * date 7/16/21
 **/
@Service
@Scope("prototype")
public class SampleTask extends AbstractTask {

    //Fields we will write to each repository document.
    public static final String INPUT_FIELD = "sampleTaskInput";
    public static final String ERR_FIELD = "sampleTaskErr";
    public static final String COMMAND_STRING_FIELD = "commandField";
    public static final String INVALID_COMMAND_1 = "shutdown";
    public static final String TEST_CHECK_BOX = "testCheck";
    private static final Logger log = Logger.getLogger(SampleTask.class);
    //Holds the command value from user config.
    private String command;
    //Runtime for command execution.
    private Runtime runtime;

    /**
     * Constructor for each job run.
     */
    public SampleTask() {
        ResourceBundleMessageSource bundleMessageSource = new ResourceBundleMessageSource();
        bundleMessageSource.setBasename("i18/sample-task");
        bundleMessageSource.setDefaultEncoding("UTF-8");
        this.setMessageSource(bundleMessageSource);
        setMessageBase("sampleTask");
        setName("sampleTask");
    }

    /**
     * Generate a list of all form fields for user configuration.
     *
     * @return List<EditProperty> with each form field.
     */
    @Override
    public final List<com.simflofy.data.edit.EditProperty> getFormFields() {
        EditPropertyFactory epf = new EditPropertyFactory();
        epf.addTextEP(COMMAND_STRING_FIELD, getTaskMessage("commandFieldLabel"), "pwd");
        //This checkbox is completely ignored, but used as an example.
        epf.addCheckEP(TEST_CHECK_BOX, getTaskMessage("testMessage"), getTaskMessage("testMessageDescription"));
        return epf.getEpList();
    }

    /**
     * Do the actual work against each document in the queue.
     *
     * @param repositoryDocument next repo doc from queue.
     * @return JobTaskStatus
     */
    @Override
    public final JobTaskStatus process(RepositoryDocument repositoryDocument) {

        Process proc;
        BufferedReader lineReader = null;
        BufferedReader errorReader = null;
        try {
            //TODO: THIS SHOULD NEVER BE USED AS AN ACTUAL TASK. DEPLOYING THIS TASK (AS IS) TO A PRODUCTION SYSTEM WOULD BE DAFT.
            //noinspection CallToRuntimeExecWithNonConstantString
            proc = runtime.exec(command);

            //Add input field to repo document.
            lineReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            repositoryDocument.addSingleField(INPUT_FIELD, lineReader.lines().collect(Collectors.joining()));

            //Add error field to repo document (or you could capture and set status to error).
            errorReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            repositoryDocument.addSingleField(ERR_FIELD, errorReader.lines().collect(Collectors.joining()));

        } catch (IOException e) {
            log.error("Sample Task could not process document " + repositoryDocument.getSimflofySourceRepositoryId(), e);
            //Mark document as failed in the job run report.
            return JobTaskStatus.FAILED;
        } finally {
            IOUtils.closeQuietly(lineReader);
            IOUtils.closeQuietly(errorReader);
        }

        //Success, keep going.
        return JobTaskStatus.CONTINUE;
    }

    /**
     * Server-side form checking for easy access.
     *
     * @param formFields Map of all form fields available.
     * @return String message (for bad data) or null to indicate no issues.
     */
    @Override
    public final String validateFormFields(Map<String, String> formFields) {
        String cmd = formFields.get(COMMAND_STRING_FIELD);

        if (cmd.contains(INVALID_COMMAND_1))
            //Return to sender. This message could be localized.
            return "Invalid command found. '" + INVALID_COMMAND_1 + "' is not allowed.";

        //All good.
        return null;
    }

    /**
     * Initialize the processor for this job run. This is a good place
     * to set up clients, variables, and other tools needed for each document
     * being processed.
     *
     * @param formFields A list of form fields provided by the migration manager.
     */
    @Override
    public final void init(Map<String, String> formFields) {
        super.init(formFields);
        //Get the command from user's config.
        this.command = this.getFormFieldMap().get(COMMAND_STRING_FIELD);
        //Initialize runtime.
        this.runtime = Runtime.getRuntime();
    }

    /**
     * Close the processer after the job is complete.
     * messages-tasks_en.properties
     *
     * @throws IOException if issues arise from closing stream etc..
     */
    @Override
    public final void close() throws IOException {
        super.close();
        //Close out lingering clients/streams.
    }

}
