package org.sid.billingservice.kafka;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.sid.billingservice.entities.Bill;
import org.sid.billingservice.feign.CustomerRestClient;
import org.sid.billingservice.models.Customer;
import org.sid.billingservice.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class BillingService {

    @Autowired
    CustomerRestClient customerRestClient;

    @Autowired
    BillRepository billRepository;

    @Bean
    public Consumer<Bill> BillConsumer(){
        return (input) -> {
            System.out.println("***********");
            System.out.println(input.toString());
            System.out.println("***********");
        };
    }
    @Bean
    public Supplier<Bill> BillSupplier(){
        return () -> {
            Customer customer = customerRestClient.getCustomerById(1L);
            return  new Bill(null, new Date(), null, customer.getId());
        };
    }
    @Bean
    public Function<Bill,Bill> BillFunction(){
        return (input) -> {
            input.setBillingDate(new Date());
            input.setCustomerID((long) new Random().nextInt(9000));
            return input;
        };
    }
    @Bean
    public Function<KStream<String , Bill>,KStream<String,Long>> kStreamFunction(){
        return (input) -> {
            return input
                    .map((k,v)->new KeyValue<>(v.getBillingDate(),0L))
                    .groupBy((k,v)->k)
                    .windowedBy(TimeWindows.of(Duration.ofMillis(5000)))
                    .count(Materialized.as("page-count"))
                    .toStream()
                    .map((k,v)->new KeyValue<>("=>"+k.window().startTime()+k.window().endTime()+k.key(),v));
        };
    }
}