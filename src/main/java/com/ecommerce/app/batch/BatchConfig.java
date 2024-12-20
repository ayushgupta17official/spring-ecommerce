package com.ecommerce.app.batch;

import com.ecommerce.app.entity.Order;
import com.ecommerce.app.repository.OrderRepository;
import com.ecommerce.app.util.InvoiceGenerator;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Iterator;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final OrderRepository orderRepository;
    private final InvoiceGenerator invoiceGenerator;

    public BatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,
                       OrderRepository orderRepository, InvoiceGenerator invoiceGenerator) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.orderRepository = orderRepository;
        this.invoiceGenerator = invoiceGenerator;
    }

    @Bean
    public Step generateInvoiceStep() {
        return stepBuilderFactory.get("generateInvoiceStep")
                .<Order, Order>chunk(10)
                .reader(orderItemReader())
                .processor(orderItemProcessor())
                .writer(orderItemWriter())
                .build();
    }
    
    @Bean
    public ItemReader<Order> orderItemReader() {
        return new ItemReader<>() {
            private final Iterator<Order> orderIterator = orderRepository.findByIsInvoiceGeneratedFalse().iterator();

            @Override
            public Order read() {
                if (orderIterator.hasNext()) {
                    return orderIterator.next();
                }
                return null;
            }
        };
    }
    
    @Bean
    public ItemProcessor<Order, Order> orderItemProcessor() {
        return order -> {
            invoiceGenerator.generateInvoice(order);
            order.setIsInvoiceGenerated(true);
            return order;
        };
    }

    @Bean
    public ItemWriter<Order> orderItemWriter() {
        return orders -> orderRepository.saveAll(orders);
    }
    
    @Bean
    public Job invoiceJob() {
        return jobBuilderFactory.get("invoiceJob")
                .start(generateInvoiceStep())
                .build();
    }
}
