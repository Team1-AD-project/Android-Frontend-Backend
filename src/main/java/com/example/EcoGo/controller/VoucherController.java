package com.example.EcoGo.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.EcoGo.dto.ResponseMessage;
import com.example.EcoGo.dto.VoucherRedeemRequestDto;
import com.example.EcoGo.model.Voucher;
import com.example.EcoGo.repository.VoucherRepository;

@RestController
@RequestMapping("/api/v1/vouchers")
public class VoucherController {
    private final VoucherRepository voucherRepository;

    public VoucherController(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    @GetMapping
    public ResponseMessage<List<Voucher>> getVouchers() {
        return ResponseMessage.success(voucherRepository.findAll());
    }

    @PostMapping("/redeem")
    public ResponseMessage<String> redeem(@RequestBody VoucherRedeemRequestDto request) {
        String msg = "Redeemed voucher " + request.getVoucherId() + " for user " + request.getUserId();
        return ResponseMessage.success(msg);
    }
}
