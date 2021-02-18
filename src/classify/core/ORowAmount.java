/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classify.core;

import erp.mfin.data.SFinAccountConfigEntry;

/**
 *
 * @author Edwin Carmona
 */
public class ORowAmount {
    
    private double[] amount;
    private SFinAccountConfigEntry accCfg;
    private int[] taxPk;

    public double[] getAmount() {
        return amount;
    }

    public void setAmount(double[] amount) {
        this.amount = amount;
    }

    public SFinAccountConfigEntry getAccCfg() {
        return accCfg;
    }

    public void setAccCfg(SFinAccountConfigEntry accCfg) {
        this.accCfg = accCfg;
    }

    public int[] getTaxPk() {
        return taxPk;
    }

    public void setTaxPk(int[] taxPk) {
        this.taxPk = taxPk;
    }
}
