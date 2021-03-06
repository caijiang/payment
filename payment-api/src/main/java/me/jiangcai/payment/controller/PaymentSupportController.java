package me.jiangcai.payment.controller;

import me.jiangcai.payment.PayableOrder;
import me.jiangcai.payment.entity.PayOrder;
import me.jiangcai.payment.service.PayableSystemService;
import me.jiangcai.payment.service.PaymentGatewayService;
import me.jiangcai.payment.util.RequestUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

/**
 * @author CJ
 */
@Controller
@RequestMapping("/_payment")
public class PaymentSupportController {

    private static final Log log = LogFactory.getLog(PaymentSupportController.class);

    @Autowired
    private PayableSystemService payableSystemService;
    @Autowired
    private PaymentGatewayService paymentGatewayService;
    @Autowired
    private ApplicationContext applicationContext;

    @RequestMapping(method = RequestMethod.GET, value = "/completed/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<Boolean> completed(@PathVariable("id") String id) {
        PayOrder order = paymentGatewayService.getLatestOrder(id);
        if (order != null) {
            paymentGatewayService.queryPayStatus(order);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(payableSystemService.isPaySuccess(id));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/success/{id}")
    public ModelAndView success(HttpServletRequest request, @PathVariable("id") String id) {
        // 获取那个成功支付的订单
        try {
            PayOrder order = paymentGatewayService.getSuccessOrder(id);
            final PayableOrder payableOrder = payableSystemService.getOrder(id);
            ModelAndView modelAndView = payableSystemService.paySuccess(request, payableOrder, order);
            modelAndView.addObject("payOrder", order);
            modelAndView.addObject("PayableOrder", payableOrder);
            return modelAndView;
        } catch (NoResultException ignored) {
            log.trace("可能是异步通知晚于同步回调导致的", ignored);
            ModelAndView modelAndView = payableSystemService.redirectSelfView(request);
            if (modelAndView != null)
                return modelAndView;
            //等待服务端通知
            final CheckSuccessLaterView view = new CheckSuccessLaterView();
            view.setServletContext(request.getServletContext());
            view.setApplicationContext(applicationContext);
            return new ModelAndView(view, Collections.singletonMap("url"
                    , RequestUtil.buildContextUrl(request).append("/_payment/success/").append(id)));
        }
    }

}
