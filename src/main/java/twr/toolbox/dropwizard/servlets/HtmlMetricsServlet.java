package twr.toolbox.dropwizard.servlets;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricProcessor;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Sampling;
import com.yammer.metrics.core.Summarizable;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.stats.Snapshot;

/**
 * An HTTP servlet which outputs the metrics in a {@link MetricsRegistry}. Only responds to {@code GET} requests.
 * <p/>
 * If the servlet context has an attribute named {@code com.yammer.metrics.reporting.MetricsServlet.registry} which is a
 * {@link MetricsRegistry} instance, {@link HtmlMetricsServlet} will use it instead of {@link Metrics}.
 * <p/>
 * {@code GET} requests to {@link HtmlMetricsServlet} can make use of the following query-string parameters:
 * <dl>
 * <dt><code>/metrics?class=com.example.service</code></dt>
 * <dd>
 * <code>class</code> is a string used to filter the metrics by metric name. In the given example, only metrics for
 * classes whose canonical name starts with <code>com.example.service</code> would be shown.</dd>
 */
public class HtmlMetricsServlet extends HttpServlet implements MetricProcessor<HtmlMetricsServlet.Context> {

    /**
     * The attribute name of the {@link MetricsRegistry} instance in the servlet context.
     */
    public static final String REGISTRY_ATTRIBUTE = HtmlMetricsServlet.class.getName() + ".registry";

    static final class Context {
        final OutputStream output;

        public Context(OutputStream output) {
            this.output = output;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlMetricsServlet.class);
    private static final String CONTENT_TYPE = "text/html";

    private MetricsRegistry registry;

    /**
     * Creates a new {@link HtmlMetricsServlet}.
     */
    public HtmlMetricsServlet() {
        this(Metrics.defaultRegistry());
    }

    /**
     * Creates a new {@link HtmlMetricsServlet}.
     * 
     * @param registry
     *            a {@link MetricsRegistry}
     */
    public HtmlMetricsServlet(MetricsRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        final Object o = config.getServletContext().getAttribute(REGISTRY_ATTRIBUTE);
        if (o instanceof MetricsRegistry) {
            this.registry = (MetricsRegistry) o;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String classPrefix = req.getParameter("class");

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(CONTENT_TYPE);
        final OutputStream output = resp.getOutputStream();

        writeHeader(output);
        writeMetrics(output, classPrefix);
        writeFooter(output);

        output.close();
    }

    private void writeHeader(OutputStream output) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<html>\n");
        html.append("<head>\n");

        html.append("<link rel=\"stylesheet\" href=\"../assets/themes/blue/style.css\" type=\"text/css\" media=\"print, projection, screen\" />\n");
        html.append("<script src=\"http://code.jquery.com/jquery-1.9.1.min.js\"></script>\n");
        html.append("<script type=\"text/javascript\" src=\"../assets/jquery.tablesorter.min.js\"></script>\n");

        html.append("</head>\n");
        html.append("<table id=\"metrics\" class=\"tablesorter\">\n");

        html.append(tableHeader());

        html.append("<tbody>\n");

        IOUtils.write(html, output);
    }

    private void writeFooter(OutputStream output) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("</tbody>\n");

        html.append("</table>\n");
        html.append("<script>\n");
        html.append("$(document).ready(function() { $('#metrics').tablesorter({widgets: ['zebra'], sortList: [[4,1]]}); });\n");
        html.append("</script>\n");

        html.append("</body>\n");
        html.append("</html>");

        IOUtils.write(html, output);
    }

    private String tableHeader() {
        StringBuilder head = new StringBuilder("<thead>\n<tr>\n");

        List<String> columnHeaders = asList("group", "api", "duration unit", "min", "max", "mean", "std_dev", "median",
                "p75", "p95", "p98", "p99", "p999", "rate unit", "count", "mean", "m1", "m5", "m15");
        for (String each : columnHeaders) {
            head.append("<th>").append(each).append("</th>");
        }

        head.append("\n</tr>\n</thead>\n");
        return head.toString();
    }

    private void writeMetrics(OutputStream output, String classPrefix) throws IOException {
        for (Map.Entry<String, SortedMap<MetricName, Metric>> entry : registry.groupedMetrics().entrySet()) {
            if (include(entry.getKey()) && (classPrefix == null || entry.getKey().startsWith(classPrefix))) {

                for (Map.Entry<MetricName, Metric> subEntry : entry.getValue().entrySet()) {
                    try {
                        subEntry.getValue().processWith(this, subEntry.getKey(), new Context(output));
                    } catch (Exception e) {
                        LOGGER.warn("Error writing out {}", subEntry.getKey(), e);
                    }
                }
            }
        }
    }

    private boolean include(String name) {
        return !asList("com.yammer.metrics.web.WebappMetricsFilter").contains(name);
    }

    @Override
    public void processHistogram(MetricName name, Histogram histogram, Context context) throws Exception {
        // ignore
    }

    @Override
    public void processCounter(MetricName name, Counter counter, Context context) throws Exception {
        // ignore
    }

    @Override
    public void processGauge(MetricName name, Gauge<?> gauge, Context context) throws Exception {
        // ignore
    }

    @Override
    public void processMeter(MetricName name, Metered meter, Context context) throws Exception {
        // ignore
    }

    @Override
    public void processTimer(MetricName name, Timer timer, Context context) throws Exception {
        final OutputStream output = context.output;

        IOUtils.write("<tr>", output);
        writeCell(name.getGroup(), output);
        writeCell(name.getType() + " " + name.getName(), output);

        writeCell(timer.durationUnit().toString().toLowerCase(), output);
        writeSummarizable(timer, output);
        writeSampling(timer, output);

        writeMeteredFields(timer, output);

        IOUtils.write("</tr>\n", output);
    }

    private static void writeSummarizable(Summarizable metric, OutputStream output) throws IOException {
        writeCell(metric.min(), output);
        writeCell(metric.max(), output);
        writeCell(metric.mean(), output);
        writeCell(metric.stdDev(), output);
    }

    private static void writeSampling(Sampling metric, OutputStream output) throws IOException {
        final Snapshot snapshot = metric.getSnapshot();
        writeCell(snapshot.getMedian(), output);
        writeCell(snapshot.get75thPercentile(), output);
        writeCell(snapshot.get95thPercentile(), output);
        writeCell(snapshot.get98thPercentile(), output);
        writeCell(snapshot.get99thPercentile(), output);
        writeCell(snapshot.get999thPercentile(), output);
    }

    private static void writeMeteredFields(Metered metered, OutputStream output) throws IOException {
        writeCell(metered.rateUnit().toString().toLowerCase(), output);
        writeCell(metered.count(), output);
        writeCell(metered.meanRate(), output);
        writeCell(metered.oneMinuteRate(), output);
        writeCell(metered.fiveMinuteRate(), output);
        writeCell(metered.fifteenMinuteRate(), output);
    }

    private static void writeCell(double value, OutputStream output) throws IOException {
        writeCell(String.format("%.0f", value), output);
    }

    private static void writeCell(long value, OutputStream output) throws IOException {
        writeCell(String.valueOf(value), output);
    }

    private static void writeCell(Object value, OutputStream output) throws IOException {
        IOUtils.write("<td>", output);
        IOUtils.write(String.valueOf(value), output);
        IOUtils.write("</td>", output);
    }

}
