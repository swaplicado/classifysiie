/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classify.core;

import clasifysiie.ClassifySiie;
import classifysiie.db.DbMySqlConnection;
import classifysiie.db.OConfig;
import classifysiie.db.OConfigReader;
import classifysiie.db.OProcessDocuments;
import erp.data.SDataConstantsSys;
import erp.mfin.data.SFinAccountConfigEntry;
import erp.mfin.utils.SBalanceTax;
import erp.mfin.utils.SMfinUtils;
import erp.mod.SModSysConsts;
import erp.mod.trn.db.STrnUtils;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Edwin Carmona
 */
public class OCore {
    
    private String fileName;
    private HashMap<String, Integer> accs;
    
    public void reclassify(final int year) throws CloneNotSupportedException, SQLException {
        OConfigReader cfgReader = new OConfigReader();
        OConfig cfg = cfgReader.readConfig(); 
        DbMySqlConnection c = new DbMySqlConnection(cfg.getSiieConnection().getNameDb(), cfg.getSiieConnection().getHostDb(), cfg.getSiieConnection().getPortDb(), cfg.getSiieConnection().getUserDb(), cfg.getSiieConnection().getPswdDb());
        ArrayList<OTrnDps> documents = OProcessDocuments.getDocuments(c.connectMySQL(), year);
        
        fileName = OLog.writeFile(null, null, 0, null);
        accs = OProcessDocuments.getAccs(c.connectMySQL());
        
        int situation = 0;
        SFinAccountConfigEntry accCfg;
        for (OTrnDps document : documents) {
            // obtener renglones del documento con impuesto
            ArrayList<OEtyTax> etyTaxes = OProcessDocuments.getEtyTaxes(c.connectMySQL(), document.getIdYear(), document.getIdDoc());
            
            // obtener renglones del documento en pólizas
            ArrayList<OFinRec> recs = OProcessDocuments.getRecs(c.connectMySQL(), document.getIdYear(), document.getIdDoc(), document.getCatDps(), document.getClassDps(), ClassifySiie.TP_DOCUMENTS);
            
            if (recs.size() == 1 && document.getTaxesCount() > recs.size()) {
                // Ajustar renglones de póliza del documento
                ArrayList<ODpsEty> etys = OProcessDocuments.getEtys(c.connectMySQL(), document.getIdYear(), document.getIdDoc());
                HashMap<String, Double[]> amounts = new HashMap();

                String key;
                double amount;
                double amountCur;
                for (ODpsEty ety : etys) {
                    key = ety.getTaxBas() + "_" + ety.getTax();

                    if (amounts.containsKey(key)) {
                        amount = amounts.get(key)[0] + ety.getTotal();
                        amountCur = amounts.get(key)[1] + ety.getTotalCur();

                        amounts.replace( key, new Double[] {amount, amountCur} );
                    }
                    else {
                        amounts.put(key, new Double[] { ety.getTotal(), ety.getTotalCur() } );
                    }
                }

                // consultar el consecutivo más alto de la póliza para hacer la separación por impuesto
                OFinRec etyTax = recs.get(0);
                ORecEty recEty = OProcessDocuments.getRecEty(c.connectMySQL(), etyTax.getIdYear(), etyTax.getIdPer(), etyTax.getIdBkc(), etyTax.getIdTpRec(), etyTax.getIdNum(), etyTax.getIdEty());

                int max = OProcessDocuments.getMaxRecs(c.connectMySQL(), recEty);
                int idEty = max;
                if (max > 0) {
                    ORecEty recEtyN;

                    for (Map.Entry<String, Double[]> entry : amounts.entrySet()) {
                        String key1 = entry.getKey();
                        Double[] aAmount = entry.getValue();

                        recEtyN = (ORecEty) recEty.clone();
                        
                        String[] k = key1.split("_");
                        int taxBas = Integer.parseInt(k[0]);
                        int tax = Integer.parseInt(k[1]);

                        int[] pkTax = new int[] { taxBas, tax };
                        // consultar la configuración de la cuenta contable correspondiente al impuesto del monto
                        accCfg = OProcessDocuments.readCfg(
                                document.getIdBp(), STrnUtils.getBizPartnerCategoryId(document.getCatDps()), etyTax.getIdBkc(),
                                document.getDt(), SDataConstantsSys.FINS_TP_ACC_BP_OP, SModSysConsts.BPSS_CT_BP_CUS == document.getCatDps(), pkTax, (c.connectMySQL()).createStatement());

                        if (accCfg == null) {
                            return;
                        }

                        recEtyN.fk_acc = accs.get(accCfg.getAccountId());
                        recEtyN.fid_acc = accCfg.getAccountId();
                        recEtyN.fid_cc_n = accCfg.getCostCenterId();
                        recEtyN.fid_tax_bas_n = taxBas;
                        recEtyN.fid_tax_n = tax;
                        
                        if(recEtyN.debit_cur > 0) {
                            recEtyN.credit = 0d;
                            recEtyN.credit_cur = 0d;
                            // debit
                            recEtyN.debit = aAmount[0];
                            recEtyN.debit_cur = aAmount[1];

                        }
                        else {
                            recEtyN.debit = 0d;
                            recEtyN.debit_cur = 0d;
                            //credit
                            recEtyN.credit = aAmount[0];
                            recEtyN.credit_cur = aAmount[1];
                        }

                        recEtyN.id_ety = ++idEty;
                        
                        OProcessDocuments.insertRecEty(c.connectMySQL(), recEtyN, fileName);    
                    }
                    
                    recEty.b_del = true;
                    OProcessDocuments.deleteRecEty(c.connectMySQL(), recEty, fileName);
                    
                    situation = ClassifySiie.SEVERAL_TAXES;
                }
            }
            else {
                if (recs.size() > 1) {
                    continue;
                }
                
                OFinRec etyRec = recs.get(0);
                ORecEty oRecEtyOriginal = OProcessDocuments.getRecEty(c.connectMySQL(), etyRec.getIdYear(), etyRec.getIdPer(), etyRec.getIdBkc(), etyRec.getIdTpRec(), etyRec.getIdNum(), etyRec.getIdEty());
                ORecEty oRecEty = (ORecEty) oRecEtyOriginal.clone();
                
                if (etyTaxes.isEmpty()) {
                    // consultar la configuración de la cuenta contable correspondiente al impuesto del monto
                    accCfg = OProcessDocuments.readCfg(
                            document.getIdBp(), STrnUtils.getBizPartnerCategoryId(document.getCatDps()), etyRec.getIdBkc(),
                            document.getDt(), SDataConstantsSys.FINS_TP_ACC_BP_OP, SModSysConsts.BPSS_CT_BP_CUS == document.getCatDps(), null, (c.connectMySQL()).createStatement());

                    if (accCfg == null) {
                        return;
                    }
                    
                    if (oRecEty.fid_acc.equals(accCfg.getAccountId())) {
                        continue;
                    }

                    oRecEty.fk_acc = accs.get(accCfg.getAccountId());
                    oRecEty.fid_acc = accCfg.getAccountId();
                    oRecEty.fid_cc_n = accCfg.getCostCenterId();
                    
                    situation = ClassifySiie.NO_TAXES;
                }
                else {
                    OEtyTax etyTax = etyTaxes.get(0);
                    int[] pkTax = new int[] { etyTax.getTaxBas(), etyTax.getTax() };
                    // consultar la configuración de la cuenta contable correspondiente al impuesto del monto
                    accCfg = OProcessDocuments.readCfg(
                            document.getIdBp(), STrnUtils.getBizPartnerCategoryId(document.getCatDps()), etyRec.getIdBkc(),
                            document.getDt(), SDataConstantsSys.FINS_TP_ACC_BP_OP, SModSysConsts.BPSS_CT_BP_CUS == document.getCatDps(), pkTax, (c.connectMySQL()).createStatement());

                    if (accCfg == null) {
                        return;
                    }
                    
                    if (oRecEty.fid_acc.equals(accCfg.getAccountId())) {
                        continue;
                    }

                    oRecEty.fk_acc = accs.get(accCfg.getAccountId());
                    oRecEty.fid_acc = accCfg.getAccountId();
                    oRecEty.fid_cc_n = accCfg.getCostCenterId();
                    oRecEty.fid_tax_bas_n = etyTax.getTaxBas();
                    oRecEty.fid_tax_n = etyTax.getTax();
                    
                    situation = ClassifySiie.ONE_TAX;
                }
                // consultar el consecutivo más alto de la póliza para hacer la separación por impuesto
                int max = OProcessDocuments.getMaxRecs(c.connectMySQL(), oRecEty);
                
                oRecEty.id_ety = ++max;
                OProcessDocuments.insertRecEty(c.connectMySQL(), oRecEty, fileName);
                
                oRecEtyOriginal.b_del = true;
                OProcessDocuments.deleteRecEty(c.connectMySQL(), oRecEtyOriginal, fileName);
            }
            
            // consultar si el documento tiene pagos
            ArrayList<OFinRec> payRecs = OProcessDocuments.getRecs(c.connectMySQL(), document.getIdYear(), document.getIdDoc(), document.getCatDps(), document.getClassDps(), ClassifySiie.TP_RECORDS);
            
            if (payRecs.isEmpty()) {
                continue;
            }
            
            // si tiene pagos y no están separados por impuesto, deben ser separados también
            
            SFinAccountConfigEntry accCfgn = null;
            switch(situation) {
                case ClassifySiie.NO_TAXES:
                    // tiene una o varias partidas pero sin impuesto

                    // consultar la configuración de la cuenta contable correspondiente al impuesto del monto
                    accCfgn = OProcessDocuments.readCfg(
                            document.getIdBp(), STrnUtils.getBizPartnerCategoryId(document.getCatDps()), payRecs.get(0).getIdBkc(),
                            document.getDt(), SDataConstantsSys.FINS_TP_ACC_BP_OP, SModSysConsts.BPSS_CT_BP_CUS == document.getCatDps(), null, (c.connectMySQL()).createStatement());
                    
                    if (accCfgn == null) {
                        return;
                    }
            
                    for (OFinRec payRec : payRecs) {
                        ORecEty payRecEty = OProcessDocuments.getRecEty(c.connectMySQL(), payRec.getIdYear(), payRec.getIdPer(), payRec.getIdBkc(), payRec.getIdTpRec(), payRec.getIdNum(), payRec.getIdEty());
                        
                        ORecEty oPayRecEty = (ORecEty) payRecEty.clone();
                        
                        if (oPayRecEty.fid_acc.equals(accCfgn.getAccountId())) {
                            continue;
                        }
                        
                        oPayRecEty.fk_acc = accs.get(accCfgn.getAccountId());
                        oPayRecEty.fid_acc = accCfgn.getAccountId();
                        oPayRecEty.fid_cc_n = accCfgn.getCostCenterId();
                        
                        
                        payRecEty.b_del = true;
                        OProcessDocuments.deleteRecEty(c.connectMySQL(), payRecEty, fileName);
                        
                        // consultar el consecutivo más alto de la póliza para hacer la separación por impuesto
                        int max = OProcessDocuments.getMaxRecs(c.connectMySQL(), payRecEty);
                        oPayRecEty.id_ety = ++max;
                        OProcessDocuments.insertRecEty(c.connectMySQL(), oPayRecEty, fileName);
                    }
                    break;
                    
                case ClassifySiie.ONE_TAX:
                    // tiene una o varias partidas con un solo impuesto (el mismo todas las partidas)
                    
                    OEtyTax etyTax = etyTaxes.get(0);
                    int[] pkTax = new int[] { etyTax.getTaxBas(), etyTax.getTax() };
                    
                    for (OFinRec payRec : payRecs) {
                        // consultar la configuración de la cuenta contable correspondiente al impuesto del monto
                        accCfgn = OProcessDocuments.readCfg(
                                document.getIdBp(), STrnUtils.getBizPartnerCategoryId(document.getCatDps()), payRec.getIdBkc(),
                                document.getDt(), SDataConstantsSys.FINS_TP_ACC_BP_OP, SModSysConsts.BPSS_CT_BP_CUS == document.getCatDps(), pkTax, (c.connectMySQL()).createStatement());
                        

                        if (accCfgn == null) {
                            return;
                        }
                        
                        ORecEty payRecEty = OProcessDocuments.getRecEty(c.connectMySQL(), payRec.getIdYear(), payRec.getIdPer(), payRec.getIdBkc(), payRec.getIdTpRec(), payRec.getIdNum(), payRec.getIdEty());
                        
                        ORecEty oPayRecEty = (ORecEty) payRecEty.clone();
                        
                         if (oPayRecEty.fid_acc.equals(accCfgn.getAccountId())) {
                            continue;
                        }
                        
                        oPayRecEty.fk_acc = accs.get(accCfgn.getAccountId());
                        oPayRecEty.fid_acc = accCfgn.getAccountId();
                        oPayRecEty.fid_cc_n = accCfgn.getCostCenterId();
                        oPayRecEty.fid_tax_bas_n = etyTax.getTaxBas();
                        oPayRecEty.fid_tax_n = etyTax.getTax();
                        
                        payRecEty.b_del = true;
                        OProcessDocuments.deleteRecEty(c.connectMySQL(), payRecEty, fileName);
                        
                        // consultar el consecutivo más alto de la póliza para hacer la separación por impuesto
                        int max = OProcessDocuments.getMaxRecs(c.connectMySQL(), payRecEty);
                        oPayRecEty.id_ety = ++max;
                        OProcessDocuments.insertRecEty(c.connectMySQL(), oPayRecEty, fileName);
                    }
                    break;
                    
                case ClassifySiie.SEVERAL_TAXES:
                    // tiene una o varias partidas con varios impuestos (diferente cada una)
                    ArrayList<ORecEty> pays = new ArrayList();
                    // recorrer los renglones de los pagos que se han hecho a la factura
                    // se borran estos renglones para disntribuir los pagos por impuestos
                    for (OFinRec payFinRec : payRecs) {
                        ORecEty payRecEty = OProcessDocuments.getRecEty(c.connectMySQL(), payFinRec.getIdYear(), payFinRec.getIdPer(), payFinRec.getIdBkc(), payFinRec.getIdTpRec(), payFinRec.getIdNum(), payFinRec.getIdEty());
                        ORecEty oPayRecEty = (ORecEty) payRecEty.clone();

                        pays.add(oPayRecEty);

                        payRecEty.b_del = true;
                        OProcessDocuments.deleteRecEty(c.connectMySQL(), payRecEty, fileName);
                    }
                    
                    for (ORecEty newRecEty : pays) {
                        ArrayList<SBalanceTax> newBalances = SMfinUtils.getBalanceByTax(c.connectMySQL(), document.getIdDoc(), document.getIdYear(), "", 
                                STrnUtils.getBizPartnerCategoryId(document.getCatDps()) == SDataConstantsSys.BPSS_CT_BP_SUP ? SDataConstantsSys.FINS_TP_SYS_MOV_BPS_SUP[0] : SDataConstantsSys.FINS_TP_SYS_MOV_BPS_CUS[0], 
                                STrnUtils.getBizPartnerCategoryId(document.getCatDps()) == SDataConstantsSys.BPSS_CT_BP_SUP ? SDataConstantsSys.FINS_TP_SYS_MOV_BPS_SUP[1] : SDataConstantsSys.FINS_TP_SYS_MOV_BPS_CUS[1]);

                        double creditCur = newRecEty.credit_cur;
                        double credit = newRecEty.credit;
                        double debitCur = newRecEty.debit_cur;
                        double debit = newRecEty.debit;

                        // consultar el consecutivo más alto de la póliza para hacer la separación por impuesto
                        int max = OProcessDocuments.getMaxRecs(c.connectMySQL(), newRecEty);
                        for (SBalanceTax newBalance : newBalances) {
                            ORecEty balanceEty = (ORecEty) newRecEty.clone();
                            balanceEty.id_ety = ++max;

                            if (creditCur > 0d) {
                                if (creditCur >= newBalance.getBalanceCurrency()) {
                                    balanceEty.credit = newBalance.getBalance();
                                    balanceEty.credit_cur = newBalance.getBalanceCurrency();
                                }
                                else {
                                    balanceEty.credit = credit;
                                    balanceEty.credit_cur = creditCur;
                                }

                                creditCur -= newBalance.getBalanceCurrency();
                                credit -= newBalance.getBalance();
                            }
                            else if (debitCur > 0d) {
                                if (debitCur >= newBalance.getBalanceCurrency()) {
                                    balanceEty.debit = newBalance.getBalance();
                                    balanceEty.debit_cur = newBalance.getBalanceCurrency();
                                }
                                else {
                                    balanceEty.debit = debit;
                                    balanceEty.debit_cur = debitCur;
                                }

                                debitCur -= newBalance.getBalanceCurrency();
                                debit -= newBalance.getBalance();
                            }
                            else {
                                break;
                            }

                            // consultar la configuración de la cuenta contable correspondiente al impuesto del monto
                            SFinAccountConfigEntry accConf = OProcessDocuments.readCfg(
                                    document.getIdBp(), STrnUtils.getBizPartnerCategoryId(document.getCatDps()), balanceEty.id_bkc,
                                    document.getDt(), SDataConstantsSys.FINS_TP_ACC_BP_OP, SModSysConsts.BPSS_CT_BP_CUS == document.getCatDps(), newBalance.getTaxPk(), (c.connectMySQL()).createStatement());

                            if (accConf == null) {
                                return;
                            }

                            balanceEty.fk_acc = accs.get(accConf.getAccountId());
                            balanceEty.fid_acc = accConf.getAccountId();
                            balanceEty.fid_cc_n = accConf.getCostCenterId();
                            balanceEty.fid_tax_bas_n = newBalance.getTaxPk()[0];
                            balanceEty.fid_tax_n = newBalance.getTaxPk()[1];

                            OProcessDocuments.insertRecEty(c.connectMySQL(), balanceEty, fileName);
                        }
                    }
                    
                    break;
            }
        }
    }
    
    private boolean isDebitForOperations(int ct, int cl) {
        return this.isDocumentPur(ct, cl) || isAdjustmentSal(ct, cl);
    }
    
    public boolean isDocumentPur(int ct, int cl) {
        return ct == SDataConstantsSys.TRNS_CL_DPS_PUR_DOC[0] && cl == SDataConstantsSys.TRNS_CL_DPS_PUR_DOC[1];
    }
    
    public boolean isAdjustmentSal(int ct, int cl) {
        return ct == SDataConstantsSys.TRNS_CL_DPS_SAL_ADJ[0] && cl == SDataConstantsSys.TRNS_CL_DPS_SAL_ADJ[1];
    }
    
}
