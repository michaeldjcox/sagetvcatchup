package uk.co.mdjcox.sagetv.catchup;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * Created by michael on 24/10/14.
 */
public interface CatchupServerRemote extends Remote {

  boolean available() throws RemoteException;

  Map<String, String> getStatus() throws RemoteException;

  String startCataloging() throws RemoteException;

  String stopCataloging() throws RemoteException;

  String stopAllRecording() throws RemoteException;

  String shutdown() throws RemoteException;

  void setProperty(String name, String value) throws RemoteException;
}
