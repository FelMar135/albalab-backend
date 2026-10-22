package com.albalab.backend.service;

import cl.transbank.common.IntegrationApiKeys;
import cl.transbank.common.IntegrationCommerceCodes;
import cl.transbank.common.IntegrationType;
import cl.transbank.webpay.common.WebpayOptions;
import cl.transbank.webpay.webpayplus.WebpayPlus;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCreateResponse;
import org.springframework.stereotype.Service;

@Service
public class WebpayService {

    public WebpayPlusTransactionCreateResponse crearTransaccion(String ordenCompra, String idSesion, double monto, String urlRetorno) throws Exception {
        // Inicializa la transacción con credenciales de integración (Test)
        WebpayPlus.Transaction tx = new WebpayPlus.Transaction(new WebpayOptions(
                IntegrationCommerceCodes.WEBPAY_PLUS,
                IntegrationApiKeys.WEBPAY,
                IntegrationType.TEST
        ));
        
        return tx.create(ordenCompra, idSesion, monto, urlRetorno);
    }
}