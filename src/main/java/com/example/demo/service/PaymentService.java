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
            
            // Step 2: Credit merchant account (after deducting wallet fee)
            Merchant merchant = product.getMerchant();
            BigDecimal merchantCreditAmount = totalAmount.subtract(walletFee);
            
            merchant.setWalletBalance(merchant.getWalletBalance().add(merchantCreditAmount));
            merchant.setUpdatedAt(LocalDateTime.now());
            merchantRepository.save(merchant);
            
            // Create credit transaction
            Transaction creditTransaction = createTransaction(order, Transaction.TransactionType.CREDIT, 
                merchantCreditAmount, order.getCurrency(), 
                "Credit to merchant " + merchant.getMerchantName() + " for order " + order.getOrderNumber());
            transactions.add(creditTransaction);
            
            auditService.logAudit("Merchant", merchant.getMerchantId(), AuditLog.AuditAction.MERCHANT_CREDITED, 
                "Credited " + merchantCreditAmount + " " + order.getCurrency() + " to merchant wallet after deducting wallet fee of " + walletFee);
            
            logger.info("Credited {} {} to merchant account (after {} fee)", merchantCreditAmount, order.getCurrency(), walletFee);
            
            // Step 3: Record wallet fee transaction
            Transaction feeTransaction = createTransaction(order, Transaction.TransactionType.WALLET_FEE, 
                walletFee, order.getCurrency(), "Wallet fee collected for order " + order.getOrderNumber());
            transactions.add(feeTransaction);
            
            auditService.logAudit("Order", order.getOrderId(), AuditLog.AuditAction.WALLET_FEE_COLLECTED, 
                "Wallet fee of " + walletFee + " " + order.getCurrency() + " collected");
            
            logger.info("Wallet fee of {} {} collected", walletFee, order.getCurrency());
            
            // Step 4: Send notification to merchant
            try {
                notificationService.sendMerchantNotification(order, merchant.getEmail(), 
                    "Payment of " + merchantCreditAmount + " " + order.getCurrency() + 
                    " received for order " + order.getOrderNumber());
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
                "Payment processed successfully for amount " + totalAmount + " " + order.getCurrency());
            
            result.setSuccess(true);
            result.setMessage("Payment processed successfully");
            result.setTransactions(transactions);
            result.setWalletFee(walletFee);
            result.setRemainingBalance(wallet.getBalance());
            
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
            
            throw new PaymentProcessingException("Payment processing failed: " + e.getMessage(), e);
        }
        
        return result;
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
}
