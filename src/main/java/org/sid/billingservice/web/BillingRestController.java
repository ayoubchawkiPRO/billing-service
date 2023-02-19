package org.sid.billingservice.web;

import org.sid.billingservice.entities.Bill;
import org.sid.billingservice.entities.ProductItem;
import org.sid.billingservice.feign.CustomerRestClient;
import org.sid.billingservice.feign.ProductItemRestClient;
import org.sid.billingservice.models.Customer;
import org.sid.billingservice.models.Product;
import org.sid.billingservice.repository.BillRepository;
import org.sid.billingservice.repository.ProductItemRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin(origins = {"http://localhost:4200","http://localhost:8888"})
public class BillingRestController {
    private final BillRepository billRepository;
    private final ProductItemRestClient productItemRestClient;
    private final ProductItemRepository productItemRepository;

    public BillingRestController(BillRepository billRepository, ProductItemRestClient productItemRestClient, ProductItemRepository productItemRepository) {
        this.billRepository = billRepository;
        this.productItemRestClient = productItemRestClient;
        this.productItemRepository = productItemRepository;
    }

    @GetMapping(path = "/fullBill/{id}")
    public Bill getBill(@PathVariable(name = "id") Long id){
        Bill bill = billRepository.findById(id).orElseThrow();
        bill.getProductItems().forEach(productItem -> {
            Product product = productItemRestClient.getProductById(productItem.getProductID());
            //productItem.setProduct(product);
            productItem.setProductName(product.getName());
        });
        return bill;
    }

    @GetMapping(path = "/fullBills")
    public List<Bill> getBills(){
        List<Bill> bills = billRepository.findAll();
        bills.forEach((bill -> bill.getProductItems().forEach(productItem -> {
            Product product = productItemRestClient.getProductById(productItem.getProductID());
            productItem.setProductName(product.getName());
        })));
        return bills;
    }

    @DeleteMapping(path = "/fullBill/{id}")
    public void deleteBill(@PathVariable(name = "id") Long id){
        Bill bill = billRepository.findById(id).orElseThrow();
        productItemRepository.deleteAll(bill.getProductItems());
        billRepository.delete(bill);
    }

    @PutMapping(path = "/fullBill/{id}")
    public void updateBill(@PathVariable(name = "id") Long id, @RequestBody Bill newBill){
        Bill bill = billRepository.findById(id).orElseThrow();
        bill.setProductItems(newBill.getProductItems());
        if (bill.getProductItems().isEmpty()){
            billRepository.delete(bill);
        } else {
            billRepository.save(bill);
        }
    }

    @PostMapping(path = "/addBill")
    public void addProductItem(@RequestBody ProductItem newProductItem){
        Bill bill = billRepository.findById(newProductItem.getBill().getId()).orElseThrow();
        productItemRepository.save(newProductItem);
        bill.setTotalPrice(bill.getTotalPrice()+newProductItem.getPrice());
        bill.getProductItems().add(newProductItem);
        billRepository.save(bill);
    }
}