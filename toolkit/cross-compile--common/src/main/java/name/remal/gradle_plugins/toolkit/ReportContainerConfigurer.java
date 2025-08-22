package name.remal.gradle_plugins.toolkit;

import org.gradle.api.reporting.CustomizableHtmlReport;
import org.gradle.api.reporting.DirectoryReport;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.tasks.testing.JUnitXmlReport;
import org.jspecify.annotations.Nullable;

interface ReportContainerConfigurer {

    SingleFileReport addSingleFileReport(String reportName);

    DirectoryReport addDirectoryReport(String reportName, @Nullable String relativeEntryPath);

    CustomizableHtmlReport addCustomizableHtmlReport(String reportName);

    JUnitXmlReport addJUnitXmlReport(String reportName);

}
