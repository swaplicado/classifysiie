# Reclasificación de saldos siie
## Reclasificación de saldos por impuesto

Esta es una herramienta para la reclasificación de saldos de pólizas contables 
de siie por impuesto

## Instrucciones:

Este es el archivo de configuración:

```sh
    "ctaCustToSearch" : "1120",
    "ctaSupToSearch" : "2105",
    "ctaMCustomerReclassFrom" : "2120-0000-0000",
    "ctaCustomerReclassDes" : "2120-0009-0000",
    "reclassTaxCus" : [ 0, 0 ],
    "ctaMSupplierReclassFrom" : "1140-0000-0000",
    "ctaSupplierReclassDes" : "1140-0003-0000",
    "reclassTaxSupp" : [ 1, 6 ]
```

**ctaCustToSearch** Se refeiere a la cuenta contable desde la cuál hará la
búsqueda para comenzar la clasificación de clientes.

**ctaSupToSearch** Se refeiere a la cuenta contable desde la cuál hará la
búsqueda para comenzar la clasificación de proveedores.

## Esto solo es necesario para el complemento (UPDATES del script de BD):

**ctaMCustomerReclassFrom** Cuenta contable desde la cuál se moverán los saldos de clientes.

**ctaCustomerReclassDes** Cuenta contable destino clientes.

**reclassTaxCus** Llave del impuesto al cual serán movidos los saldos de clientes.

**ctaMSupplierReclassFrom** Cuenta contable desde la cuál se moverán los saldos de proveedores.

**ctaSupplierReclassDes** Cuenta contable destino proveedores.

**reclassTaxSupp** Llave del impuesto al cual serán movidos los saldos de proveedores.

## **NOTA:**
Se corre el proceso normal con el año a reclasificar y en vez de correr 
el complemento se corren las querys de UPDATE