/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classify.core;

/**
 *
 * @author Edwin Carmona
 */
public class OEtyTax {
    int taxBas;
    int tax;
    String taxName;
    int idEty;
    double percent;
    double taxAmount;
    double taxAmountCur;

    public int getTaxBas() {
        return taxBas;
    }

    public void setTaxBas(int taxBas) {
        this.taxBas = taxBas;
    }

    public int getTax() {
        return tax;
    }

    public void setTax(int tax) {
        this.tax = tax;
    }

    public String getTaxName() {
        return taxName;
    }

    public void setTaxName(String taxName) {
        this.taxName = taxName;
    }

    public int getIdEty() {
        return idEty;
    }

    public void setIdEty(int idEty) {
        this.idEty = idEty;
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public double getTaxAmountCur() {
        return taxAmountCur;
    }

    public void setTaxAmountCur(double taxAmountCur) {
        this.taxAmountCur = taxAmountCur;
    }
    
}
