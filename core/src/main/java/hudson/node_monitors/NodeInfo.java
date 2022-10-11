/*
 * The MIT License
 */

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

//-----------------------------------------------------------------------------
/**
 * Represent node-info column in /computer page.
 *
 * The column shows node name with direct link to the node self.
 * It is also possible to enable / disable following additional information:
 * + assigned labels
 * + assigned dynamic labels
 * + node description
 * + offline cause
 * + connect since
 * + launch mode
 * + connecting status
 *
 * @note This is not typical node-monitor, because it will never change online state.
 */
@Extension
@Restricted(DoNotUse.class)
public class NodeInfo extends NodeMonitor
{
  // per default is everything false, so it will has the same look like before
  // max count of labels: 0 means disabled, < 0 means all
  private int maxLabelCount = 0;
  // enable dynamic labels too
  private boolean showDynamicLabels = false;
  // show node descriptions
  private boolean showDescription = false;
  // show connected since text
  private boolean showConnectSince = false;
  // show launch mode
  private boolean showLaunchMode = false;
  // show offline cause
  private boolean showOfflineCause = false;
  // show connecting state
  private boolean showConnectingState = false;

  //---------------------------------------------------------------------------
  public NodeInfo() {}

  //---------------------------------------------------------------------------
  @DataBoundConstructor
  public NodeInfo(boolean showDynamicLabels, int maxLabelCount,
                  boolean showDescription, boolean showConnectSince,
                  boolean showLaunchMode, boolean showOfflineCause,
                  boolean showConnectingState)
  {
    this.showDynamicLabels = showDynamicLabels;
    this.maxLabelCount = maxLabelCount;
    this.showDescription = showDescription;
    this.showConnectSince = showConnectSince;
    this.showLaunchMode = showLaunchMode;
    this.showOfflineCause = showOfflineCause;
    this.showConnectingState = showConnectingState;
  }

  //---------------------------------------------------------------------------
  @Override
  public boolean isIgnored() {
    return false; // this is node column caption, Can not be ignored
  }

  //---------------------------------------------------------------------------
  /**
   * Show dynamic labels or not.
   */
  @SuppressWarnings("unused") // used by jelly view
  public boolean getShowDynamicLabels()
  {
    return showDynamicLabels;
  }

  //---------------------------------------------------------------------------
  /**
   * Returns max count of labels to be shown.
   */
  @SuppressWarnings("unused") // used by jelly view
  public int getMaxLabelCount()
  {
    return maxLabelCount;
  }

  //---------------------------------------------------------------------------
  /**
   * Show node description or not.
   */
  @SuppressWarnings("unused") // used by jelly view
  public boolean getShowDescription()
  {
    return showDescription;
  }

  //---------------------------------------------------------------------------
  /**
   * Show connect-since text or not.
   */
  public boolean getShowConnectSince()
  {
    return showConnectSince;
  }

  //---------------------------------------------------------------------------
  /**
   * Show launch mode or not.
   */
  public boolean getShowLaunchMode()
  {
    return showLaunchMode;
  }

  //---------------------------------------------------------------------------
  /**
   * Show offline cause or not.
   */
  public boolean getShowOfflineCause()
  {
    return showOfflineCause;
  }

  //---------------------------------------------------------------------------
  /**
   * Show connecting state or not.
   */
  public boolean getShowConnectingState()
  {
    return showConnectingState;
  }

  //---------------------------------------------------------------------------
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

  //---------------------------------------------------------------------------
  @Override
  public NodeMonitorNodeInfoColumn getColumn() {
    return new NodeMonitorNodeInfoColumn();
  }

  //---------------------------------------------------------------------------
  @Restricted(DoNotUse.class)
  @ExportedBean(defaultVisibility = 0)
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

  //---------------------------------------------------------------------------
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

    public String getComputerName() {
      return computerName;
    }

    //-------------------------------------------------------------------------
    public Set<LabelAtom> getAllowedLabels() {

      // return all labels
      if (this.maxLabelsCount < 0) {
        return Collections.unmodifiableSet(labels);
      }

      // return allowed count only
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

    public Set<LabelAtom> getAllLabels() {
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