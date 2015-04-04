package gov.noaa.ncdc.common;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class StreamGobbler extends Thread
{
    InputStream is;
    String type;
    
    StreamGobbler(InputStream is, String type)
    {
        this.is = is;
        this.type = type;
    }
    
    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null)
                System.out.println(type + ">" + line);    
            } catch (IOException ioe)
              {
                ioe.printStackTrace();  
              }
    }
}


public class GoodWindowsExec
{
    public static final void exec(String args[])
    {
        if (args.length < 1)
        {
            System.out.println("USAGE: java GoodWindowsExec <cmd>");
            System.exit(1);
        }
        
        try
        {            
            String osName = System.getProperty("os.name" );
            String[] cmd = new String[3];

            Runtime rt = Runtime.getRuntime();
            Process proc;
            if( osName.equals( "Windows NT" ) )
            {
                cmd[0] = "cmd.exe" ;
                cmd[1] = "/C" ;
                cmd[2] = args[0];

                System.out.println("Execing " + cmd[0] + " " + cmd[1] + " " + cmd[2]);
                proc = rt.exec(cmd);
            }
            else if( osName.equals( "Windows 95" ) )
            {
                cmd[0] = "command.com" ;
                cmd[1] = "/C" ;
                cmd[2] = args[0];

                System.out.println("Execing " + cmd[0] + " " + cmd[1] + " " + cmd[2]);
                proc = rt.exec(cmd);
            }
            else 
            {            
                System.out.println("Execing " + args[0]);
                proc = rt.exec(args[0]);
            }
            // any error message?
            StreamGobbler errorGobbler = new
                StreamGobbler(proc.getErrorStream(), "ERROR");            
            
            // any output?
            StreamGobbler outputGobbler = new
                StreamGobbler(proc.getInputStream(), "OUTPUT");
                
            // kick them off
            errorGobbler.start();
            outputGobbler.start();
                                    
            // any error???
            int exitVal = proc.waitFor();
            System.out.println("ExitValue: " + exitVal);        
        } catch (Throwable t)
          {
            t.printStackTrace();
          }
    }
}
