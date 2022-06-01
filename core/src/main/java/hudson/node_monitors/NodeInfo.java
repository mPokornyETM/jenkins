package hudson.node_monitors;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;
import hudson.remoting.Callable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Displays the labels that are defined for an agent.
 */
public class NodeInfo extends NodeMonitor
{
  // per default is everything false, so it will has the same look like before
  private boolean showDynamicLabels = false;
  private int maxLabelCount = 0;
  private boolean showDescription = false;
  private boolean showConnectSince = false;
  private boolean showLaunchMode = false;
  private boolean showOfflineCause = false;
  private boolean showConnectingState = false;

  public NodeInfo() {}

  @DataBoundConstructor
  public NodeInfo(boolean showDynamicLabels, int maxLabelCount, boolean showDescription, boolean showConnectSince, boolean showLaunchMode, boolean showOfflineCause, boolean showConnectingState)
  {
    this.showDynamicLabels = showDynamicLabels;
    if (maxLabelCount == 0) {
      maxLabelCount = 1;
    }
    this.maxLabelCount = maxLabelCount;
    this.showDescription = showDescription;
    this.showConnectSince = showConnectSince;
    this.showLaunchMode = showLaunchMode;
    this.showOfflineCause = showOfflineCause;
    this.showConnectingState = showConnectingState;
  }

  @Override
  public boolean isIgnored() {
    return false; // this is node column caption, Can not be ignored
  }

  public boolean getShowDynamicLabels()
  {
    return showDynamicLabels;
  }

  public int getMaxLabelCount()
  {
    return maxLabelCount;
  }

  public boolean getShowDescription()
  {
    return showDescription;
  }

  public boolean getShowConnectSince()
  {
    return showConnectSince;
  }

  public boolean getShowLaunchMode()
  {
    return showLaunchMode;
  }

  public boolean getShowOfflineCause()
  {
    return showOfflineCause;
  }

  public boolean getShowConnectingState()
  {
    return showConnectingState;
  }

  @Override
  public Object data(Computer c) {
    Node n = c.getNode();
    Set<LabelAtom> data = Collections.emptySet();
    if (n != null)
    {
      data = Label.parse(n.getLabelString());
      if (this.showDynamicLabels)
      {
        data.addAll(n.getDynamicLabels());
      }
    }
    return new Data(c.getName(), data, maxLabelCount);
  }

  @Override
  public NodeMonitorNodeInfoColumn getColumn() {
    return new NodeMonitorNodeInfoColumn();
  }

  // @Restricted(DoNotUse.class)
  // @ExportedBean(defaultVisibility = 0)
  public static class NodeMonitorNodeInfoColumn extends NodeMonitorColumn {

    public NodeMonitorNodeInfoColumn() {
    }

    @Override
    public int getPreferredPosition() {
      return 2;
    }

    @Override
    public boolean isImplemented() {
      return true;
    }
  }

  @Restricted(DoNotUse.class)
  @ExportedBean(defaultVisibility = 0)
  public static class Data {
    private final String computerName;
    private final int maxLabelsCount;
    private final Set<LabelAtom> labels;

    private Data(String computerName, Set<LabelAtom> labels, int maxLabelsCount) {
      this.computerName = computerName;
      this.labels = labels;
      this.maxLabelsCount = maxLabelsCount;
    }

    public String getComputerName()
    {
      return computerName;
    }

    public Set<LabelAtom> getAllowedLabels()
    {
      Set<LabelAtom> allowedLabels = new HashSet<LabelAtom>();
      int i = 0;
      for (LabelAtom label : labels) {
        if (i >= this.maxLabelsCount)
          break;

        allowedLabels.add(label);
        i++;
      }
      return Collections.unmodifiableSet(allowedLabels);
    }

    public Set<LabelAtom> getAllLabels()
    {
      return Collections.unmodifiableSet(labels);
    }
  }

  @Extension
  @Symbol("nodeInfo")
  public static class DescriptorImpl extends AbstractAsyncNodeMonitorDescriptor<Set<LabelAtom>> {

    @Override
    public String getDisplayName() {
        return Messages.NodeInfo_DisplayName();
    }

    @Override
    protected Callable<Set<LabelAtom>, IOException> createCallable(Computer c)
    {
      return null;
    }
  }

}