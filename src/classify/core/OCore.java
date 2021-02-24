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
import java.util.TreeMap;

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
            
            int[] majTax = new int[] { 0, 0 };
            
            // si hay más de un impuesto y la póliza del documento solo registra un renglón
            if (recs.size() == 1 && document.getTaxesCount() > recs.size()) {
                // Ajustar renglones de póliza del documento
                ArrayList<ODpsEty> etys = OProcessDocuments.getEtys(c.connectMySQL(), document.getIdYear(), document.getIdDoc());
                HashMap<String, double[]> amounts = new HashMap();
                ArrayList<ORowAmount> cfgAmounts = new ArrayList();

                String key;
                double amount;
                double amountCur;
                for (ODpsEty ety : etys) {
                    key = ety.getTaxBas() + "_" + ety.getTax();

                    if (amounts.containsKey(key)) {
                        amount = amounts.get(key)[0] + ety.getTotal();
                        amountCur = amounts.get(key)[1] + ety.getTotalCur();

                        amounts.replace( key, new double[] {amount, amountCur} );
                    }
                    else {
                        amounts.put(key, new double[] { ety.getTotal(), ety.getTotalCur() } );
                    }
                }
                
                OFinRec etyTax = recs.get(0);
                ORecEty recEty = OProcessDocuments.getRecEty(c.connectMySQL(), etyTax.getIdYear(), etyTax.getIdPer(), etyTax.getIdBkc(), etyTax.getIdTpRec(), etyTax.getIdNum(), etyTax.getIdEty());

                int max = OProcessDocuments.getMaxRecs(c.connectMySQL(), recEty);
                // consultar el consecutivo más alto de la póliza para hacer la separación por impuesto
                int idEty = max;
                if (max > 0) {
                    ORecEty recEtyN;

                    for (Map.Entry<String, double[]> entry : amounts.entrySet()) {
                        String key1 = entry.getKey();
                        double[] aAmount = entry.getValue();
                        
                        String[] k = key1.split("_");
                        int taxBas = Integer.parseInt(k[0]);
                        int tax = Integer.parseInt(k[1]);

                        int[] pkTax = new int[] { taxBas, tax };
                        // consultar la configuración de la cuenta contable correspondiente al impuesto del monto
                        Object[] result = OProcessDocuments.readCfg(
                                document.getIdBp(), STrnUtils.getBizPartnerCategoryId(document.getCatDps()), etyTax.getIdBkc(),
                                document.getDt(), SDataConstantsSys.FINS_TP_ACC_BP_OP, SModSysConsts.BPSS_CT_BP_CUS == document.getCatDps(), pkTax, (c.connectMySQL()).createStatement());

                        if (result == null) {
                            return;
                        }
                        
                        accCfg = (SFinAccountConfigEntry) result[0];
                        
                        boolean exists = false;
                        for (ORowAmount cfgAmount : cfgAmounts) {
                            if (cfgAmount.getAccCfg().getAccountId().equals(accCfg.getAccountId())) {
                                cfgAmount.getAmount()[0] += aAmount[0];
                                cfgAmount.getAmount()[1] += aAmount[1];
                                exists = true;
                                if (cfgAmount.getTaxPk() != pkTax) {
                                    cfgAmount.setTaxPk(new int[] { 0, 0});
                                }
                                break;
                            }
                        }
                        
                        if (! exists) {
                            ORowAmount amt = new ORowAmount();
                            amt.setAccCfg(accCfg);
                            amt.setAmount(aAmount);
                            if ((Boolean) result[1]) {
                                amt.setTaxPk(new int[] { 0, 0});
                            }
                            else {
                                amt.setTaxPk(pkTax);
                            }
                            
                            cfgAmounts.add(amt);
                        }
                    }
                    
                    if (cfgAmounts.size() > 1 || (cfgAmounts.size() == 1 && (! recEty.fid_acc.equals(cfgAmounts.get(0).getAccCfg().getAccountId())))) {
                        for (ORowAmount cfgAmount : cfgAmounts) {
                            recEtyN = (ORecEty) recEty.clone();

                            recEtyN.fk_acc = accs.get(cfgAmount.getAccCfg().getAccountId());
                            recEtyN.fid_acc = cfgAmount.getAccCfg().getAccountId();
                            recEtyN.fid_cc_n = cfgAmount.getAccCfg().getCostCenterId();
                            recEtyN.fid_tax_bas_n = cfgAmount.getTaxPk()[0];
                            recEtyN.fid_tax_n = cfgAmount.getTaxPk()[1];

                            if(recEtyN.debit_cur > 0) {
                                recEtyN.credit = 0d;
                                recEtyN.credit_cur = 0d;
                                // debit
                                recEtyN.debit = cfgAmount.getAmount()[0];
                                recEtyN.debit_cur = cfgAmount.getAmount()[1];
                            }
                            else {
                                recEtyN.debit = 0d;
                                recEtyN.debit_cur = 0d;
                                //credit
                                recEtyN.credit = cfgAmount.getAmount()[0];
                                recEtyN.credit_cur = cfgAmount.getAmount()[1];
                            }

                            recEtyN.id_ety = ++idEty;

                            OProcessDocuments.insertRecEty(c.connectMySQL(), recEtyN, fileName);    
                        }

                        recEty.b_del = true;
                        OProcessDocuments.deleteRecEty(c.connectMySQL(), recEty, fileName);
                    }
                    
                    double maj = 0d;
                    for (ORowAmount cfgAmount : cfgAmounts) {
                        if (cfgAmount.getAmount()[1] > maj) {
                            maj = cfgAmount.getAmount()[1];
                            majTax = cfgAmount.getTaxPk();
                        }
                    }
                    
                    situation = ClassifySiie.SEVERAL_TAXES;
                }
            }
            else {
                if (recs.size() > 1 && document.getClassDps() != ClassifySiie.TRNU_TP_DPS_SAL_CN[1]) {
                    continue;
                }
                
                for (OFinRec etyRec : recs) {
                    boolean isNewEty = false;
                    ORecEty oRecEtyOriginal = OProcessDocuments.getRecEty(c.connectMySQL(), etyRec.getIdYear(), etyRec.getIdPer(), etyRec.getIdBkc(), etyRec.getIdTpRec(), etyRec.getIdNum(), etyRec.getIdEty());
                    ORecEty oRecEty = (ORecEty) oRecEtyOriginal.clone();

                    // si el documento no tiene impuestos
                    if (etyTaxes.isEmpty()) {
                        // consultar la configuración de la cuenta contable correspondiente al impuesto del monto
                        Object[] result = OProcessDocuments.readCfg(
                                document.getIdBp(), STrnUtils.getBizPartnerCategoryId(document.getCatDps()), etyRec.getIdBkc(),
                                document.getDt(), SDataConstantsSys.FINS_TP_ACC_BP_OP, SModSysConsts.BPSS_CT_BP_CUS == document.getCatDps(), null, (c.connectMySQL()).createStatement());

                        if (result == null) {
                            return;
                        }

                        accCfg = (SFinAccountConfigEntry) result[0];

                        if (! oRecEty.fid_acc.equals(accCfg.getAccountId())) {
                            oRecEty.fk_acc = accs.get(accCfg.getAccountId());
                            oRecEty.fid_acc = accCfg.getAccountId();
                            oRecEty.fid_cc_n = accCfg.getCostCenterId();

                            isNewEty = true;
                        }

                        situation = ClassifySiie.NO_TAXES;
                    }
                    // si el documento solo tiene un impuesto
                    else {
                        OEtyTax etyTax = etyTaxes.get(0);
                        int[] pkTax = new int[] { etyTax.getTaxBas(), etyTax.getTax() };
                        // consultar la configuración de la cuenta contable correspondiente al impuesto del monto
                        Object[] result = OProcessDocuments.readCfg(
                                document.getIdBp(), STrnUtils.getBizPartnerCategoryId(document.getCatDps()), etyRec.getIdBkc(),
                                document.getDt(), SDataConstantsSys.FINS_TP_ACC_BP_OP, SModSysConsts.BPSS_CT_BP_CUS == document.getCatDps(), pkTax, (c.connectMySQL()).createStatement());

                        if (result == null) {
                            return;
                        }

                        accCfg = (SFinAccountConfigEntry) result[0];

                        if (! oRecEty.fid_acc.equals(accCfg.getAccountId()) || oRecEty.fid_tax_bas_n != etyTax.getTaxBas() || oRecEty.fid_tax_n != etyTax.getTax()) {
                            oRecEty.fk_acc = accs.get(accCfg.getAccountId());
                            oRecEty.fid_acc = accCfg.getAccountId();
                            oRecEty.fid_cc_n = accCfg.getCostCenterId();
                            oRecEty.fid_tax_bas_n = etyTax.getTaxBas();
                            oRecEty.fid_tax_n = etyTax.getTax();

                            isNewEty = true;
                        }

                        situation = ClassifySiie.ONE_TAX;
                    }

                    if (isNewEty) {
                        // consultar el consecutivo más alto de la póliza para hacer la separación por impuesto
                        int max = OProcessDocuments.getMaxRecs(c.connectMySQL(), oRecEty);

                        oRecEty.id_ety = ++max;
                        OProcessDocuments.insertRecEty(c.connectMySQL(), oRecEty, fileName);

                        oRecEtyOriginal.b_del = true;
                        OProcessDocuments.deleteRecEty(c.connectMySQL(), oRecEtyOriginal, fileName);
                    }
                }
            }
            
            
            /******************************************************************************************************************************************************************** */
            /******************************************************************************************************************************************************************** */
            /******************************************************************************************************************************************************************** */
            
            
            // consultar si el documento tiene pagos
            ArrayList<OFinRec> payRecs = OProcessDocuments.getRecs(c.connectMySQL(), document.getIdYear(), document.getIdDoc(), document.getCatDps(), document.getClassDps(), ClassifySiie.TP_RECORDS);
            
            if (payRecs.isEmpty()) {
                continue;
            }
            
            HashMap<Integer, ArrayList<OFinRec>> yearRecs = new HashMap();
            for (OFinRec payRec : payRecs) {
                if (! yearRecs.containsKey(payRec.getIdYear())) {
                    ArrayList<OFinRec> recsTemp = new ArrayList();
                    recsTemp.add(payRec);
                    yearRecs.put(payRec.getIdYear(), recsTemp);
                }
                else {
                    yearRecs.get(payRec.getIdYear()).add(payRec);
                }
            }
            
            TreeMap<Integer, ArrayList<OFinRec>> tm = new TreeMap(yearRecs);
            
            // si tiene pagos y no están separados por impuesto, deben ser separados también por año
            
            for (Map.Entry<Integer, ArrayList<OFinRec>> entry : tm.entrySet()) {
                Integer recYear = entry.getKey();
                ArrayList<OFinRec> yearPayRecs = entry.getValue();
                
                SFinAccountConfigEntry accCfgn = null;
                switch(situation) {
                    case ClassifySiie.NO_TAXES:
                        // tiene una o varias partidas pero sin impuesto

                        // consultar la configuración de la cuenta contable correspondiente al impuesto del monto
                        Object[] result = OProcessDocuments.readCfg(
                                document.getIdBp(), STrnUtils.getBizPartnerCategoryId(document.getCatDps()), yearPayRecs.get(0).getIdBkc(),
                                document.getDt(), SDataConstantsSys.FINS_TP_ACC_BP_OP, SModSysConsts.BPSS_CT_BP_CUS == document.getCatDps(), null, (c.connectMySQL()).createStatement());

                        if (result == null) {
                            return;
                        }

                        accCfgn = (SFinAccountConfigEntry) result[0];

                        for (OFinRec payRec : yearPayRecs) {
                            ORecEty payRecEty = OProcessDocuments.getRecEty(c.connectMySQL(), payRec.getIdYear(), payRec.getIdPer(), payRec.getIdBkc(), payRec.getIdTpRec(), payRec.getIdNum(), payRec.getIdEty());

                            ORecEty oPayRecEty = (ORecEty) payRecEty.clone();

                            if (! oPayRecEty.fid_acc.equals(accCfgn.getAccountId())) {
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
                        }
                        break;

                    case ClassifySiie.ONE_TAX:
                        // tiene una o varias partidas con un solo impuesto (el mismo todas las partidas)

                        OEtyTax etyTax = etyTaxes.get(0);
                        int[] pkTax = new int[] { etyTax.getTaxBas(), etyTax.getTax() };

                        for (OFinRec payRec : yearPayRecs) {
                            // consultar la configuración de la cuenta contable correspondiente al impuesto del monto
                            Object[] result1 = OProcessDocuments.readCfg(
                                    document.getIdBp(), STrnUtils.getBizPartnerCategoryId(document.getCatDps()), payRec.getIdBkc(),
                                    document.getDt(), SDataConstantsSys.FINS_TP_ACC_BP_OP, SModSysConsts.BPSS_CT_BP_CUS == document.getCatDps(), pkTax, (c.connectMySQL()).createStatement());


                            if (result1 == null) {
                                return;
                            }

                            accCfgn = (SFinAccountConfigEntry) result1[0];

                            ORecEty payRecEty = OProcessDocuments.getRecEty(c.connectMySQL(), payRec.getIdYear(), payRec.getIdPer(), payRec.getIdBkc(), payRec.getIdTpRec(), payRec.getIdNum(), payRec.getIdEty());

                            ORecEty oPayRecEty = (ORecEty) payRecEty.clone();

                            if (! oPayRecEty.fid_acc.equals(accCfgn.getAccountId()) || oPayRecEty.fid_tax_bas_n != etyTax.getTaxBas() || oPayRecEty.fid_tax_n != etyTax.getTax()) {
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
                        }
                        break;

                    case ClassifySiie.SEVERAL_TAXES:
                        // tiene una o varias partidas con varios impuestos (diferente cada una)
                        // recorrer los renglones de los pagos que se han hecho a la factura
                        // se borran estos renglones para disntribuir los pagos por impuestos
                        for (OFinRec payFinRec : yearPayRecs) {
                            ORecEty payRecEty = OProcessDocuments.getRecEty(c.connectMySQL(), payFinRec.getIdYear(), payFinRec.getIdPer(), payFinRec.getIdBkc(), payFinRec.getIdTpRec(), payFinRec.getIdNum(), payFinRec.getIdEty());
                            ORecEty oPayRecEty = (ORecEty) payRecEty.clone();
                            payRecEty.b_del = true;
                            OProcessDocuments.deleteRecEty(c.connectMySQL(), payRecEty, fileName);

                            oPayRecEty.setDt(payFinRec.getDt());
                            
                            ArrayList<SBalanceTax> newBalances = SMfinUtils.getBalanceByTax(c.connectMySQL(), document.getIdDoc(), document.getIdYear(), recYear, "", 
                                    STrnUtils.getBizPartnerCategoryId(document.getCatDps()) == SDataConstantsSys.BPSS_CT_BP_SUP ? SDataConstantsSys.FINS_TP_SYS_MOV_BPS_SUP[0] : SDataConstantsSys.FINS_TP_SYS_MOV_BPS_CUS[0], 
                                    STrnUtils.getBizPartnerCategoryId(document.getCatDps()) == SDataConstantsSys.BPSS_CT_BP_SUP ? SDataConstantsSys.FINS_TP_SYS_MOV_BPS_SUP[1] : SDataConstantsSys.FINS_TP_SYS_MOV_BPS_CUS[1]);

                            double creditCur = oPayRecEty.credit_cur;
                            double credit = oPayRecEty.credit;
                            double debitCur = oPayRecEty.debit_cur;
                            double debit = oPayRecEty.debit;

                            boolean wasInserted = false;
                            // consultar el consecutivo más alto de la póliza para hacer la separación por impuesto
                            int max = OProcessDocuments.getMaxRecs(c.connectMySQL(), oPayRecEty);
                            for (SBalanceTax newBalance : newBalances) {
                                ORecEty balanceEty = (ORecEty) oPayRecEty.clone();
                                balanceEty.id_ety = ++max;
                                
                                newBalance.setBalanceCurrency(Math.abs(newBalance.getBalanceCurrency()));
                                newBalance.setBalance(Math.abs(newBalance.getBalance()));

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
                                Object[] result2 = OProcessDocuments.readCfg(
                                        document.getIdBp(), STrnUtils.getBizPartnerCategoryId(document.getCatDps()), balanceEty.id_bkc,
                                        document.getDt(), SDataConstantsSys.FINS_TP_ACC_BP_OP, SModSysConsts.BPSS_CT_BP_CUS == document.getCatDps(), newBalance.getTaxPk(), (c.connectMySQL()).createStatement());

                                if (result2 == null) {
                                    return;
                                }

                                SFinAccountConfigEntry accConf = (SFinAccountConfigEntry) result2[0];

                                balanceEty.fk_acc = accs.get(accConf.getAccountId());
                                balanceEty.fid_acc = accConf.getAccountId();
                                balanceEty.fid_cc_n = accConf.getCostCenterId();
                                balanceEty.fid_tax_bas_n = newBalance.getTaxPk()[0];
                                balanceEty.fid_tax_n = newBalance.getTaxPk()[1];

                                OProcessDocuments.insertRecEty(c.connectMySQL(), balanceEty, fileName);
                                wasInserted = true;
                            }
                            
                            /**
                             * Cuando el documento ya no tiene saldo pero hay renglones en la póliza solo se mandan a la configuración por default
                             * pero se queda el mismo renglón
                             */
                            if (newBalances.isEmpty() || ! wasInserted) {
                                ORecEty balanceEty = (ORecEty) oPayRecEty.clone();
                                balanceEty.id_ety = ++max;
                                // consultar la configuración de la cuenta contable correspondiente al impuesto del monto
                                Object[] result2 = OProcessDocuments.readCfg(
                                        document.getIdBp(), STrnUtils.getBizPartnerCategoryId(document.getCatDps()), balanceEty.id_bkc,
                                        document.getDt(), SDataConstantsSys.FINS_TP_ACC_BP_OP, SModSysConsts.BPSS_CT_BP_CUS == document.getCatDps(), majTax, (c.connectMySQL()).createStatement());

                                if (result2 == null) {
                                    return;
                                }

                                SFinAccountConfigEntry accConf = (SFinAccountConfigEntry) result2[0];

                                balanceEty.fk_acc = accs.get(accConf.getAccountId());
                                balanceEty.fid_acc = accConf.getAccountId();
                                balanceEty.fid_cc_n = accConf.getCostCenterId();
                                balanceEty.fid_tax_bas_n = majTax[0];
                                balanceEty.fid_tax_n = majTax[1];

                                OProcessDocuments.insertRecEty(c.connectMySQL(), balanceEty, fileName);
                            }
                        }

                        break;
                }
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
