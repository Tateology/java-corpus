/**
 * NOAA's National Climatic Data Center
 * NOAA/NESDIS/NCDC
 * 151 Patton Ave, Asheville, NC  28801
 * 
 * THIS SOFTWARE AND ITS DOCUMENTATION ARE CONSIDERED TO BE IN THE 
 * PUBLIC DOMAIN AND THUS ARE AVAILABLE FOR UNRESTRICTED PUBLIC USE.  
 * THEY ARE FURNISHED "AS IS." THE AUTHORS, THE UNITED STATES GOVERNMENT, ITS
 * INSTRUMENTALITIES, OFFICERS, EMPLOYEES, AND AGENTS MAKE NO WARRANTY,
 * EXPRESS OR IMPLIED, AS TO THE USEFULNESS OF THE SOFTWARE AND
 * DOCUMENTATION FOR ANY PURPOSE. THEY ASSUME NO RESPONSIBILITY (1)
 * FOR THE USE OF THE SOFTWARE AND DOCUMENTATION; OR (2) TO PROVIDE
 * TECHNICAL SUPPORT TO USERS.
 */

package gov.noaa.ncdc.common;


import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;


public class SSHDirectory {
   
   
  public static void main(String[] arg) {
     
     try {
        SSHDirectory dir = new SSHDirectory();
        String[] filenames = dir.getFileNames("sansari", "doppler", "/doppler/sansari");
        
        System.out.println("-----------");
        for (int n=0; n<filenames.length; n++) {
           System.out.println("filename["+n+"]  "+filenames[n]);
        }
        System.out.println("-----------");
     } catch (Exception e) {
        e.printStackTrace();
     }
     
  }

  public Session getSession(String user, String host) throws JSchException {

      JSch jsch=new JSch();  
            
      //String host=JOptionPane.showInputDialog("Enter username@hostname",
      //                                        System.getProperty("user.name")+
      //                                        "@localhost"); 
      //String user=host.substring(0, host.indexOf('@'));
      //host=host.substring(host.indexOf('@')+1);

      Session session=jsch.getSession(user, host, 22);
      
      /*
      String xhost="127.0.0.1";
      int xport=0;
      String display=JOptionPane.showInputDialog("Enter display name", 
                                                 xhost+":"+xport);
      xhost=display.substring(0, display.indexOf(':'));
      xport=Integer.parseInt(display.substring(display.indexOf(':')+1));
      session.setX11Host(xhost);
      session.setX11Port(xport+6000);
      */

      // username and password will be given via UserInfo interface.
      //if (password == null) {
         UserInfo ui=new MyUserInfo();
         session.setUserInfo(ui);
      //}
      //else {
      //   session.setPassword(password);
      //}
      
      session.connect();
      
      return session;
  }

  
  public String[] getFileNames(String user, String host, String directory) 
      throws IOException, JSchException {
  
      Session session = getSession(user, host);
      return getFileNames(session, directory);
      
  }
  
  
  
  public String[] getFileNames(Session session, String directory) 
      throws IOException, JSchException {
         
      //String command=JOptionPane.showInputDialog("Enter command", 
      //                                           "set|grep SSH");
      
      String command = "ls -p "+directory;

      Channel channel=session.openChannel("exec");
      ((ChannelExec)channel).setCommand(command);
      //channel.setXForwarding(true);

      //channel.setInputStream(System.in);
      channel.setInputStream(null);

      //channel.setOutputStream(System.out);

      //FileOutputStream fos=new FileOutputStream("/tmp/stderr");
      //((ChannelExec)channel).setErrStream(fos);
      ((ChannelExec)channel).setErrStream(System.err);

      InputStream in=channel.getInputStream();

      channel.connect();

      StringBuffer sb = new StringBuffer();
      
      
      byte[] tmp=new byte[1024];
      while(true){
        while(in.available()>0){
          int i=in.read(tmp, 0, 1024);
          if(i<0)break;
          //System.out.print(new String(tmp, 0, i));
          sb.append(new String(tmp, 0, i));
        }
        if(channel.isClosed()){
          System.out.println("exit-status: "+channel.getExitStatus());
          break;
        }
        try{Thread.sleep(1000);}catch(Exception ee){}
      }
      channel.disconnect();
      //session.disconnect();
      
      
      String[] filenames = sb.toString().split("\n");
      // exclude directories
      Vector plainFiles = new Vector();
      for (int n=0; n<filenames.length; n++) {
         filenames[n] = filenames[n].trim();
         if (! filenames[n].endsWith("/")) {
            plainFiles.add(filenames[n]);
         }
      }
      
      String[] returnArray = new String[plainFiles.size()];
      returnArray = (String[])(plainFiles.toArray(returnArray));
      
      return returnArray;
      
  }
  
  
  
  

  public static class MyUserInfo implements UserInfo{
    public String getPassword(){ return passwd; }
    public boolean promptYesNo(String str){
      Object[] options={ "yes", "no" };
      int foo=JOptionPane.showOptionDialog(null, 
             str,
             "Warning", 
             JOptionPane.DEFAULT_OPTION, 
             JOptionPane.WARNING_MESSAGE,
             null, options, options[0]);
       return foo==0;
    }
  
    String passwd;
    JTextField passwordField=(JTextField)new JPasswordField(20);

    public String getPassphrase(){ return null; }
    public boolean promptPassphrase(String message){ return true; }
    public boolean promptPassword(String message){
      Object[] ob={passwordField}; 
      int result=
        JOptionPane.showConfirmDialog(null, ob, message,
                                      JOptionPane.OK_CANCEL_OPTION);
      if(result==JOptionPane.OK_OPTION){
        passwd=passwordField.getText();
        return true;
      }
      else{ 
        return false; 
      }
    }
    public void showMessage(String message){
      JOptionPane.showMessageDialog(null, message);
    }
  }
  
  
}

