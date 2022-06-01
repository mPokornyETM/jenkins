package hudson.node_monitors;

import hudson.Extension;
import hudson.model.Computer;
import hudson.remoting.Callable;
import hudson.util.RemotingDiagnostics;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Logger;
import jenkins.YesNoMaybe;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Displays the labels that are defined for an agent.
 */
public class NodeProperty extends NodeMonitor
{
  private static final Logger LOGGER = Logger.getLogger(NodeProperty.class.getName());

  String propertyValueGroovy;
  String propertyRangeGroovy;
  String propertyDetailGroovy;
  String columnName;
  String columnTooltip;

  //-----------------------------------------------------------------------------
  public NodeProperty() {}

  //-----------------------------------------------------------------------------
  @DataBoundConstructor
  public NodeProperty(String propertyValueGroovy, String propertyRangeGroovy, String propertyDetailGroovy, String columnName, String columnTooltip) {
    this.propertyValueGroovy = propertyValueGroovy;
    this.propertyRangeGroovy = propertyRangeGroovy;
    this.propertyDetailGroovy = propertyDetailGroovy;
    this.columnName = columnName;
    this.columnTooltip = columnTooltip;
  }

  //-----------------------------------------------------------------------------
  @Override
  public NodeMonitorNodePropertyColumn getColumn() {
    return new NodeMonitorNodePropertyColumn(this.columnName, this.columnTooltip);
  }

  //-----------------------------------------------------------------------------
  // @Restricted(DoNotUse.class)
  // @ExportedBean(defaultVisibility = 0)
  public static class NodeMonitorNodePropertyColumn extends NodeMonitorColumn {

    private final String columnName;
    private final String columnTooltip;

    //---------------------------------------------------------------------------
    public NodeMonitorNodePropertyColumn(String columnName, String columnTooltip) {
      this.columnName = columnName;
      this.columnTooltip = columnTooltip;
    }

    //---------------------------------------------------------------------------
    @Override
    public boolean isImplemented() {
      return true;
    }

    //---------------------------------------------------------------------------
    public String getName() {
      return this.columnName;
    }

    //---------------------------------------------------------------------------
    public String getTooltip() {
      if (this.columnTooltip == null || this.columnTooltip.isEmpty())
        return getName();

      return this.columnTooltip;
    }
  }

  //-----------------------------------------------------------------------------
  @Extension
  @Symbol("nodeProperty")
  public static class DescriptorImpl extends AbstractAsyncNodeMonitorDescriptor<String> {

    //---------------------------------------------------------------------------
    @Override
    public String getDisplayName() {
      // if (propertyName != null && !propertyName.isEmpty()) {
      //   return propertyName;
      // }
      return Messages.NodeProperty_DisplayName();
    }

    //---------------------------------------------------------------------------
    @Override
    protected Callable<String, IOException> createCallable(Computer c)
    {
      return null;
    }

    // @Override
    //     protected Map<Computer, Data> monitor() throws InterruptedException {
    //       LOGGER.log('monitor prop');
    //         Result<Data> base = monitorDetailed();
    //         Map<Computer, Data> monitoringData = base.getMonitoringData();
    //         for (Map.Entry<Computer, Data> e : monitoringData.entrySet()) {
    //             Computer c = e.getKey();
    //             Data d = e.getValue();
    //             if (base.getSkipped().contains(c)) {
    //                 assert d == null;
    //                 continue;
    //             }

    //        //     LOGGER.log('monitor ' + c.getName() + ' has data ' + d != null ? "yes" : "no");
    //             if (d == null) {
    //                 // if we failed to monitor, put in the special value that indicates a failure
    //            //     e.setValue(d = new Data(get(c), -1L));
    //             }

    //          /*   if (d.hasTooManyTimeouts() && !isIgnored()) {
    //                 // unlike other monitors whose failure still allow us to communicate with the agent,
    //                 // the failure in this monitor indicates that we are just unable to make any requests
    //                 // to this agent. So we should severe the connection, as opposed to marking it temporarily
    //                 // off line, which still keeps the underlying channel open.
    //                 c.disconnect(d);
    //                 LOGGER.warning(Messages.ResponseTimeMonitor_MarkedOffline(c.getName()));
    //             }*/
    //         }
    //         return monitoringData;
    //     }
  }


  //-----------------------------------------------------------------------------
  private Data lastData = null;

  @Override
  public Object data(Computer c) {

    if (c == null || c.getChannel() == null) {
      return null;
    }
    LOGGER.info("get data " + c.getName());

    Data data = new Data(c, propertyValueGroovy, propertyRangeGroovy, propertyDetailGroovy);
    YesNoMaybe isOk = data.isOk();
    if (YesNoMaybe.MAYBE == isOk) {
      LOGGER.info("get data maybe " + c.getName());
    }
    else if (YesNoMaybe.YES == isOk)
      {
        if (getDescriptor().markOnline(c)) {
          LOGGER.info(c.getName() + "marked online " +  c.getDisplayName());
        }
      } else {
        // todo set cause here
        if (getDescriptor().markOffline(c))  {
          PropertyOfflineCause cause = new PropertyOfflineCause();
          cause.causeText = this.columnName + " out of range";
          LOGGER.info(c.getName() + "marked offline " +  c.getDisplayName());
        }
      }

    return data;
  }

  private static final class PropertyOfflineCause extends MonitorOfflineCause implements Serializable {

        public String causeText;
        /**
         * String rendering of the data
         */
        @Override
        public String toString() {
          return causeText != null ? causeText : "Node property check failed";
        }

        @Override
        public Class<? extends NodeMonitor> getTrigger() {
            return ResponseTimeMonitor.class;
        }

        private static final long serialVersionUID = 1L;
  }

  //-----------------------------------------------------------------------------
  // @Restricted(DoNotUse.class)
  // @ExportedBean(defaultVisibility = 0)
  public static class Data {
    private final Computer comp;
    private final String propertyValueGroovy;
    private final String propertyRangeGroovy;
    private final String propertyDetailGroovy;

    //---------------------------------------------------------------------------
    private Data(Computer c, String propertyValueGroovy, String propertyRangeGroovy, String propertyDetailGroovy) {
      this.comp = c;
      this.propertyValueGroovy = propertyValueGroovy;
      this.propertyRangeGroovy = propertyRangeGroovy;
      this.propertyDetailGroovy = propertyDetailGroovy;
    }

    //---------------------------------------------------------------------------
    public YesNoMaybe isOk() {

      if (this.propertyRangeGroovy == null || this.comp.getChannel() == null) {
        return YesNoMaybe.MAYBE;
      }

      try {
        String str = RemotingDiagnostics.executeGroovy(this.propertyRangeGroovy, this.comp.getChannel());
        return str.equals(String.valueOf(true)) ? YesNoMaybe.YES : YesNoMaybe.NO;
      } catch (Exception e) {
        LOGGER.info("Failed to check property " + this.propertyRangeGroovy + " for node " + comp.getName() + ": " + e.getMessage());
        return YesNoMaybe.MAYBE;
      }
    }

    //---------------------------------------------------------------------------
    public String getValue() {

      if (this.propertyValueGroovy == null || this.comp.getChannel() == null) {
        return null;
      }

      try {
        return RemotingDiagnostics.executeGroovy(this.propertyValueGroovy, this.comp.getChannel());
      } catch (Exception e) {
        LOGGER.info("Failed to get property value " + this.propertyValueGroovy + " for node " + comp.getName() + ": " + e.getMessage());
        return null;
      }
    }

    //---------------------------------------------------------------------------
    public String getValueDetail() {

      if (this.propertyDetailGroovy == null || this.comp.getChannel() == null) {
        return null;
      }

      try {
        return RemotingDiagnostics.executeGroovy(this.propertyDetailGroovy, this.comp.getChannel());
      } catch (Exception e) {
        LOGGER.info("Failed to get property detail " + this.propertyDetailGroovy + " for node " + comp.getName() + ": " + e.getMessage());
        return null;
      }
    }
  }
}