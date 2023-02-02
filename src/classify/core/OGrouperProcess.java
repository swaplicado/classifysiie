/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classify.core;

import classifysiie.db.DbMySqlConnection;
import classifysiie.db.OConfig;
import classifysiie.db.OConfigReader;
import classifysiie.db.ODocumentsGrouperManager;
import java.util.ArrayList;

/**
 *
 * @author Edwin Carmona
 */
public class OGrouperProcess {
    
    public void groupEtys(final int iYear) {
        OConfigReader cfgReader = new OConfigReader();
        OConfig cfg = cfgReader.readConfig();
        DbMySqlConnection c = new DbMySqlConnection(cfg.getSiieConnection().getNameDb(), 
                                                    cfg.getSiieConnection().getHostDb(), 
                                                    cfg.getSiieConnection().getPortDb(), 
                                                    cfg.getSiieConnection().getUserDb(), 
                                                    cfg.getSiieConnection().getPswdDb());
        
        ArrayList<ORecEty> lEtyGroups = ODocumentsGrouperManager.getRecEtyGroupers(c.connectMySQL(), iYear, cfg.getCtaCustToSearch(), cfg.getCtaSupToSearch());
        String fileName = OLog.writeFile(null, null, 0, null);
        fileName = fileName.replace(".csv", "_grouper.csv");
        
        int counter = 0;
        int p100 = lEtyGroups.size();
        for (ORecEty oEty : lEtyGroups) {
            OCoreGrouper.groupDocumentEtys(c, oEty, fileName);
            counter++;
            System.out.println(counter * 100 / p100 + "%");
        }
    }
}
