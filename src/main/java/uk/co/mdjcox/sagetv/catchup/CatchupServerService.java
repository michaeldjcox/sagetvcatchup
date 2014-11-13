package uk.co.mdjcox.sagetv.catchup;

import uk.co.mdjcox.sagetv.catchup.server.Server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by michael on 24/10/14.
 */
public class CatchupServerService extends UnicastRemoteObject implements CatchupServerRemote {

  private final CatchupContextInterface context;
  private Cataloger cataloger;
  private Recorder recorder;

  public CatchupServerService(Cataloger cataloger, Recorder recorder, CatchupContextInterface context) throws RemoteException {
    this.cataloger = cataloger;
    this.recorder = recorder;
    this.context = context;
  }

  @Override
  public Map<String, String> getStatus() throws RemoteException {
    Map<String,String> statii = new HashMap<String, String>();
    statii.put("Catalog Progress", cataloger.getProgress());
    statii.put("Recording Progress", String.valueOf(recorder.getRecordingCount()));
    statii.put("Recording Processes", String.valueOf(recorder.getProcessCount()));
    return statii;
  }

  @Override
  public String startCataloging() throws RemoteException {
    return cataloger.startCataloging();
  }

  @Override
  public String stopCataloging() throws RemoteException {
    return cataloger.stopCataloging();
  }

  @Override
  public String stopAllRecording() throws RemoteException {
    return recorder.requestStopAll();
  }

  @Override
  public String shutdown() throws RemoteException {
    Thread shutdown = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          // Ignore
        } finally {
          System.exit(1);
        }
      }
    });
    return "Shutting down";
  }

  @Override
  public boolean available() throws RemoteException {
    return true;
  }

    @Override
    public void setProperty(String name, String value) throws RemoteException {
        context.setProperty(name, value);
    }
}
