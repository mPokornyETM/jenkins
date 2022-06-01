/*
 * The MIT License
 */

package hudson.node_monitors;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.labels.LabelAtom;
import hudson.remoting.Callable;
import java.io.IOException;
import java.util.Set;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Displays the labels that are defined for an agent.
 */
public class NodeStatus extends NodeMonitor
{
  @DataBoundConstructor
  public NodeStatus() {}

  @Override
  public NodeMonitorNodeStatusColumn getColumn() {
    return new NodeMonitorNodeStatusColumn();
  }

  // @Restricted(DoNotUse.class)
  // @ExportedBean(defaultVisibility = 0)
  public static class NodeMonitorNodeStatusColumn extends NodeMonitorColumn {

    public NodeMonitorNodeStatusColumn() {
    }

    @Override
    public int getPreferredPosition() {
      return 1;
    }

    @Override
    public boolean isImplemented() {
      return true;
    }
  }

  @Extension
  @Symbol("nodeStatus")
  public static class DescriptorImpl extends AbstractAsyncNodeMonitorDescriptor<Set<LabelAtom>> {

    @Override
    public String getDisplayName() {
        return Messages.NodeStatus_DisplayName();
    }

    @Override
    protected Callable<Set<LabelAtom>, IOException> createCallable(Computer c)
    {
      return null;
    }
  }
}