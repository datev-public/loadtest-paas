package de.datev.samples.loadtest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
@ConfigurationProperties(
        prefix = "app-config",
        ignoreUnknownFields = false
)
public class LoadTestConfiguration {

    private boolean showConfigOnStartup = false;
    private long defaultSleepTimeMilliseconds = 10;
    private int defaultFibInput = 35;
    private int defaultReturnSize = 102400;
    private int defaultNumberOfKiloByteBlocks = 1024;
    private String defaultRemoteUrl = "$PROTOCOL$://$HOST$:$PORT$/api/test/fib?input=1";
    private int defaultMemoryFactor = 16;
    private String urlReplacementBase = null;
    private boolean forceSsl = false;
    private boolean forwardedHeaderFilterActivated = false;
    private String forwardedHeaderFilterPatterns = null;
    private boolean forwardedHeaderFilterRemoveOnly = false;
    private boolean forwardedHeaderFilterRelativeRedirect = false;
    private String propagatedHeader = "Authorization";
    private HashSet<String> propagatedHeaderSet = new HashSet<>();

    public boolean isShowConfigOnStartup() {
        return showConfigOnStartup;
    }

    public void setShowConfigOnStartup(boolean showConfigOnStartup) {
        this.showConfigOnStartup = showConfigOnStartup;
    }

    public void setPropagatedHeaderSet(HashSet<String> propagatedHeaderSet) {
        this.propagatedHeaderSet = propagatedHeaderSet;
    }

    public long getDefaultSleepTimeMilliseconds() {
        return defaultSleepTimeMilliseconds;
    }

    public void setDefaultSleepTimeMilliseconds(long defaultSleepTimeMilliseconds) {
        this.defaultSleepTimeMilliseconds = defaultSleepTimeMilliseconds;
    }

    public int getDefaultFibInput() {
        return defaultFibInput;
    }

    public void setDefaultFibInput(int defaultFibInput) {
        this.defaultFibInput = defaultFibInput;
    }

    public int getDefaultReturnSize() {
        return defaultReturnSize;
    }

    public void setDefaultReturnSize(int defaultReturnSize) {
        this.defaultReturnSize = defaultReturnSize;
    }

    public int getDefaultNumberOfKiloByteBlocks() {
        return defaultNumberOfKiloByteBlocks;
    }

    public void setDefaultNumberOfKiloByteBlocks(int defaultNumberOfKiloByteBlocks) {
        this.defaultNumberOfKiloByteBlocks = defaultNumberOfKiloByteBlocks;
    }

    public String getDefaultRemoteUrl() {
        return defaultRemoteUrl;
    }

    public void setDefaultRemoteUrl(String defaultRemoteUrl) {
        this.defaultRemoteUrl = defaultRemoteUrl;
    }

    public int getDefaultMemoryFactor() {
        return defaultMemoryFactor;
    }

    public void setDefaultMemoryFactor(int defaultMemoryFactor) {
        this.defaultMemoryFactor = defaultMemoryFactor;
    }

    public String getUrlReplacementBase() {
        return urlReplacementBase;
    }

    public void setUrlReplacementBase(String urlReplacementBase) {
        this.urlReplacementBase = urlReplacementBase;
    }

    public boolean isForceSsl() {
        return forceSsl;
    }

    public void setForceSsl(boolean forceSsl) {
        this.forceSsl = forceSsl;
    }

    public boolean isForwardedHeaderFilterActivated() {
        return forwardedHeaderFilterActivated;
    }

    public void setForwardedHeaderFilterActivated(boolean forwardedHeaderFilterActivated) {
        this.forwardedHeaderFilterActivated = forwardedHeaderFilterActivated;
    }

    public boolean isForwardedHeaderFilterRemoveOnly() {
        return forwardedHeaderFilterRemoveOnly;
    }

    public void setForwardedHeaderFilterRemoveOnly(boolean forwardedHeaderFilterRemoveOnly) {
        this.forwardedHeaderFilterRemoveOnly = forwardedHeaderFilterRemoveOnly;
    }

    public String getForwardedHeaderFilterPatterns() {
        return forwardedHeaderFilterPatterns;
    }

    public void setForwardedHeaderFilterPatterns(String forwardedHeaderFilterPatterns) {
        this.forwardedHeaderFilterPatterns = forwardedHeaderFilterPatterns;
    }

    public boolean isForwardedHeaderFilterRelativeRedirect() {
        return forwardedHeaderFilterRelativeRedirect;
    }

    public void setForwardedHeaderFilterRelativeRedirect(boolean forwardedHeaderFilterRelativeRedirect) {
        this.forwardedHeaderFilterRelativeRedirect = forwardedHeaderFilterRelativeRedirect;
    }

    public String getPropagatedHeader() {
        return propagatedHeader;
    }

    public HashSet<String> getPropagatedHeaderSet() {
        return propagatedHeaderSet;
    }

    public void setPropagatedHeader(String propagedHeader) {
        this.propagatedHeader = propagedHeader;
        this.propagatedHeaderSet.clear();
        if (this.propagatedHeader != null && this.propagatedHeader.trim().length() > 0) {
            for (String s : this.propagatedHeader.split(",")) {
                if (s.trim().length() > 0) {
                    this.propagatedHeaderSet.add(s.trim());
                }
            }
        }
    }

    @Override
    public String toString() {
        return "LoadTestConfiguration{" +
                "showConfigOnStartup=" + showConfigOnStartup +
                ", defaultSleepTimeMilliseconds=" + defaultSleepTimeMilliseconds +
                ", defaultFibInput=" + defaultFibInput +
                ", defaultReturnSize=" + defaultReturnSize +
                ", defaultNumberOfKiloByteBlocks=" + defaultNumberOfKiloByteBlocks +
                ", defaultRemoteUrl='" + defaultRemoteUrl + '\'' +
                ", defaultMemoryFactor=" + defaultMemoryFactor +
                ", urlReplacementBase='" + urlReplacementBase + '\'' +
                ", forceSsl=" + forceSsl +
                ", forwardedHeaderFilterActivated=" + forwardedHeaderFilterActivated +
                ", forwardedHeaderFilterRemoveOnly=" + forwardedHeaderFilterRemoveOnly +
                ", forwardedHeaderFilterPatterns='" + forwardedHeaderFilterPatterns + '\'' +
                ", propagatedHeader='" + propagatedHeader + '\'' +
                '}';
    }
}
