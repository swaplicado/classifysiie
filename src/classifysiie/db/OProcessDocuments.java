/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classifysiie.db;

import clasifysiie.ClassifySiie;
import classify.core.ODpsEty;
import classify.core.OEtyTax;
import classify.core.OFinRec;
import classify.core.OLog;
import classify.core.ORecEty;
import classify.core.OTrnDps;
import erp.mfin.data.SFinAccountConfigEntry;
import erp.mfin.data.SFinAccountUtilities;
import erp.mod.SModSysConsts;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Edwin Carmona
 */
public class OProcessDocuments {

    /**
     * Lee los documentos a clasificar partiendo de un año recibido
     * 
     * @param conn
     * @param year
     * @return 
     */
    public static ArrayList<OTrnDps> getDocuments(Connection conn, int year) {
        
        /**
         * Obtención de documentos de póliza
         */
        
        String recDocsSql = "SELECT " +
                        "   DISTINCT CONCAT_WS('_', fid_dps_year_n, fid_dps_doc_n) AS doc_key " +
                        "FROM " +
                        "    fin_rec_ety " +
                        "WHERE " +
                        "    id_year = " + year + 
                        "        AND fid_dps_doc_n > 0" +
//                        "        AND fid_dps_doc_n = 13168 " +
                        "        AND fid_dps_adj_doc_n IS NULL" +
                        "        AND NOT b_del;";
        
        StringBuffer docs = new StringBuffer("");
        
        try {
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(recDocsSql);

            while (res.next()) {
                docs.append("'");
                docs.append(res.getString("doc_key"));
                docs.append("'");
                docs.append(",");
            }
        }
        catch (SQLException ex) {

        }
        
        /**
         * Obtención de documentos de ajuste de póliza
         */
        
        String recAdjSql = "SELECT " +
                        "   DISTINCT CONCAT_WS('_', fid_dps_adj_year_n, fid_dps_adj_doc_n) AS doc_key " +
                        "FROM " +
                        "    fin_rec_ety " +
                        "WHERE " +
                        "    id_year = " + year + " " +
//                        "    id_year = 2018 AND fid_dps_adj_doc_n = 1037 " +
                        "        AND fid_dps_adj_doc_n > 0 " +
                        "        AND NOT b_del;";
        
        try {
            Statement st1 = conn.createStatement();
            ResultSet res1 = st1.executeQuery(recAdjSql);

            while (res1.next()) {
                docs.append("'");
                docs.append(res1.getString("doc_key"));
                docs.append("'");
                docs.append(",");
            }
            
            if (docs.length() > 1) {
                docs.deleteCharAt(docs.length() - 1);
            }
        }
        catch (SQLException ex) {

        }

        String sql = "SELECT " +
                "    (SELECT " +
                "            COUNT(DISTINCT id_tax) " +
                "        FROM " +
                "            trn_dps_ety_tax etytax " +
                "        WHERE " +
                "            fid_tp_tax = " + SModSysConsts.FINS_TP_TAX_CHARGED +
                "                AND td.id_year = etytax.id_year " +
                "                AND td.id_doc = etytax.id_doc) AS taxes, " +
                "(SELECT " +
                "            COUNT(*) " +
                "        FROM " +
                "            trn_dps_ety ety " +
                "        WHERE " +
                "            ety.b_del = FALSE " +
                "                AND td.id_year = ety.id_year " +
                "                AND td.id_doc = ety.id_doc) AS etis, " +
                "    td.* " +
                "FROM " +
                "    trn_dps td " +
                "WHERE " +
                "    CONCAT_WS('_', td.id_year, td.id_doc) IN (" + docs.toString() + ");  ";
//                "    CONCAT_WS('_', td.id_year, td.id_doc) IN ('2014_2761'," +
//"'2014_2848'," +
//"'2023_11731'," +
//"'2023_11732'," +
//"'2023_11736'," +
//"'2023_11742'," +
//"'2023_11744'," +
//"'2023_11778'," +
//"'2023_11782'," +
//"'2023_11785'," +
//"'2023_11786'," +
//"'2023_11787'," +
//"'2023_11791'," +
//"'2023_11805'," +
//"'2023_11859'," +
//"'2023_11895'," +
//"'2023_11899'," +
//"'2023_11900'," +
//"'2023_11902'," +
//"'2023_11904'," +
//"'2023_11910'," +
//"'2023_11932'," +
//"'2023_11934'," +
//"'2023_11936'," +
//"'2023_11966'," +
//"'2023_11971'," +
//"'2023_11972'," +
//"'2023_11978'," +
//"'2023_12031'," +
//"'2023_12034'," +
//"'2023_12035'," +
//"'2023_12037'," +
//"'2023_12038'," +
//"'2023_12039'," +
//"'2023_12040'," +
//"'2023_12041'," +
//"'2023_12042'," +
//"'2023_12043'," +
//"'2023_12044'," +
//"'2023_12045'," +
//"'2023_12047'," +
//"'2023_12048'," +
//"'2023_12050'," +
//"'2023_12051'," +
//"'2023_12052'," +
//"'2023_12053'," +
//"'2023_12054'," +
//"'2023_12055'," +
//"'2023_12072'," +
//"'2023_12073'," +
//"'2023_12075'," +
//"'2023_12081'," +
//"'2023_12082'," +
//"'2023_12083'," +
//"'2023_12084'," +
//"'2023_12085'," +
//"'2023_12086'," +
//"'2023_12087'," +
//"'2023_12088'," +
//"'2023_12089'," +
//"'2023_12090'," +
//"'2023_12205'," +
//"'2023_12208'," +
//"'2023_12209'," +
//"'2023_12210'," +
//"'2023_12249'," +
//"'2023_12252'," +
//"'2023_12255'," +
//"'2023_12333'," +
//"'2023_12335'," +
//"'2023_12347'," +
//"'2023_12362'," +
//"'2023_12395'," +
//"'2023_12425'," +
//"'2023_12426'," +
//"'2023_12427'," +
//"'2023_12428'," +
//"'2023_12437'," +
//"'2023_12460'," +
//"'2023_12498'," +
//"'2023_12503'," +
//"'2023_12507'," +
//"'2023_12598'," +
//"'2023_12609'," +
//"'2023_12610'," +
//"'2023_12611'," +
//"'2023_12612'," +
//"'2023_12614'," +
//"'2023_12615'," +
//"'2023_12616'," +
//"'2023_12617'," +
//"'2023_12622'," +
//"'2023_12630'," +
//"'2023_12631'," +
//"'2023_12632'," +
//"'2023_12633'," +
//"'2023_12649'," +
//"'2023_12650'," +
//"'2023_12654'," +
//"'2023_12690'," +
//"'2023_12693'," +
//"'2023_12694'," +
//"'2023_12695'," +
//"'2023_12696'," +
//"'2023_12697'," +
//"'2023_12699'," +
//"'2023_12700'," +
//"'2023_12701'," +
//"'2023_12705'," +
//"'2023_12707'," +
//"'2023_12708'," +
//"'2023_12714'," +
//"'2023_12717'," +
//"'2023_12720'," +
//"'2023_12721'," +
//"'2023_12725'," +
//"'2023_12726'," +
//"'2023_12727'," +
//"'2023_12754'," +
//"'2023_12794'," +
//"'2023_12798'," +
//"'2023_12800'," +
//"'2023_12801'," +
//"'2023_12805'," +
//"'2023_12806'," +
//"'2023_12816'," +
//"'2023_12847'," +
//"'2023_12889'," +
//"'2023_12890'," +
//"'2023_12891'," +
//"'2023_12892'," +
//"'2023_12893'," +
//"'2023_12953'," +
//"'2023_12954'," +
//"'2023_12959'," +
//"'2023_12976'," +
//"'2023_12977'," +
//"'2023_12979'," +
//"'2023_12985'," +
//"'2023_12986'," +
//"'2023_12987'," +
//"'2023_12988'," +
//"'2023_12989'," +
//"'2023_12994'," +
//"'2023_12999'," +
//"'2023_13000'," +
//"'2023_13001'," +
//"'2023_13007'," +
//"'2023_13026'," +
//"'2023_13039'," +
//"'2023_13044'," +
//"'2023_13050'," +
//"'2023_13051'," +
//"'2023_13052'," +
//"'2023_13053'," +
//"'2023_13058'," +
//"'2023_13101'," +
//"'2023_13102'," +
//"'2023_13103'," +
//"'2023_13104'," +
//"'2023_13105'," +
//"'2023_13106'," +
//"'2023_13107'," +
//"'2023_13124'," +
//"'2023_13125'," +
//"'2023_13127'," +
//"'2023_13137'," +
//"'2023_13138'," +
//"'2023_13163'," +
//"'2023_13170'," +
//"'2023_13236'," +
//"'2023_13239'," +
//"'2023_13286'," +
//"'2023_13288'," +
//"'2023_13289'," +
//"'2023_13345'," +
//"'2023_13346'," +
//"'2023_13354'," +
//"'2023_13379'," +
//"'2023_13380'," +
//"'2023_13395'," +
//"'2023_13398'," +
//"'2023_13404'," +
//"'2023_13405'," +
//"'2023_13406'," +
//"'2023_13407'," +
//"'2023_13408'," +
//"'2023_13417'," +
//"'2023_13420'," +
//"'2023_13507'," +
//"'2023_13507'," +
//"'2023_13507'," +
//"'2023_13508'," +
//"'2023_13508'," +
//"'2023_13508'," +
//"'2023_13509'," +
//"'2023_13510'," +
//"'2023_13521'," +
//"'2023_13522'," +
//"'2023_13533'," +
//"'2023_13534'," +
//"'2023_13535'," +
//"'2023_13550'," +
//"'2023_13551'," +
//"'2023_13554'," +
//"'2023_13555'," +
//"'2023_13583'," +
//"'2023_13584'," +
//"'2023_13586'," +
//"'2023_13588'," +
//"'2023_13589'," +
//"'2023_13590'," +
//"'2023_13591'," +
//"'2023_13597'," +
//"'2023_13598'," +
//"'2023_13605');  ";

        try {
            Statement st2 = conn.createStatement();
            ResultSet res2 = st2.executeQuery(sql);

            ArrayList<OTrnDps> dpss = new ArrayList();
            OTrnDps dps = null;
            while (res2.next()) {
                dps = new OTrnDps();
                
                dps.setIdYear(res2.getInt("id_year"));
                dps.setIdDoc(res2.getInt("id_doc"));
                dps.setDt(res2.getDate("dt"));
                dps.setTaxesCount(res2.getInt("taxes"));
                dps.setEtis(res2.getInt("etis"));
                dps.setCatDps(res2.getInt("fid_ct_dps"));
                dps.setClassDps(res2.getInt("fid_cl_dps"));
                dps.setIdBp(res2.getInt("fid_bp_r"));

                dpss.add(dps);
            }

            return dpss;
        }
        catch (SQLException ex) {

        }

        return null;
    }
    
    /**
     * Obtiene los renglones del documento
     * 
     * @param conn
     * @param idYear
     * @param idDoc
     * @return 
     */
    public static ArrayList<ODpsEty> getEtys(Connection conn, int idYear, int idDoc) {

        String sql = "SELECT "
                + "    * "
                + "FROM "
                + "    trn_dps_ety ety "
                + "        LEFT JOIN "
                + "    trn_dps_ety_tax etytax ON ety.id_doc = etytax.id_doc "
                + "        AND ety.id_year = etytax.id_year "
                + "        AND ety.id_ety = etytax.id_ety "
                + "WHERE "
                + "  ety.id_year = " + idYear + " "
                + "  AND ety.id_doc = " + idDoc + " "
                + "  AND fid_tp_tax = " + SModSysConsts.FINS_TP_TAX_CHARGED
                + "  AND b_del = FALSE ";

        try {
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(sql);

            ArrayList<ODpsEty> etis = new ArrayList();
            ODpsEty ety = null;
            while (res.next()) {
                
                ety = new ODpsEty();
                
                ety.setIdEty(res.getInt("id_ety"));
                ety.setTaxBas(res.getInt("id_tax_bas"));
                ety.setTax(res.getInt("id_tax"));
                ety.setSubtotal(res.getDouble("stot_r"));
                ety.setSubtotalCur(res.getDouble("stot_cur_r"));
                ety.setTaxD(res.getDouble("tax_charged_r"));
                ety.setTaxCur(res.getDouble("tax_charged_cur_r"));
                ety.setTotal(res.getDouble("tot_r"));
                ety.setTotalCur(res.getDouble("tot_cur_r"));

                etis.add(ety);
            }

            return etis;
        }
        catch (SQLException ex) {

        }

        return null;
    }
    
    /**
     * Lee los renglones del documento con sus impuestos
     * 
     * @param conn
     * @param idYear
     * @param idDoc
     * @return 
     */
    public static ArrayList<OEtyTax> getEtyTaxes(Connection conn, int idYear, int idDoc) {

        String sql = "SELECT "
                + "    * "
                + "FROM "
                + "    trn_dps_ety_tax etytax "
                + "        INNER JOIN "
                + "    erp.finu_tax tax ON etytax.id_tax_bas = tax.id_tax_bas "
                + "        AND etytax.id_tax = tax.id_tax "
                + "WHERE "
                + "  etytax.id_year = " + idYear + ""
                + "  AND etytax.id_doc = " + idDoc + ""
                + "  AND etytax.fid_tp_tax = " + SModSysConsts.FINS_TP_TAX_CHARGED
                + " ";

        try {
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(sql);

            ArrayList<OEtyTax> etis = new ArrayList();
            OEtyTax etyTax = null;
            
            while (res.next()) {
                
                etyTax = new OEtyTax();
                
                etyTax.setIdEty(res.getInt("id_ety"));
                etyTax.setPercent(res.getDouble("per"));
                etyTax.setTaxBas(res.getInt("id_tax_bas"));
                etyTax.setTax(res.getInt("id_tax"));
                etyTax.setTaxName(res.getString("tax.tax"));
                etyTax.setTaxAmount(res.getDouble("etytax.tax"));
                etyTax.setTaxAmountCur(res.getDouble("etytax.tax_cur"));

                etis.add(etyTax);
            }

            return etis;
        }
        catch (SQLException ex) {

        }

        return null;
    }

    /**
     * obtiene el id_ety máximo de una póliza
     * 
     * @param conn
     * @param recEty
     * @return 
     */
    public static int getMaxRecs(Connection conn, ORecEty recEty) {
        
        String sql = "SELECT "
                + "    MAX(id_ety) as max_con "
                + "   FROM "
                + "    fin_rec_ety WHERE id_per = " + recEty.id_per + " "
                + "        AND id_bkc = " + recEty.id_bkc + " "
                + "        AND id_tp_rec = '" + recEty.id_tp_rec + "' "
                + "        AND id_num = " + recEty.id_num + " "
                + "        AND id_year = " + recEty.id_year + " ";
        
        try {
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(sql);
            
            if (res.next()) {
                return res.getInt("max_con");
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(OProcessDocuments.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 0;
    }
    
    /**
     * leer recs asociados a un documento
     * 
     * @param conn
     * @param idRecYear
     * @param idDocYear
     * @param idDoc
     * @param docCat
     * @param docClass
     * @param tpAccMovs
     * @param ctaCus
     * @param ctaSupp
     * @return 
     */
    public static ArrayList<OFinRec> getRecs(Connection conn, int idRecYear, int idDocYear, int idDoc, int docCat, int docClass, int tpAccMovs, String ctaCus, String ctaSupp) {
        int[] dpsTp = null;

        if (docCat == ClassifySiie.CT_SALES) {
            dpsTp = ClassifySiie.FINS_TP_SYS_MOV_BPS_CUS;
        }
        else {
            dpsTp = ClassifySiie.FINS_TP_SYS_MOV_BPS_SUP;
        }
        
        String filterSql = "";
        
        if (docClass == ClassifySiie.TRNU_TP_DPS_SAL_CN[1]) {
            filterSql += " AND re.fid_dps_adj_doc_n = " + idDoc + " "
                        + " AND re.fid_dps_adj_year_n = " + idDocYear + " ";
        }
        else {
            filterSql += " AND re.fid_dps_doc_n = " + idDoc + " "
                        + " AND re.fid_dps_year_n = " + idDocYear + " "
                        + " AND re.fid_dps_adj_doc_n IS NULL ";
        }
        
        if (ClassifySiie.TP_DOCUMENTS == tpAccMovs) {
            filterSql += "AND (re.fid_tp_acc_mov = " + 5 + " OR re.fid_tp_acc_mov = " + 4 + " OR re.fid_tp_acc_mov = 16 OR re.fid_tp_acc_mov = 2)";
        }
        else {
            filterSql += "AND (re.fid_tp_acc_mov <> " + 5 + " AND re.fid_tp_acc_mov <> " + 4 + " OR re.fid_tp_acc_mov = 16 OR re.fid_tp_acc_mov = 2)";
        }

        String sql = "SELECT "
                + "    * "
                + "   FROM "
                + "    fin_rec AS r "
                + "        INNER JOIN "
                + "    fin_rec_ety AS re ON r.id_year = re.id_year "
                + "        AND r.id_per = re.id_per "
                + "        AND r.id_bkc = re.id_bkc "
                + "        AND r.id_tp_rec = re.id_tp_rec "
                + "        AND r.id_num = re.id_num "
                + "        AND r.id_year = " + idRecYear + " "
//                + "        AND r.dt <= '" + idYear + "-12-31'"
                + "        AND NOT r.b_del"
                + "        AND NOT re.b_del"
                + "        AND re.fid_ct_sys_mov_xxx = " + dpsTp[0]
                + "        AND re.fid_tp_sys_mov_xxx = " + dpsTp[1]
                + "        AND (re.fid_acc LIKE '" + ctaCus + "%' OR re.fid_acc LIKE '" + ctaSupp + "%') "
                + " " + filterSql
                + " ORDER BY r.id_year ASC, r.dt ASC;";
        
        try {
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(sql);

            ArrayList<OFinRec> recs = new ArrayList();
            OFinRec rec = null;
            
            while (res.next()) {
                rec = new OFinRec();

                rec.setIdYear(res.getInt("re.id_year"));
                rec.setIdPer(res.getInt("re.id_per"));
                rec.setIdBkc(res.getInt("re.id_bkc"));
                rec.setIdTpRec(res.getString("re.id_tp_rec"));
                rec.setIdNum(res.getInt("re.id_num"));
                rec.setIdEty(res.getInt("re.id_ety"));
                rec.setDt(res.getDate("r.dt"));

                recs.add(rec);
            }

            return recs;
        }
        catch (SQLException ex) {
            Logger.getLogger(OProcessDocuments.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
    
    /**
     * read rec_ety
     * 
     * @param conn
     * @param idYear
     * @param idPer
     * @param idBkc
     * @param idTpRec
     * @param idNum
     * @param idEty
     * @return 
     */
    public static ORecEty getRecEty(Connection conn, int idYear, int idPer, int idBkc, String idTpRec, int idNum, int idEty) {
        
        String sql = "SELECT * FROM fin_rec_ety WHERE id_year = " + idYear +
                " AND id_per = " + idPer + 
                " AND id_bkc = " + idBkc +
                " AND id_tp_rec = '" + idTpRec + "' " +
                " AND id_num = " + idNum +
                " AND id_ety = " + idEty + ";";
        
        try {
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(sql);
            ORecEty rec;
            
            if (res.next()) {
                rec = new ORecEty();
                
                rec.id_year = res.getInt("id_year");
                rec.id_per = res.getInt("id_per");
                rec.id_bkc = res.getInt("id_bkc");
                rec.id_tp_rec = res.getString("id_tp_rec");
                rec.id_num = res.getInt("id_num");
                rec.id_ety = res.getInt("id_ety");
                rec.concept = res.getString("concept");
                rec.ref = res.getString("ref");
                rec.b_ref_tax = res.getBoolean("b_ref_tax");
                rec.debit = res.getDouble("debit");
                rec.credit = res.getDouble("credit");
                rec.exc_rate = res.getDouble("exc_rate");
                rec.exc_rate_sys = res.getDouble("exc_rate_sys");
                rec.debit_cur = res.getDouble("debit_cur");
                rec.credit_cur = res.getDouble("credit_cur");
                rec.units = res.getDouble("units");
                rec.usr_id = res.getInt("usr_id");
                rec.sort_pos = res.getInt("sort_pos");
                rec.occ_fiscal_id = res.getString("occ_fiscal_id");
                rec.b_exc_diff = res.getBoolean("b_exc_diff");
                rec.b_sys = res.getBoolean("b_sys");
                rec.b_del = res.getBoolean("b_del");
                rec.fid_acc = res.getString("fid_acc");
                rec.fk_acc = res.getInt("fk_acc");
                rec.fk_cc_n = res.getInt("fk_cc_n");
                rec.fid_tp_acc_mov = res.getInt("fid_tp_acc_mov");
                rec.fid_cl_acc_mov = res.getInt("fid_cl_acc_mov");
                rec.fid_cls_acc_mov = res.getInt("fid_cls_acc_mov");
                rec.fid_cl_sys_mov = res.getInt("fid_cl_sys_mov");
                rec.fid_tp_sys_mov = res.getInt("fid_tp_sys_mov");
                rec.fid_cl_sys_acc = res.getInt("fid_cl_sys_acc");
                rec.fid_tp_sys_acc = res.getInt("fid_tp_sys_acc");
                rec.fid_ct_sys_mov_xxx = res.getInt("fid_ct_sys_mov_xxx");
                rec.fid_tp_sys_mov_xxx = res.getInt("fid_tp_sys_mov_xxx");
                rec.fid_cur = res.getInt("fid_cur");
                rec.fid_cc_n = res.getString("fid_cc_n");
                rec.fid_check_wal_n = res.getInt("fid_check_wal_n");
                rec.fid_check_n = res.getInt("fid_check_n");
                rec.fid_bp_nr = res.getInt("fid_bp_nr");
                rec.fid_bpb_n = res.getInt("fid_bpb_n");
                rec.fid_ct_ref_n = res.getInt("fid_ct_ref_n");
                rec.fid_cob_n = res.getInt("fid_cob_n");
                rec.fid_ent_n = res.getInt("fid_ent_n");
                rec.fid_plt_cob_n = res.getInt("fid_plt_cob_n");
                rec.fid_plt_ent_n = res.getInt("fid_plt_ent_n");
                rec.fid_tax_bas_n = res.getInt("fid_tax_bas_n");
                rec.fid_tax_n = res.getInt("fid_tax_n");
                rec.fid_year_n = res.getInt("fid_year_n");
                rec.fid_dps_year_n = res.getInt("fid_dps_year_n");
                rec.fid_dps_doc_n = res.getInt("fid_dps_doc_n");
                rec.fid_dps_adj_year_n = res.getInt("fid_dps_adj_year_n");
                rec.fid_dps_adj_doc_n = res.getInt("fid_dps_adj_doc_n");
                rec.fid_diog_year_n = res.getInt("fid_diog_year_n");
                rec.fid_diog_doc_n = res.getInt("fid_diog_doc_n");
                rec.fid_mfg_year_n = res.getInt("fid_mfg_year_n");
                rec.fid_mfg_ord_n = res.getInt("fid_mfg_ord_n");
                rec.fid_cfd_n = res.getInt("fid_cfd_n");
                rec.fid_cost_gic_n = res.getInt("fid_cost_gic_n");
                rec.fid_payroll_n = res.getInt("fid_payroll_n");
                rec.fid_pay_n = res.getInt("fid_pay_n");
                rec.fid_item_n = res.getInt("fid_item_n");
                rec.fid_item_aux_n = res.getInt("fid_item_aux_n");
                rec.fid_unit_n = res.getInt("fid_unit_n");
                rec.fid_bkk_year_n = res.getInt("fid_bkk_year_n");
                rec.fid_bkk_num_n = res.getInt("fid_bkk_num_n");
                rec.fid_usr_new = res.getInt("fid_usr_new");
                rec.fid_usr_edit = res.getInt("fid_usr_edit");
                rec.fid_usr_del = res.getInt("fid_usr_del");
                rec.ts_new = res.getDate("ts_new");
                rec.ts_edit = res.getDate("ts_edit");
                rec.ts_del = res.getDate("ts_del");
 
                return rec;
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(OProcessDocuments.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
    
    /**
     * Insert new rec_ety
     * 
     * @param conn
     * @param recEty
     * @return 
     */
    public static boolean insertRecEty(Connection conn, ORecEty recEty, String fileName) throws SQLException {
        
        DateFormat dft = new SimpleDateFormat("yyyy-MM-dd");
        
        String sql = "INSERT INTO fin_rec_ety " +
                        "(id_year," +
                        "id_per," +
                        "id_bkc," +
                        "id_tp_rec," +
                        "id_num," +
                        "id_ety," +
                        "concept," +
                        "ref," +
                        "b_ref_tax," +
                        "debit," +
                        "credit," +
                        "exc_rate," +
                        "exc_rate_sys," +
                        "debit_cur," +
                        "credit_cur," +
                        "units," +
                        "usr_id," +
                        "sort_pos," +
                        "occ_fiscal_id," +
                        "b_exc_diff," +
                        "b_sys," +
                        "b_del," +
                        "fid_acc," +
                        "fk_acc," +
                        "fk_cc_n," +
                        "fid_tp_acc_mov," +
                        "fid_cl_acc_mov," +
                        "fid_cls_acc_mov," +
                        "fid_cl_sys_mov," +
                        "fid_tp_sys_mov," +
                        "fid_cl_sys_acc," +
                        "fid_tp_sys_acc," +
                        "fid_ct_sys_mov_xxx," +
                        "fid_tp_sys_mov_xxx," +
                        "fid_cur," +
                        "fid_cc_n," +
                        "fid_check_wal_n," +
                        "fid_check_n," +
                        "fid_bp_nr," +
                        "fid_bpb_n," +
                        "fid_ct_ref_n," +
                        "fid_cob_n," +
                        "fid_ent_n," +
                        "fid_plt_cob_n," +
                        "fid_plt_ent_n," +
                        "fid_tax_bas_n," +
                        "fid_tax_n," +
                        "fid_year_n," +
                        "fid_dps_year_n," +
                        "fid_dps_doc_n," +
                        "fid_dps_adj_year_n," +
                        "fid_dps_adj_doc_n," +
                        "fid_diog_year_n," +
                        "fid_diog_doc_n," +
                        "fid_mfg_year_n," +
                        "fid_mfg_ord_n," +
                        "fid_cfd_n," +
                        "fid_cost_gic_n," +
                        "fid_payroll_n," +
                        "fid_pay_n," +
                        "fid_item_n," +
                        "fid_item_aux_n," +
                        "fid_unit_n," +
                        "fid_bkk_year_n," +
                        "fid_bkk_num_n," +
                        "fid_usr_new," +
                        "fid_usr_edit," +
                        "fid_usr_del," +
                        "ts_new," +
                        "ts_edit," +
                        "ts_del)" +
                        "VALUES" +
                        "(" + recEty.id_year + "," +
                        "" + recEty.id_per + " ," +
                        "" + recEty.id_bkc + " ," +
                        "'" + recEty.id_tp_rec + "' ," +
                        "" + recEty.id_num + " ," +
                        "" + recEty.id_ety + " ," +
                        "'" + recEty.concept.replace("'", "") + "' ," +
                        "'" + recEty.ref + "' ," +
                        "" + recEty.b_ref_tax + " ," +
                        "" + recEty.debit + " ," +
                        "" + recEty.credit + " ," +
                        "" + recEty.exc_rate + " ," +
                        "" + recEty.exc_rate_sys + " ," +
                        "" + recEty.debit_cur + " ," +
                        "" + recEty.credit_cur + " ," +
                        "" + recEty.units + " ," +
                        "" + recEty.usr_id + " ," +
                        "" + recEty.sort_pos + " ," +
                        "'" + recEty.occ_fiscal_id + "' ," +
                        "" + recEty.b_exc_diff + " ," +
                        "" + recEty.b_sys + " ," +
                        "" + recEty.b_del + " ," +
                        "'" + recEty.fid_acc + "' ," +
                        "" + recEty.fk_acc + " ," +
                        (recEty.fk_cc_n > 0 ? recEty.fk_cc_n : "NULL") + ", " +
                        "" + recEty.fid_tp_acc_mov + " ," +
                        "" + recEty.fid_cl_acc_mov + " ," +
                        "" + recEty.fid_cls_acc_mov + " ," +
                        "" + recEty.fid_cl_sys_mov + " ," +
                        "" + recEty.fid_tp_sys_mov + " ," +
                        "" + recEty.fid_cl_sys_acc + " ," +
                        "" + recEty.fid_tp_sys_acc + " ," +
                        "" + recEty.fid_ct_sys_mov_xxx + " ," +
                        "" + recEty.fid_tp_sys_mov_xxx + " ," +
                        "" + recEty.fid_cur + " ," +
                        "" + (recEty.fid_cc_n == null || recEty.fid_cc_n.isEmpty() ? null : "'" + recEty.fid_cc_n + "'") + " ," +
                        (recEty.fid_check_wal_n > 0 ? recEty.fid_check_wal_n : "NULL") + ", " +
                        (recEty.fid_check_n > 0 ? recEty.fid_check_n : "NULL") + ", " +
                        (recEty.fid_bp_nr > 0 ? recEty.fid_bp_nr : "NULL") + ", " +
                        (recEty.fid_bpb_n > 0 ? recEty.fid_bpb_n : "NULL") + ", " +
                        (recEty.fid_ct_ref_n > 0 ? recEty.fid_ct_ref_n : "NULL") + ", " +
                        (recEty.fid_cob_n > 0 ? recEty.fid_cob_n : "NULL") + ", " +
                        (recEty.fid_ent_n > 0 ? recEty.fid_ent_n : "NULL") + ", " +
                        (recEty.fid_plt_cob_n > 0 ? recEty.fid_plt_cob_n : "NULL") + ", " +
                        (recEty.fid_plt_ent_n > 0 ? recEty.fid_plt_ent_n : "NULL") + ", " +
                        (recEty.fid_tax_bas_n > 0 ? recEty.fid_tax_bas_n : "NULL") + ", " +
                        (recEty.fid_tax_n > 0 ? recEty.fid_tax_n : "NULL") + ", " +
                        (recEty.fid_year_n > 0 ? recEty.fid_year_n : "NULL") + ", " +
                        (recEty.fid_dps_year_n > 0 ? recEty.fid_dps_year_n : "NULL") + ", " +
                        (recEty.fid_dps_doc_n > 0 ? recEty.fid_dps_doc_n : "NULL") + ", " +
                        (recEty.fid_dps_adj_year_n > 0 ? recEty.fid_dps_adj_year_n : "NULL") + ", " +
                        (recEty.fid_dps_adj_doc_n > 0 ? recEty.fid_dps_adj_doc_n : "NULL") + ", " +
                        (recEty.fid_diog_year_n > 0 ? recEty.fid_diog_year_n : "NULL") + ", " +
                        (recEty.fid_diog_doc_n > 0 ? recEty.fid_diog_doc_n : "NULL") + ", " +
                        (recEty.fid_mfg_year_n > 0 ? recEty.fid_mfg_year_n : "NULL") + ", " +
                        (recEty.fid_mfg_ord_n > 0 ? recEty.fid_mfg_ord_n : "NULL") + ", " +
                        (recEty.fid_cfd_n > 0 ? recEty.fid_cfd_n : "NULL") + ", " +
                        (recEty.fid_cost_gic_n > 0 ? recEty.fid_cost_gic_n : "NULL") + ", " +
                        (recEty.fid_payroll_n > 0 ? recEty.fid_payroll_n : "NULL") + ", " +
                        (recEty.fid_pay_n > 0 ? recEty.fid_pay_n : "NULL") + ", " +
                        (recEty.fid_item_n > 0 ? recEty.fid_item_n : "NULL") + ", " +
                        (recEty.fid_item_aux_n > 0 ? recEty.fid_item_aux_n : "NULL") + ", " +
                        (recEty.fid_unit_n > 0 ? recEty.fid_unit_n : "NULL") + ", " +
                        (recEty.fid_bkk_year_n > 0 ? recEty.fid_bkk_year_n : "NULL") + ", " +
                        (recEty.fid_bkk_num_n > 0 ? recEty.fid_bkk_num_n : "NULL") + ", " +
                        "" + recEty.fid_usr_new + " ," +
                        "" + recEty.fid_usr_edit + " ," +
                        "" + recEty.fid_usr_del + " ," +
                        "'" + dft.format(recEty.ts_new) + "' ," +
                        "'" + dft.format(recEty.ts_edit) + "' ," +
                        "'" + dft.format(recEty.ts_del) + "' );";
        
        try {
            Statement st = conn.createStatement();
            int res = st.executeUpdate(sql);
            OLog.writeFile(fileName, recEty, ClassifySiie.INSERT, ClassifySiie.OK);
            
            return true;
        }
        catch (SQLException ex) {
            Logger.getLogger(OProcessDocuments.class.getName()).log(Level.SEVERE, null, ex);
            OLog.writeFile(fileName, recEty, ClassifySiie.INSERT, ClassifySiie.ERROR);
            throw ex;
        }
    }
    
    
    /**
     * update b_del = true
     * 
     * @param conn
     * @param recEty
     * @param fileName
     * @return 
     * @throws java.sql.SQLException 
     */
    public static boolean deleteRecEty(Connection conn, ORecEty recEty, String fileName) throws SQLException {
        String sql = "UPDATE fin_rec_ety " +
                    " SET " +
                    " b_del = " + recEty.b_del + " " +
                    " WHERE " +
                    "    id_year = " + recEty.id_year + 
                    "        AND id_per = " + recEty.id_per + "" +
                    "        AND id_bkc = " + recEty.id_bkc + "" +
                    "        AND id_tp_rec = '" + recEty.id_tp_rec + "'" +
                    "        AND id_num = '" + recEty.id_num + "'" +
                    "        AND id_ety = " + recEty.id_ety + ";";
        
        try {
            Statement st = conn.createStatement();
            int res = st.executeUpdate(sql);
            
            OLog.writeFile(fileName, recEty, ClassifySiie.DELETE, ClassifySiie.OK);
            
            return true;
        }
        catch (SQLException ex) {
            Logger.getLogger(OProcessDocuments.class.getName()).log(Level.SEVERE, null, ex);
            OLog.writeFile(fileName, recEty, ClassifySiie.DELETE, ClassifySiie.ERROR);
            throw ex;
        }
    }
    
    /**
     * Leer cuentas contables para la obtención del pk
     * 
     * @param conn
     * @return
     * @throws SQLException 
     */
    public static HashMap<String, Integer> getAccs(Connection conn) throws SQLException {
        HashMap<String, Integer> accs = new HashMap();
        
        String sql = "SELECT id_acc, pk_acc FROM fin_acc WHERE NOT b_del;";
        
        try {
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(sql);
            
            while (res.next()) {
                accs.put(res.getString("id_acc"), res.getInt("pk_acc"));
            }
            
            return accs;
        }
        catch (SQLException ex) {
            Logger.getLogger(OProcessDocuments.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }
    
    /**
     * Leer configuración
     * 
     * @param idBizPartner
     * @param idBizPartnerCategory
     * @param idBkc
     * @param dateStart
     * @param idBizPartnerAccountType
     * @param isDebit
     * @param pkTax
     * @param statement
     * @return 
     */
    public static Object[] readCfg(int idBizPartner, int idBizPartnerCategory, int idBkc, java.util.Date dateStart, int idBizPartnerAccountType, 
                        boolean isDebit, int[] pkTax, java.sql.Statement statement) {
        try {
            boolean isDefault = false;
            if (pkTax != null) {
                if (pkTax[0] == 0) {
                    pkTax = null;
                    isDefault = true;
                }
            }
            
            Vector<SFinAccountConfigEntry> accountConfigs = SFinAccountUtilities.obtainBizPartnerAccountConfigs(idBizPartner, idBizPartnerCategory, idBkc, dateStart, idBizPartnerAccountType, isDebit, pkTax, statement);
            
            if (pkTax != null && (accountConfigs == null || accountConfigs.isEmpty())) {
                accountConfigs = SFinAccountUtilities.obtainBizPartnerAccountConfigs(idBizPartner, idBizPartnerCategory, idBkc, dateStart, idBizPartnerAccountType, isDebit, null, statement);
                isDefault = true;
            }
            
            return new Object[] { accountConfigs.get(0), isDefault };
        }
        catch (Exception ex) {
            Logger.getLogger(OProcessDocuments.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
}
