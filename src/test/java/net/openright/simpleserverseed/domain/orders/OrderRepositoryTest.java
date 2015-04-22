package net.openright.simpleserverseed.domain.orders;

import static org.assertj.core.api.Assertions.assertThat;
import net.openright.infrastructure.db.Database;
import net.openright.infrastructure.test.SampleData;
import net.openright.simpleserverseed.application.SeedAppConfig;
import net.openright.simpleserverseed.application.SimpleseedTestConfig;
import net.openright.simpleserverseed.domain.products.Product;
import net.openright.simpleserverseed.domain.products.ProductRepositoryForTestData;
import net.openright.simpleserverseed.domain.products.ProductRepositoryTest;

import org.junit.Before;
import org.junit.Test;

public class OrderRepositoryTest {

    private SeedAppConfig config = new SimpleseedTestConfig();
    private Database database = new Database(config.createDataSource());
    private OrdersRepository repository = new OrdersRepository(database);
    private ProductRepositoryForTestData productTestRepository = new ProductRepositoryForTestData(database);
    private Product product = ProductRepositoryTest.sampleProduct();

    @Before
    public void saveProduct() {
    	productTestRepository.insert(product);
    }

    @Test
    public void shouldRetrieveSavedOrdersWithoutOrderLines() throws Exception {
        Order order = sampleOrder();
        repository.insert(order);
        assertThat(repository.list()).contains(order);

        assertThat(repository.retrieve(order.getId()))
            .isEqualToComparingFieldByField(order);
    }

    @Test
    public void shouldListOrdersWithoutOrderLines() throws Exception {
        Order order = sampleOrder();
        order.addOrderLine(product.getId(), 2);
        repository.insert(order);

        Order savedOrder =  repository.list().stream()
                .filter(o -> o.getId() == order.getId())
                .findFirst().get();
        assertThat(savedOrder.getOrderLines()).isEmpty();
    }

    @Test
    public void shouldIncludeProductWhenRetrievingOrder() throws Exception {
        Order order = sampleOrder();
        order.addOrderLine(product.getId(), 2);
        repository.insert(order);

        Order savedOrder = repository.retrieve(order.getId());
        assertThat(savedOrder.getOrderLines().get(0).getProduct().get())
            .isEqualTo(product);
        assertThat(savedOrder.getOrderLines().get(0).getAmount())
            .isEqualTo(2);
    }

    @Test
    public void shouldUpdateOrder() throws Exception {
        Order order = sampleOrder();
        order.addOrderLine(product.getId(), 2);
        repository.insert(order);

        order.getOrderLines().get(0).setAmount(1000);
        order.setTitle(SampleData.sampleString(3));
        repository.update(order.getId(), order);

        assertThat(repository.retrieve(order.getId()).getTitle())
            .isEqualTo(order.getTitle());
        assertThat(repository.retrieve(order.getId()).getOrderLines().get(0).getAmount())
            .isEqualTo(order.getOrderLines().get(0).getAmount());
    }

    @Test
    public void shouldRetrieveSavedOrdersWithOrderLines() throws Exception {
        Product product2 = ProductRepositoryTest.sampleProduct();
        productTestRepository.insert(product2);

        Order order = sampleOrder();

        order.addOrderLine(product.getId(), 10);
        order.addOrderLine(product2.getId(), 100);

        repository.insert(order);

        Order savedOrder = repository.retrieve(order.getId());
        assertThat(savedOrder).isEqualToComparingFieldByField(order);
        assertThat(savedOrder.getTotalAmount())
            .isEqualTo(10 * product.getPrice() + 100 * product2.getPrice());
    }

    public static Order sampleOrder() {
        return new Order(SampleData.sampleString(4));
    }

}
