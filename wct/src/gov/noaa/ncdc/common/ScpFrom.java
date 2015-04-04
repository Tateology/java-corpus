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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class ScpFrom {
   
   
   
   
   
  public static void main(String[] arg){
    if(arg.length!=2){
      System.err.println("usage: java ScpFrom user@remotehost:file1 file2");
      System.exit(-1);
    }
    
    String user=arg[0].substring(0, arg[0].indexOf('@'));
    arg[0]=arg[0].substring(arg[0].indexOf('@')+1);
    String host=arg[0].substring(0, arg[0].indexOf(':'));
    String rfile=arg[0].substring(arg[0].indexOf(':')+1);
    String lfile=arg[1];
    
    ScpFrom scp = new ScpFrom();
    try {
       scp.getFile(user, host, rfile, lfile);
    } catch (Exception e) {
       e.printStackTrace();
    }
    
  }
  
  
  public Session getSession(String user, String host) throws JSchException {
  
      JSch jsch=new JSch();
      Session session=jsch.getSession(user, host, 22);

      // username and password will be given via UserInfo interface.
      UserInfo ui=new MyUserInfo();
      session.setUserInfo(ui);
      session.connect();
      
      return session; 
  }
  
  
  public void getFile(String user, String host, String rfile, String lfile) throws JSchException {

     Session session = getSession(user, host);
     getFile(session, rfile, lfile);
     
  }
  
  public void getFile(Session session, String rfile, String lfile) throws JSchException {
     
     
    FileOutputStream fos=null;
    try {

      String prefix=null;
      if(new File(lfile).isDirectory()){
        prefix=lfile+File.separator;
      }

      // exec 'scp -f rfile' remotely
      String command="scp -f "+rfile;
      Channel channel=session.openChannel("exec");
      ((ChannelExec)channel).setCommand(command);

      // get I/O streams for remote scp
      OutputStream out=channel.getOutputStream();
      InputStream in=channel.getInputStream();

      channel.connect();

      byte[] buf=new byte[1024];

      // send '\0'
      buf[0]=0; out.write(buf, 0, 1); out.flush();

      while(true){
	int c=checkAck(in);
        if(c!='C'){
	  break;
	}

        // read '0644 '
        in.read(buf, 0, 5);

        long filesize=0L;
        while(true){
          if(in.read(buf, 0, 1)<0){
            // error
            break; 
          }
          if(buf[0]==' ')break;
          filesize=filesize*10L+(long)(buf[0]-'0');
        }

        String file=null;
        for(int i=0;;i++){
          in.read(buf, i, 1);
          if(buf[i]==(byte)0x0a){
            file=new String(buf, 0, i);
            break;
  	  }
        }

	//System.out.println("filesize="+filesize+", file="+file);

        // send '\0'
        buf[0]=0; out.write(buf, 0, 1); out.flush();

        // read a content of lfile
        fos=new FileOutputStream(prefix==null ? lfile : prefix+file);
        int foo;
        while(true){
          if(buf.length<filesize) foo=buf.length;
	  else foo=(int)filesize;
          foo=in.read(buf, 0, foo);
          if(foo<0){
            // error 
            break;
          }
          fos.write(buf, 0, foo);
          filesize-=foo;
          if(filesize==0L) break;
        }
        fos.close();
        fos=null;

        byte[] tmp=new byte[1];

	if(checkAck(in)!=0){
	  //System.exit(0);
     throw new JSchException("checkAck Error");
	}

        // send '\0'
        buf[0]=0; out.write(buf, 0, 1); out.flush();
      }

      //session.disconnect();

      //System.exit(0);      
      return;
      
    }
    catch(Exception e){
      System.out.println(e);
      try{if(fos!=null)fos.close();}catch(Exception ee){}
      throw new JSchException("Scp Error");
    }
  }

  static int checkAck(InputStream in) throws IOException{
    int b=in.read();
    // b may be 0 for success,
    //          1 for error,
    //          2 for fatal error,
    //          -1
    if(b==0) return b;
    if(b==-1) return b;

    if(b==1 || b==2){
      StringBuffer sb=new StringBuffer();
      int c;
      do {
	c=in.read();
	sb.append((char)c);
      }
      while(c!='\n');
      if(b==1){ // error
	System.out.print(sb.toString());
      }
      if(b==2){ // fatal error
	System.out.print(sb.toString());
      }
    }
    return b;
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
      else{ return false; }
    }
    public void showMessage(String message){
      JOptionPane.showMessageDialog(null, message);
    }
  }

}

