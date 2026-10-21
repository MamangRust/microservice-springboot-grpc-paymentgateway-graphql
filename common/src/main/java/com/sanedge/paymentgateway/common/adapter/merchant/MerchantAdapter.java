package com.sanedge.paymentgateway.common.adapter.merchant;

import com.sanedge.paymentgateway.common.adapter.model.Merchant;
import com.sanedge.paymentgateway.common.adapter.support.ProtoTime;
import com.sanedge.paymentgateway.common.exception.ResourceNotFoundException;
import com.sanedge.paymentgateway.pb.merchant.FindByApiKeyRequest;
import com.sanedge.paymentgateway.pb.merchant.FindByIdMerchantRequest;
import com.sanedge.paymentgateway.pb.merchant.MerchantQueryServiceGrpc.MerchantQueryServiceBlockingStub;
import com.sanedge.paymentgateway.pb.merchant.MerchantResponse;

public class MerchantAdapter implements MerchantPort {

    private final MerchantQueryServiceBlockingStub query;

    public MerchantAdapter(MerchantQueryServiceBlockingStub query) {
        this.query = query;
    }

    @Override
    public Merchant findByApiKey(String apiKey) {
        var resp = query.findByApiKey(FindByApiKeyRequest.newBuilder().setApiKey(apiKey).build());
        if (resp == null || !resp.hasData()) {
            throw new ResourceNotFoundException("Merchant not found for api key");
        }
        return toMerchant(resp.getData());
    }

    @Override
    public Merchant findByMerchantId(int merchantId) {
        var resp = query.findByIdMerchant(FindByIdMerchantRequest.newBuilder().setMerchantId(merchantId).build());
        if (resp == null || !resp.hasData()) {
            throw new ResourceNotFoundException("Merchant not found: " + merchantId);
        }
        return toMerchant(resp.getData());
    }

    private static Merchant toMerchant(MerchantResponse m) {
        if (m == null) {
            return null;
        }
        return new Merchant(m.getId(), m.getName(), m.getApiKey(), m.getStatus(), m.getUserId(),
                ProtoTime.parse(m.getCreatedAt()), ProtoTime.parse(m.getUpdatedAt()));
    }
}
