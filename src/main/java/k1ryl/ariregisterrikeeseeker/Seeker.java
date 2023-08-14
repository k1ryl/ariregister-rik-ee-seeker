package k1ryl.ariregisterrikeeseeker;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.PrintStream;

import static java.nio.charset.StandardCharsets.UTF_8;

@RestController
public class Seeker {

    @GetMapping("/{regNumber}")
    public ResponseEntity<Object> search(@PathVariable String regNumber) {
        try (final WebClient webClient = new WebClient(BrowserVersion.FIREFOX)) {
            // Enable JavaScript for HtmlUnit
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getOptions().setCssEnabled(false);

            // Navigate to the search page
            HtmlPage searchPage = webClient.getPage("https://ariregister.rik.ee/eng");

            // Locate the input field for name, registry code, or VAT number
            HtmlTextInput inputField = searchPage.getFirstByXPath("//input[@name='name_or_code']");

            // Set the value for the input field (e.g., "80032273" as the registration number)
            inputField.setValueAttribute(regNumber);

            // Locate the search button and simulate the click
            HtmlButton submitButton = searchPage.getFirstByXPath("//button[@type='submit']");
            HtmlPage resultsPage = submitButton.click();

            // Get the resulting page's content as an XML string
            String resultsXml = resultsPage.asXml();

            // Parse the XML content using Jsoup with explicit UTF-8 encoding
            Document doc = Jsoup.parse(resultsXml);

            // Extract the company name and address
            Element companyNameElement = doc.selectFirst("a.h2.text-primary");
            Elements addressElements = doc.select("div.col.font-weight-bold");


            System.setOut(new PrintStream(System.out, true, UTF_8));

            if (companyNameElement == null || addressElements.isEmpty()) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.ok().body(new Response(companyNameElement.text(), addressElements.get(1).text()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.internalServerError().build();
    }
}