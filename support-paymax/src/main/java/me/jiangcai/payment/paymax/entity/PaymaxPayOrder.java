package me.jiangcai.payment.paymax.entity;

import lombok.Getter;
import lombok.Setter;
import me.jiangcai.payment.PaymentForm;
import me.jiangcai.payment.entity.PayOrder;
import me.jiangcai.payment.paymax.PaymaxPaymentForm;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

/**
 * @author CJ
 */
@Entity
@Setter
@Getter
public class PaymaxPayOrder extends PayOrder {

    /**
     * 扫码支付的URL
     */
    @Column(length = 100)
    private String scanUrl;
    /**
     * 一段脚本可以引导支付
     */
    @Lob
    private String javascriptToPay;
    /**
     * 我们自己产生的编码UUID
     *
     * @see com.paymax.model.Charge#orderNo
     */
    @Column(length = 32)
    private String orderNumber;

    /**
     * @see com.paymax.model.Charge#status
     */
    @Column(length = 15)
    private String orderStatus;


    @Override
    public Class<? extends PaymentForm> getPaymentFormClass() {
        return PaymaxPaymentForm.class;
    }
}
