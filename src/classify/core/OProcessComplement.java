/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classify.core;

import classifysiie.db.DbMySqlConnection;
import classifysiie.db.OConfig;
import classifysiie.db.OConfigReader;
import classifysiie.db.OProcessDocuments;
import erp.mfin.data.SFinAccountConfigEntry;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Edwin Carmona
 */
public class OProcessComplement {
    DbMySqlConnection c;
    String fileName;
    HashMap<String, Integer> accs;
    
    /**
     * Lee la configuración y se conecta con la base de datos para iniciar con el proceso
     * 
     * @throws SQLException 
     */
    public OProcessComplement() throws SQLException {
        OConfigReader cfgReader = new OConfigReader();
        OConfig cfg = cfgReader.readConfig(); 
        c = new DbMySqlConnection(cfg.getSiieConnection().getNameDb(), cfg.getSiieConnection().getHostDb(), cfg.getSiieConnection().getPortDb(), cfg.getSiieConnection().getUserDb(), cfg.getSiieConnection().getPswdDb());
        fileName = OLog.writeFile(null, null, 0, null);
        accs = OProcessDocuments.getAccs(c.connectMySQL());
    }
    
    
    /**
     * Recibe el año a procesar y consulta los renglones que no tengan un documento y estén relacionados con
     * un asociado de negocios, para después reclasificarlos a la cuenta default
     * 
     * @param year
     * @param ctaMCus
     * @param ctaMSup
     * @throws SQLException
     * @throws CloneNotSupportedException 
     */
    public void processComplement(int year, String ctaMCus, String ctaMSup) throws SQLException, CloneNotSupportedException {
        
        String sql = "SELECT " +
                        "    * " +
                        " FROM " +
                        "    fin_rec_ety re " +
                        "        INNER JOIN " +
                        "    fin_rec r ON re.id_year = r.id_year " +
                        "        AND re.id_per = r.id_per " +
                        "        AND re.id_bkc = r.id_bkc " +
                        "        AND re.id_tp_rec = r.id_tp_rec " +
                        "        AND re.id_num = r.id_num " +
                        "WHERE " +
                        "    NOT r.b_del AND NOT re.b_del AND fid_dps_doc_n IS NULL " +
                        "        AND re.id_year = " + year + " " +
                        "        AND re.fid_bp_nr > 0 AND (re.fid_acc = '" + ctaMCus + "' " +
                        "        OR re.fid_acc = '" + ctaMSup + "') " +
                        "ORDER BY r.dt ASC , re.id_per ASC , re.id_bkc ASC , re.id_tp_rec ASC , re.id_num ASC , re.id_ety ASC";
        
        ArrayList<ORecEty> recEtys = new ArrayList();
        
        Statement st = c.connectMySQL().createStatement();
        ResultSet res = st.executeQuery(sql);

        while (res.next()) {
            ORecEty recEty = OProcessDocuments.getRecEty(c.connectMySQL(), res.getInt("id_year"), res.getInt("id_per"), res.getInt("id_bkc"), res.getString("id_tp_rec"), res.getInt("id_num"), res.getInt("id_ety"));
            recEtys.add(recEty);
        }
        
        String ctaCustomer = "1120-0009-0000";
        String ctaSupplier = "2105-0009-0000";
        
        for (ORecEty recEty : recEtys) {
            int[] pkTax = new int[] { 0, 0 };
            // consultar la configuración de la cuenta contable correspondiente al impuesto del monto
            
            String cta = "";
            if (recEty.fid_acc.equals(ctaMCus)) {
                cta = ctaCustomer;
            }
            else {
                cta = ctaSupplier;
            }
            
            SFinAccountConfigEntry accCfg = new SFinAccountConfigEntry(cta, recEty.fid_cc_n, 1, 0, 0);
            
            this.configureEty(recEty, accCfg);
        }
    }
    
    
    /**
     * Reclasifica el renglón de póliza borrando el anterior y dando de alta uno nuevo con la nueva configuración
     * 
     * @param recEty
     * @param accCfg
     * @throws CloneNotSupportedException
     * @throws SQLException 
     */
    private void configureEty(ORecEty recEty, SFinAccountConfigEntry accCfg) throws CloneNotSupportedException, SQLException {
        ORecEty recEtyN = (ORecEty) recEty.clone();
        
        recEtyN.fk_acc = accs.get(accCfg.getAccountId());
        recEtyN.fid_acc = accCfg.getAccountId();
        recEtyN.fid_cc_n = accCfg.getCostCenterId();
        recEtyN.fid_tax_bas_n = accCfg.getBasicTax();
        recEtyN.fid_tax_n = accCfg.getTax();
        
        int max = OProcessDocuments.getMaxRecs(c.connectMySQL(), recEty);
        recEtyN.id_ety = ++max;
        OProcessDocuments.insertRecEty(c.connectMySQL(), recEtyN, fileName);
        
        recEty.b_del = true;
        OProcessDocuments.deleteRecEty(c.connectMySQL(), recEty, fileName);
    }
}
