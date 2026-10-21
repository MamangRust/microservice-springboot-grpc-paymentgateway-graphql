package com.sanedge.paymentgateway.common.adapter.merchant;

import com.sanedge.paymentgateway.common.adapter.model.Merchant;

/**
 * Port for the merchant service, replacing direct {@code @GrpcClient("merchant")}
 * usage in consumer services.
 */
public interface MerchantPort {

    Merchant findByApiKey(String apiKey);

    Merchant findByMerchantId(int merchantId);
}
