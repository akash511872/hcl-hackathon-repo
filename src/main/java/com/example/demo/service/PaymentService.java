package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.MerchantRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private static final BigDecimal WALLET_FEE_PERCENTAGE = new BigDecimal("0.02"); // 2% wallet fee
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private MerchantRepository merchantRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Process payment with transaction management
     * This method ensures atomicity - all operations succeed or all rollback
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentResult processPayment(Order order, Wallet wallet, Product product) {
        List<Transaction> transactions = new ArrayList<>();
        PaymentResult result = new PaymentResult();
        
        try {
            logger.info("Starting payment process for order: {}", order.getOrderNumber());
            
            // Step 1: Deduct amount from customer wallet
            BigDecimal totalAmount = order.getTotalAmount();
            BigDecimal walletFee = calculateWalletFee(totalAmount);
            BigDecimal totalDebitAmount = totalAmount.add(walletFee);
            
            if (wallet.getBalance().compareTo(totalDebitAmount) < 0) {
                throw new InsufficientBalanceException("Insufficient wallet balance. Required: " + 
                    totalDebitAmount + ", Available: " + wallet.getBalance());
            }
            
            wallet.setBalance(wallet.getBalance().subtract(totalDebitAmount));
            wallet.setUpdatedAt(LocalDateTime.now());
            walletRepository.save(wallet);
            
            // Create debit transaction
            Transaction debitTransaction = createTransaction(order, Transaction.TransactionType.DEBIT, 
                totalAmount, order.getCurrency(), "Debit from customer wallet for order " + order.getOrderNumber());
            transactions.add(debitTransaction);
            
            auditService.logAudit("Wallet", wallet.getWalletId(), AuditLog.AuditAction.WALLET_DEBITED, 
                "Debited " + totalDebitAmount + " " + order.getCurrency() + " from customer wallet", 
                wallet.getCustomer().getCustomerId());
            
            logger.info("Debited {} {} from customer wallet", totalDebitAmount, order.getCurrency());
            
            // Step 2: Mark payment as successful (represents payment gateway confirmation)
            logger.info("Payment confirmed successful by payment gateway for order: {}", order.getOrderNumber());
            auditService.logAudit("Order", order.getOrderId(), AuditLog.AuditAction.ORDER_PAYMENT_COMPLETED, 
                "Payment confirmed successful for amount " + totalAmount + " " + order.getCurrency());
            
            // ============================================================================
            // OPERATIONS PERFORMED AFTER SUCCESSFUL PAYMENT
            // ============================================================================
            
            // Step 3: WALLET FEE COLLECTION (After successful payment)
            logger.info("Collecting wallet fee after successful payment...");
            Transaction feeTransaction = createTransaction(order, Transaction.TransactionType.WALLET_FEE, 
                walletFee, order.getCurrency(), "Wallet fee collected after successful payment for order " + order.getOrderNumber());
            transactions.add(feeTransaction);
            
            auditService.logAudit("Order", order.getOrderId(), AuditLog.AuditAction.WALLET_FEE_COLLECTED, 
                "Wallet fee of " + walletFee + " " + order.getCurrency() + " collected after successful payment");
            
            logger.info("✓ Wallet fee of {} {} collected successfully", walletFee, order.getCurrency());
            
            // Step 4: INITIATE PAYMENT TO MERCHANT BANK VIA GATEWAY (After successful payment)
            logger.info("Initiating payment to merchant bank via payment gateway...");
            Merchant merchant = product.getMerchant();
            BigDecimal merchantCreditAmount = totalAmount.subtract(walletFee);
            
            // Simulate payment gateway initiation to merchant bank
            boolean gatewaySuccess = initiatePaymentGateway(merchant, merchantCreditAmount, order.getCurrency(), order.getOrderNumber());
            
            if (!gatewaySuccess) {
                throw new PaymentGatewayException("Payment gateway failed to initiate payment to merchant bank");
            }
            
            // Update merchant wallet balance (local record - actual settlement happens later)
            merchant.setWalletBalance(merchant.getWalletBalance().add(merchantCreditAmount));
            merchant.setUpdatedAt(LocalDateTime.now());
            merchantRepository.save(merchant);
            
            // Create credit transaction
            Transaction creditTransaction = createTransaction(order, Transaction.TransactionType.CREDIT, 
                merchantCreditAmount, order.getCurrency(), 
                "Payment initiated to merchant " + merchant.getMerchantName() + " via gateway for order " + order.getOrderNumber());
            transactions.add(creditTransaction);
            
            auditService.logAudit("Merchant", merchant.getMerchantId(), AuditLog.AuditAction.MERCHANT_CREDITED, 
                "Payment of " + merchantCreditAmount + " " + order.getCurrency() + 
                " initiated to merchant bank via gateway (pending settlement). Wallet fee " + walletFee + " deducted.");
            
            logger.info("✓ Payment of {} {} initiated to merchant bank via gateway successfully", merchantCreditAmount, order.getCurrency());
            
            // ============================================================================
            
            // Step 5: Send notification to merchant about payment
            try {
                notificationService.sendMerchantNotification(order, merchant.getEmail(), 
                    "Payment of " + merchantCreditAmount + " " + order.getCurrency() + 
                    " has been initiated to your bank account for order " + order.getOrderNumber() + 
                    ". Settlement will be processed within 1-3 business days.");
            } catch (Exception e) {
                // Notification failure should not fail the transaction
                logger.error("Failed to send merchant notification, but payment succeeded", e);
            }
            
            // Mark all transactions as completed
            for (Transaction transaction : transactions) {
                transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
                transaction.setUpdatedAt(LocalDateTime.now());
                transactionRepository.save(transaction);
            }
            
            auditService.logAudit("Order", order.getOrderId(), AuditLog.AuditAction.ORDER_PAYMENT_COMPLETED, 
                "Payment processing completed: Amount debited, wallet fee collected, merchant payment initiated via gateway");
            
            result.setSuccess(true);
            result.setMessage("Payment processed successfully. Wallet fee collected and merchant payment initiated via gateway.");
            result.setTransactions(transactions);
            result.setWalletFee(walletFee);
            result.setRemainingBalance(wallet.getBalance());
            result.setGatewayInitiated(true);
            
            logger.info("Payment process completed successfully for order: {}", order.getOrderNumber());
            
        } catch (Exception e) {
            logger.error("Payment processing failed for order: {} - {}", order.getOrderNumber(), e.getMessage(), e);
            
            // Mark all transactions as failed
            for (Transaction transaction : transactions) {
                transaction.setStatus(Transaction.TransactionStatus.FAILED);
                transaction.setFailureReason(e.getMessage());
                transaction.setUpdatedAt(LocalDateTime.now());
                transactionRepository.save(transaction);
            }
            
            auditService.logAudit("Order", order.getOrderId(), AuditLog.AuditAction.ORDER_PAYMENT_FAILED, 
                "Payment failed: " + e.getMessage());
            
            result.setSuccess(false);
            result.setMessage("Payment processing failed: " + e.getMessage());
            result.setTransactions(transactions);
            result.setGatewayInitiated(false);
            
            throw new PaymentProcessingException("Payment processing failed: " + e.getMessage(), e);
        }
        
        return result;
    }
    
    /**
     * Simulate payment gateway initiation to merchant bank
     * In a real implementation, this would call external payment gateway API
     */
    private boolean initiatePaymentGateway(Merchant merchant, BigDecimal amount, String currency, String orderNumber) {
        try {
            logger.info("Calling payment gateway API to initiate transfer...");
            logger.info("Gateway Request - Merchant: {}, Amount: {} {}, Order: {}", 
                merchant.getMerchantName(), amount, currency, orderNumber);
            
            // Simulate payment gateway processing time
            Thread.sleep(100);
            
            // Simulate successful gateway response (in production, this would be actual API call)
            String gatewayTxnId = "GW-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            logger.info("Gateway Response - Status: SUCCESS, Transaction ID: {}", gatewayTxnId);
            
            auditService.logAudit("Merchant", merchant.getMerchantId(), AuditLog.AuditAction.MERCHANT_CREDITED, 
                "Payment gateway initiated transfer of " + amount + " " + currency + 
                " to merchant bank account. Gateway Transaction ID: " + gatewayTxnId);
            
            return true;
        } catch (Exception e) {
            logger.error("Payment gateway initiation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Calculate wallet fee (2% of transaction amount)
     */
    private BigDecimal calculateWalletFee(BigDecimal amount) {
        return amount.multiply(WALLET_FEE_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Create a transaction record
     */
    private Transaction createTransaction(Order order, Transaction.TransactionType type, 
                                         BigDecimal amount, String currency, String description) {
        Transaction transaction = new Transaction();
        transaction.setTransactionNumber("TXN-" + UUID.randomUUID().toString());
        transaction.setOrder(order);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setCurrency(currency);
        transaction.setDescription(description);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * Result class for payment processing
     */
    public static class PaymentResult {
        private boolean success;
        private String message;
        private List<Transaction> transactions;
        private BigDecimal walletFee;
        private BigDecimal remainingBalance;
        private boolean gatewayInitiated;
        
        // Getters and Setters
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public List<Transaction> getTransactions() {
            return transactions;
        }
        
        public void setTransactions(List<Transaction> transactions) {
            this.transactions = transactions;
        }
        
        public BigDecimal getWalletFee() {
            return walletFee;
        }
        
        public void setWalletFee(BigDecimal walletFee) {
            this.walletFee = walletFee;
        }
        
        public BigDecimal getRemainingBalance() {
            return remainingBalance;
        }
        
        public void setRemainingBalance(BigDecimal remainingBalance) {
            this.remainingBalance = remainingBalance;
        }
        
        public boolean isGatewayInitiated() {
            return gatewayInitiated;
        }
        
        public void setGatewayInitiated(boolean gatewayInitiated) {
            this.gatewayInitiated = gatewayInitiated;
        }
    }
    
    /**
     * Custom exceptions
     */
    public static class InsufficientBalanceException extends RuntimeException {
        public InsufficientBalanceException(String message) {
            super(message);
        }
    }
    
    public static class PaymentProcessingException extends RuntimeException {
        public PaymentProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static class PaymentGatewayException extends RuntimeException {
        public PaymentGatewayException(String message) {
            super(message);
        }
    }
}
