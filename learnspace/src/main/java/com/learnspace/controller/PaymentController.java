package com.learnspace.controller;

import com.learnspace.model.AppUser;
import com.learnspace.model.Batch;
import com.learnspace.model.Enrollment;
import com.learnspace.repository.BatchRepository;
import com.learnspace.repository.EnrollmentRepository;
import com.learnspace.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
 private final BatchRepository batches; private final EnrollmentRepository enrollments; private final UserRepository users;
 @Value("${razorpay.key-id}") private String keyId;
 @Value("${razorpay.key-secret}") private String keySecret;
 PaymentController(BatchRepository b,EnrollmentRepository e,UserRepository u){batches=b;enrollments=e;users=u;}
 record Purchase(Long batchId){} record PaymentOrder(String key,String orderId,long amount,String currency,String course,String batch){} record Verification(String razorpayPaymentId,String razorpayOrderId,String razorpaySignature){}
 private AppUser user(HttpSession session){Long id=(Long)session.getAttribute("user");if(id==null)throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Please sign in to purchase");return users.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED));}
 private Batch batch(Long id){return batches.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Batch not found"));}
 private void configured(){if(keyId==null||keyId.isBlank()||keySecret==null||keySecret.isBlank())throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Razorpay is not configured. Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET.");}
 @PostMapping("/orders") public PaymentOrder order(@RequestBody Purchase purchase,HttpSession session){configured();AppUser student=user(session);Batch batch=batch(purchase.batchId());if(enrollments.existsByStudentIdAndBatchId(student.getId(),batch.getId()))throw new ResponseStatusException(HttpStatus.CONFLICT,"You have already purchased this batch");if(enrollments.countByBatchId(batch.getId())>=batch.getSeats())throw new ResponseStatusException(HttpStatus.CONFLICT,"This batch is full");long amount=Math.round(batch.getCourse().getPrice()*100);try{RazorpayClient client=new RazorpayClient(keyId,keySecret);JSONObject options=new JSONObject();options.put("amount",amount);options.put("currency","INR");options.put("receipt","batch_"+batch.getId()+"_user_"+student.getId());Order order=client.orders.create(options);String orderId=String.valueOf(order.get("id"));session.setAttribute("razorpay.order."+orderId,batch.getId());return new PaymentOrder(keyId,orderId,amount,"INR",batch.getCourse().getTitle(),batch.getName());}catch(Exception ex){throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Unable to create Razorpay order",ex);}}
 @PostMapping("/verify") public Enrollment verify(@RequestBody Verification verification,HttpSession session){configured();AppUser student=user(session);Object value=session.getAttribute("razorpay.order."+verification.razorpayOrderId());if(!(value instanceof Long batchId))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unknown or expired payment order");Batch batch=batch(batchId);if(enrollments.existsByStudentIdAndBatchId(student.getId(),batchId))throw new ResponseStatusException(HttpStatus.CONFLICT,"You have already purchased this batch");try{JSONObject attributes=new JSONObject();attributes.put("razorpay_order_id",verification.razorpayOrderId());attributes.put("razorpay_payment_id",verification.razorpayPaymentId());attributes.put("razorpay_signature",verification.razorpaySignature());if(!Utils.verifyPaymentSignature(attributes,keySecret))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Payment signature verification failed");Enrollment enrollment=new Enrollment();enrollment.setStudent(student);enrollment.setBatch(batch);enrollment.setPaid(batch.getCourse().getPrice());session.removeAttribute("razorpay.order."+verification.razorpayOrderId());return enrollments.save(enrollment);}catch(ResponseStatusException ex){throw ex;}catch(Exception ex){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Payment verification failed",ex);}}
}
