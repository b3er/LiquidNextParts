package com.suxsem.liquidnextparts.components;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.suxsem.liquidnextparts.LiquidSettings;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

public class DownloadTask extends AsyncTask<String, Integer, Drawable>
{

    private Drawable d;
    private HttpURLConnection conn;
    private InputStream stream; //to read
    private ByteArrayOutputStream out; //to write

    private double fileSize;
    private double downloaded; // number of bytes downloaded
    private int status = DOWNLOADING; //status of current process

    private static final int MAX_BUFFER_SIZE = 1000000; //1kb
    private static final int DOWNLOADING = 0;
    private static final int COMPLETE = 1;

    private String filelocation = "";
    private String gorecovery = "";
    private Integer previousperc = 0;
    private NotificationHelper mNotificationHelper;
    public DownloadTask(Context context){
        mNotificationHelper = new NotificationHelper(context);
    }
    
    public void DownloadManager()
    {
        d          = null;
        conn       = null;
        fileSize   = 0;
        downloaded = 0;
        status     = DOWNLOADING;
    }


    @Override
    protected Drawable doInBackground(String... url)
    {
        try
        {
            String[] DownloadTaskInformations = url[1].split("#");
        	filelocation = DownloadTaskInformations[0];
        	gorecovery = DownloadTaskInformations[1];
            String filename = filelocation;
            {
            	
                conn     = (HttpURLConnection) new URL(url[0]).openConnection();
                fileSize = conn.getContentLength();
                FileOutputStream fos = new FileOutputStream(filename);
                conn.connect();

                stream = conn.getInputStream();
                // loop with step
                while (status == DOWNLOADING)
                {
                    byte buffer[];
                    if (fileSize - downloaded > MAX_BUFFER_SIZE)
                    {
                        buffer = new byte[MAX_BUFFER_SIZE];
                        out      = new ByteArrayOutputStream(MAX_BUFFER_SIZE);
                    }
                    else
                    {
                        buffer = new byte[(int) (fileSize - downloaded)];
                        out      = new ByteArrayOutputStream((int) (fileSize - downloaded));
                    }
                    int read = stream.read(buffer);

                    if (read == -1)
                    {
                        publishProgress(100);
                        break;
                    }
                    // writing to buffer
                    out.write(buffer, 0, read);
                    fos.write(out.toByteArray());
                    downloaded += read;
                    // update progress bar
                    
                    try {
						if (previousperc == (int) ((downloaded / fileSize) * 100)){
						}else{
						previousperc = (int) ((downloaded / fileSize) * 100);
						publishProgress();
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                } // end of while

                if (status == DOWNLOADING)
                {
                    status = COMPLETE;
                }
                try
                {                    
                    fos.close();
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                    return null;
                }

                d = Drawable.createFromStream((InputStream) new ByteArrayInputStream(out.toByteArray()), "filename");
                return d;
            } // end of if isOnline            
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }// end of catch
    } // end of class DownloadManager()

    @Override
    protected void onProgressUpdate(Integer... progress)
    {
    	
    	mNotificationHelper.progressUpdate(previousperc);
  
    }

    @Override
    protected void onPreExecute()
    {
    	mNotificationHelper.createNotification();
    }

    @Override
    protected void onPostExecute(Drawable result)
    {
    	mNotificationHelper.completed();
    	if(gorecovery.equals("r")){
    		LiquidSettings.runRootCommand("reboot recovery");
    	}
        // do something
    }
    @Override
    protected void onCancelled() {
    }

}

