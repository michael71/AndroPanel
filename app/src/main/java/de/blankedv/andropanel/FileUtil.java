package de.blankedv.andropanel;

import static de.blankedv.andropanel.AndroPanelApplication.DEBUG;
import static de.blankedv.andropanel.AndroPanelApplication.DEMO_LOCOS_FILE;
import static de.blankedv.andropanel.AndroPanelApplication.DIRECTORY;
import static de.blankedv.andropanel.AndroPanelApplication.INVALID_INT;
import static de.blankedv.andropanel.AndroPanelApplication.TAG;
import static de.blankedv.andropanel.AndroPanelApplication.configFilename;
import static de.blankedv.andropanel.AndroPanelApplication.configHasChanged;
import static de.blankedv.andropanel.AndroPanelApplication.panelElements;
import static de.blankedv.andropanel.AndroPanelApplication.panelName;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.xmlpull.v1.XmlSerializer;

import android.os.Environment;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

/**
 * FileUtil methods. 
 * 
 * @author ccollins
 *
 */
public final class FileUtil {

   // Object for intrinsic lock (per docs 0 length array "lighter" than a normal Object)
   public static final Object[] DATA_LOCK = new Object[0];

   private FileUtil() {
   }

   /**
    * Use Environment to check if external storage is writable.
    * 
    * @return
    */
   public static boolean isExternalStorageWritable() {
      return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
   }

   /**
    * Use environment to check if external storage is readable.
    * 
    * @return
    */
   public static boolean isExternalStorageReadable() {
      if (isExternalStorageWritable()) {
         return true;
      }
      return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
   }
   


}
