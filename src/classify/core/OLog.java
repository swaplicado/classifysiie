/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classify.core;

import clasifysiie.ClassifySiie;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Edwin Carmona
 */
public class OLog {
    
    /**
     *  
     * 
     * @param fileName
     * @param recEty
     * @param op ClassifySiie.INSERT, ClassifySiie.UPDATE
     * @param status
     * @return 
     */
    public static String writeFile(String fileName, ORecEty recEty, int op, String status) 
    { 
         final String NEW_LINE_SEPARATOR = "\n";
        DateFormat dft = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        
        FileWriter fileWriter = null;
        
        try {
            String fName = fileName != null ? fileName : "logs/log" + "__" + dft.format(new Date()) + ".csv";
            fileWriter = new FileWriter(fName, true);
//            CSVWriter mCsvWriter = new CSVWriter(fileWriter);

            if (fileName == null) {
                String header = 
                                "id_year," + 
                                "id_per," + 
                                "id_bkc," + 
                                "id_tp_rec," + 
                                "id_num," + 
                                "id_ety," + 
                                "debit," + 
                                "credit," + 
                                "debit_cur," + 
                                "credit_cur," + 
                                "fid_tax_bas_n," + 
                                "fid_tax_n," + 
                                "fid_acc," + 
                                "fk_acc," + 
                                "operacion," + 
                                "status";
                //Write the CSV file header
                fileWriter.append(header);
            }
            else {
                if (recEty != null) {
                    String line = 
                        recEty.id_year + "," +
                        recEty.id_per + "," +
                        recEty.id_bkc + "," +
                        recEty.id_tp_rec + "," +
                        recEty.id_num + "," +
                        recEty.id_ety + "," +
                        recEty.debit + "," +
                        recEty.credit + "," +
                        recEty.debit_cur + "," +
                        recEty.credit_cur + "," +
                        recEty.fid_tax_bas_n + "," +
                        recEty.fid_tax_n + "," +
                        recEty.fid_acc + "," +
                        recEty.fk_acc + "," +
                        (op == ClassifySiie.INSERT ? "INSERT" : "DELETE") + "," +
                        "" + status + "";
                    
                    fileWriter.append(line);
                }
            }
            
            //Add a new line separator after the header
            fileWriter.append(NEW_LINE_SEPARATOR);

            fileWriter.flush();
            fileWriter.close();
            
            return fName;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    } 
}
