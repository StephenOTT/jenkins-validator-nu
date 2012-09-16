package si.nej.hudson.plugins;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import java.io.BufferedWriter;
import java.io.FileWriter;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NUValidationBuilder extends Builder {

    // configuration variables
    private final String validatorUrl;
    private final String siteUrl;
    private final String maxErrorsForStable;
    private final String maxWarningsForStable;
    private final String maxErrorsForUnstable;
    private final String maxWarningsForUnstable;

    // constants
    public static final String FILE_STRING_OUTPUT = "nu_output.html";
    public static final String FILE_ERRORS_APPEND = "_errors.properties";
    public static final String FILE_WARNINGS_APPEND = "_warnings.properties";

    // temp helper variables
    private FilePath workspaceRootDir = null;
    private NUValidation nuValidation = null;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public NUValidationBuilder(String validatorUrl, String siteUrl,
                               String maxErrorsForStable, String maxWarningsForStable,
                               String maxErrorsForUnstable, String maxWarningsForUnstable) {
        this.validatorUrl = validatorUrl;
        this.siteUrl = siteUrl;
        this.maxErrorsForStable = maxErrorsForStable;
        this.maxWarningsForStable = maxWarningsForStable;
        this.maxErrorsForUnstable = maxErrorsForUnstable;
        this.maxWarningsForUnstable = maxWarningsForUnstable;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getValidatorUrl() {
        return validatorUrl;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public String getMaxErrorsForStable() {
        return maxErrorsForStable;
    }

    public String getMaxErrorsForUnstable() {
        return maxErrorsForUnstable;
    }

    public String getMaxWarningsForStable() {
        return maxWarningsForStable;
    }

    public String getMaxWarningsForUnstable() {
        return maxWarningsForUnstable;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {

        // workspace path
        workspaceRootDir = build.getWorkspace();

        // validation
        nuValidation = new NUValidation();
        nuValidation.setValidatorUrl(validatorUrl);
        nuValidation.setSiteUrl(siteUrl);

        try {
            nuValidation.callService();
            nuValidation.parseObservers();

            listener.getLogger().println(nuValidation);
            saveStringOutput();
            setBuildStatus(build);

        } catch (MalformedURLException ex) {
            Logger.getLogger(NUValidationBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(NUValidationBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    // overrided for better type safety.
    // if your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Simple method that saves given String to File
     * @param file
     * @param content
     * @return
     */
    private boolean save2File(String file, String content) {

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(content);
            out.close();
        } catch (Exception e) {
            return false;
        }

        // on success return true
        return true;
    }

    /**
     *
     * @throws IOException
     */
    private void saveStringOutput() throws IOException {
        save2File(workspaceRootDir + "/" + FILE_STRING_OUTPUT, nuValidation.getOutputString());
    }

    /**
     * Method that sets the build status according to no. of errors and warnings
     * @param build
     */
    private void setBuildStatus(AbstractBuild build) {

        Boolean failed = false;
        Boolean unstable = false;

        System.out.println(this.maxErrorsForStable);
        System.out.println(this.maxWarningsForStable);
        System.out.println(this.maxErrorsForUnstable);
        System.out.println(this.maxWarningsForUnstable);

        int errors = 0;
        int warnings = 0;

        for (Observation observation : nuValidation.getObservations()) {
            if ("error".equals(observation.getType())) {
                errors++;
            }
            // TODO not sure this exists - a guess for now
            if ("warning".equals(observation.getType())) {
                warnings++;
            }
        }

        if ( errors > Integer.parseInt(this.maxErrorsForStable) ) {
            build.setResult(Result.UNSTABLE);
        }
        else if ( warnings > Integer.parseInt(this.maxWarningsForStable) ) {
            build.setResult(Result.UNSTABLE);
        }
        else if ( errors > Integer.parseInt(this.maxErrorsForUnstable) ) {
            build.setResult(Result.FAILURE);
        }
        else if ( warnings > Integer.parseInt(this.maxWarningsForUnstable) ) {
            build.setResult(Result.FAILURE);
        }
    }

    /**
     * Descriptor for {@link NUValidationBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>views/hudson/plugins/hello_world/NUValidationBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // this marker indicates Hudson that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckValidatorUrl(@QueryParameter String value) throws IOException, ServletException {
            if(value.length()==0)
                return FormValidation.error("Please set the validator service URL");
            if(value.length()<4)
                return FormValidation.warning("Isn't the name too short?");
            return FormValidation.ok();
        }

        public FormValidation doCheckSiteUrl(@QueryParameter String value) throws IOException, ServletException {
            if(value.length()==0)
                return FormValidation.error("Please set the correct URL");
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "NU Validator";
        }

        // remove ?
        // TODO
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req,formData);
        }
    }
}
