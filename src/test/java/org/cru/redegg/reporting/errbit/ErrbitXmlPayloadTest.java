package org.cru.redegg.reporting.errbit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import org.cru.redegg.reporting.ErrorReport;
import org.cru.redegg.reporting.WebContext;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Matt Drees
 */
public class ErrbitXmlPayloadTest
{

    StringWriter writer;
    ErrbitConfig config;
    private ErrorReport report;


    @Before
    public void setup()
    {
        writer = new StringWriter();
        config = new ErrbitConfig();
        config.setEnvironmentName("unittest");
        config.setKey("secrets");
        config.getApplicationBasePackages().add("org.cru.redegg");
        config.setSourcePrefix("src/test/java");
    }

    @Test
    public void testWriteXmlTo() throws Exception
    {
        buildSampleReport();
        ErrbitXmlPayload payload = new ErrbitXmlPayload(report, config);
        payload.writeXmlTo(writer);
        checkXmlContent();
    }

    private void buildSampleReport() throws Exception
    {
        report = new DummyReportBuilder().buildDummyReport();
    }

    private void checkXmlContent()
    {
        String xml = writer.toString();

        System.out.println(xml.replace("><", ">\n<"));

        assertThat(xml, startsWith("<?xml "));
        assertThat(xml, containsString("<notice version=\"2.4\">"));
        assertThat(xml, containsString("<api-key>secrets</api-key>"));
        assertThat(xml, containsString("<class>java.lang.RuntimeException</class>"));
        assertThat(xml, containsString("<line method=\"caused by: java.lang.NullPointerException\">"));
        assertThat(xml, containsString("<message>something bad happened</message>"));
        assertThat(xml, containsString("<component>TestResource</component>"));
        assertThat(xml, containsString("<action>doSomething(String)</action>"));
        assertThat(xml, containsString("<var key=\"lang\">[en_au, en]</var>"));
        assertThat(xml, containsString("<var key=\"context:conferenceId\">10912</var>"));
        assertThat(xml, containsString("<var key=\"env:JAVA_OPTS\">-Xmx 512m</var>"));
        assertThat(xml, containsString("<var key=\"HTTP_X_PROXY\">[proxy.somewhere.org, proxy.somewhereelse.org]</var>"));
        assertThat(xml, containsString("<var key=\"Server Hostname\">testserver.cru.org</var>"));
        assertThat(xml, containsString("<guid>ABE4234-FASFA</guid>"));
        assertThat(xml, containsString("<framework>[resteasy-jaxrs-3.0.5.Final, weld-core-1.1.16]</framework>"));
        assertThat(xml, endsWith("</notice>"));

    }
}
