package si.nej.hudson.plugins;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;

/**
 *
 * @author jernejz
 */
public class NUValidation {

    // constants
    public static final String UNICORN_TASK = "conformance";    // all possible observers
    public static final String CONNECT_USERAGENT = "Mozilla";   // doesn't really matter which useragent you use
    public static final int CONNECT_TIMEOUT = 60000;            // 1 minute should be enough for Unicorn to return results

    private List<Observation> observations;
    private String validatorUrl;
    private String siteUrl;

    private String validationUrl;
    private String outputString;

    public NUValidation() {
         observations = new ArrayList<Observation>();
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public void setSiteUrl(String site_url) {
        this.siteUrl = site_url;
    }

    public String getValidatorUrl() {
        return validatorUrl;
    }

    public void setValidatorUrl(String unicorn_url) {
        this.validatorUrl = unicorn_url;
    }

    public String getOutputString() {
        return outputString;
    }

    public void setOutputString(String outputString) {
        this.outputString = outputString;
    }

    public List<Observation> getObservations() {
        return observations;
    }

    public void callService() throws IOException {
        Connection connection = HttpConnection.connect(validatorUrl)
            .data("doc", siteUrl)
            .data("schema", "http://s.validator.nu/w3c-html5-microdata-rdfa.rnc http://s.validator.nu/html5/assertions.sch http://c.validator.nu/base/ http://c.validator.nu/microdata/")
            .data("out", "json")
            .userAgent(CONNECT_USERAGENT)
            .cookie("auth", "token")
            .timeout(CONNECT_TIMEOUT)
            .method(Connection.Method.GET)
            .ignoreContentType(true);

        Connection.Response response = connection.execute();
        // TODO add basic response validation
        outputString = response.body();

        String encodedSiteURL = URLEncoder.encode(siteUrl,"utf-8");
        String encodedSchema = URLEncoder.encode("http://s.validator.nu/w3c-html5-microdata-rdfa.rnc http://s.validator.nu/html5/assertions.sch http://c.validator.nu/base/ http://c.validator.nu/microdata/","utf-8");
        validationUrl = validatorUrl + "?doc=" + encodedSiteURL + "&schema=" + encodedSchema;
    }

    public void parseObservers() {
        // TODO add basic response validation
        Object object = JSONValue.parse(outputString);
        JSONObject json = (JSONObject)object;
        // TODO add basic response validation
        Object messagesObject =json.get("messages");
        JSONArray messages = (JSONArray)messagesObject;
        // TODO add basic response validation
        for (Object messageObject : messages) {
            JSONObject msg = (JSONObject)messageObject;

            String type = (String)msg.get("type");
            Long lastLine = (Long)msg.get("lastLine");
            Long lastColumn = (Long)msg.get("lastColumn");
            String message = (String)msg.get("message");
            String extract = (String)msg.get("extract");
            Long hiliteStart = (Long)msg.get("hiliteStart");
            Long hiliteLength = (Long)msg.get("hiliteLength");

            observations.add(new Observation(type, lastLine, lastColumn, message, extract, hiliteStart, hiliteLength));
        }
    }

    public String outputResults() {
        return toString();
    }

    @Override
    public String toString() {

        String output = "\n";
        output += "---------------------------------------------------------\n";
        output += "NU validation results for " + siteUrl + "\n";
        output += "---------------------------------------------------------\n";
        output += "\n";

        int errors = 0;
        int warnings = 0;

        String details = "";

        for (Observation observation : observations) {
            if ("error".equals(observation.getType())) {
                errors++;
            }
            // TODO not sure this exists - a guess for now
            if ("warning".equals(observation.getType())) {
                warnings++;
            }

            details += observation;
            details += "\n";
        }

        output += "\n";
        output += "Errors: " + errors + "\n";
        output += "Warnings: " + warnings + "\n";
        output += "\n";
        output += "---------------------------------------------------------\n";
        output += "For the full report:  " + validationUrl + "\n";
        output += "---------------------------------------------------------\n";
        output += "\n";
        output += "---------------------------------------------------------\n";
        output += "Result dump follows...\n";
        output += "---------------------------------------------------------\n";
        output += "\n";
        output += details;
        output += "\n";

        return output;
    }

}
