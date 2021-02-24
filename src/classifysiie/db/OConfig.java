/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classifysiie.db;

/**
 *
 * @author Edwin Carmona
 */
public class OConfig {
    protected OConnection siieConnection;
    protected String ctaMCustomerReclassFrom;
    protected String ctaMSupplierReclassFrom;
    protected String ctaCustomerReclassDes;
    protected String ctaSupplierReclassDes;
    protected String ctaCustToSearch;
    protected String ctaSupToSearch;

    public OConnection getSiieConnection() {
        return siieConnection;
    }

    public void setSiieConnection(OConnection siieConnection) {
        this.siieConnection = siieConnection;
    }

    public String getCtaMCustomerReclassFrom() {
        return ctaMCustomerReclassFrom;
    }

    public void setCtaMCustomerReclassFrom(String ctaMCustomerReclassFrom) {
        this.ctaMCustomerReclassFrom = ctaMCustomerReclassFrom;
    }

    public String getCtaMSupplierReclassFrom() {
        return ctaMSupplierReclassFrom;
    }

    public void setCtaMSupplierReclassFrom(String ctaMSupplierReclassFrom) {
        this.ctaMSupplierReclassFrom = ctaMSupplierReclassFrom;
    }

    public String getCtaCustomerReclassDes() {
        return ctaCustomerReclassDes;
    }

    public void setCtaCustomerReclassDes(String ctaCustomerReclassDes) {
        this.ctaCustomerReclassDes = ctaCustomerReclassDes;
    }

    public String getCtaSupplierReclassDes() {
        return ctaSupplierReclassDes;
    }

    public void setCtaSupplierReclassDes(String ctaSupplierReclassDes) {
        this.ctaSupplierReclassDes = ctaSupplierReclassDes;
    }

    public String getCtaCustToSearch() {
        return ctaCustToSearch;
    }

    public void setCtaCustToSearch(String ctaCustToSearch) {
        this.ctaCustToSearch = ctaCustToSearch;
    }

    public String getCtaSupToSearch() {
        return ctaSupToSearch;
    }

    public void setCtaSupToSearch(String ctaSupToSearch) {
        this.ctaSupToSearch = ctaSupToSearch;
    }
    
}
