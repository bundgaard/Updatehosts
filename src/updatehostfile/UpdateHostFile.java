/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package updatehostfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author davidb
 */
public class UpdateHostFile {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws MalformedURLException, IOException {
        HttpURLConnection httpConnection = (HttpURLConnection) new URL("http://winhelp2002.mvps.org/hosts.zip").openConnection();
        File tmpDir = File.createTempFile("host_updater", null); // null indicate a .tmp file, location %USERPROFILE%\AppData\Local\Temp

        System.out.println(httpConnection.getResponseCode() + " " + httpConnection.getResponseMessage());
        String content_disposition = httpConnection.getHeaderField("Content-Disposition");
        System.out.println("Content-Disposition: " + content_disposition);
        Map<String, List<String>> m = httpConnection.getHeaderFields();
        Iterator<Entry<String, List<String>>> iterator = m.entrySet().iterator();
        while (iterator.hasNext()) {
            //System.out.println(iterator.next().getKey() + ": ")
            Entry<String, List<String>> e = iterator.next();
            for (String s : e.getValue()) {
                System.out.println((e.getKey() == null ? " " : e.getKey()) + ": " + s);

            }
        }
        byte[] fileByte = new byte[4096];
        int bytesRead = -1;
        InputStream readFromURL = httpConnection.getInputStream();
        FileOutputStream writeToDisk = new FileOutputStream(tmpDir);
        while ((bytesRead = readFromURL.read(fileByte)) != -1) {
            writeToDisk.write(fileByte, 0, bytesRead);

        }

        writeToDisk.close();
        readFromURL.close();

        ZipInputStream zippedFile = new ZipInputStream(new FileInputStream(tmpDir));
        ZipEntry zipEntry = zippedFile.getNextEntry();
        System.out.printf("%-20s %40s\n", "Filename", "Comment");
        // Date todayDate = Date.
//        ZoneId.getAvailableZoneIds().forEach((s) -> {
//            if ( s.matches("Europe/\\w+")) {
//                System.out.println(s);
//            }
//        });
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Stockholm"));
        Date todayDate = cal.getTime();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        // AIX expire format MMDDhhmmyy
        SimpleDateFormat aix_dateformat = new SimpleDateFormat("MMddhhmmYY");
        SimpleDateFormat httpd_dateformat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        
        //cal.add(Calendar.DAY_OF_YEAR, 2);
        System.out.println(httpd_dateformat.format(cal.getTime()));
        File host_file;
        while (zipEntry != null) {
            String filename = zipEntry.getName();
            String comment = zipEntry.getComment();

            System.out.printf("%-20s %40s\n", filename, comment == null ? " " : comment);
            File dateDirectory = new File(tmpDir.getParent() + File.separator + "");
            FileOutputStream realFileWriter = new FileOutputStream(new File(tmpDir.getParentFile() + File.separator + filename));

            int zippedFileLength = 0;
            byte[] zippedByte = new byte[4096];
            while ((zippedFileLength = zippedFile.read(zippedByte)) != -1) {
                realFileWriter.write(zippedByte, 0, zippedFileLength);
            }
            realFileWriter.close();
            Matcher matc = Pattern.compile("hosts",Pattern.CASE_INSENSITIVE).matcher(filename);
            if (matc.matches() ) {
                System.out.println("Found match: " +matc.group());
            }
            zipEntry = zippedFile.getNextEntry();
        }
        
        

        zippedFile.closeEntry();
        zippedFile.close();

        httpConnection.disconnect();
        // TODO code application logic here
    }

}
