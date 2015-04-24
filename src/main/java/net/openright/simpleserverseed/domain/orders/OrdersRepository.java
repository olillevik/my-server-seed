package net.openright.simpleserverseed.domain.orders;

import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

import net.openright.infrastructure.db.Database;
import net.openright.infrastructure.db.Database.Row;
import net.openright.simpleserverseed.domain.products.ProductRepository;

class OrdersRepository {

	private final Database database;

	OrdersRepository(Database database) {
		this.database = database;
	}

	List<Order> list() {
		return database.queryForList("select * from orders order by title", this::toOrder);
	}

	Order retrieve(int id) {
		return database
				.queryForSingle("select * from orders where id = ?", (row) -> toOrderWithOrderLines(id, row), id)
				.orElseThrow(() -> new NotFoundException("Can't find object with id " + id));
		
	}

	int insert(Order order) {
		validateOrder(order);
		return database.doInTransactionWithResult(() -> {
			int orderId = database.insert("insert into orders (title) values (?) returning id", order.getTitle());
			order.setId(orderId);
			insertOrderLines(order.getId(), order);
			return orderId;
		});
	}

	void update(int orderId, Order order) {
		validateOrder(order);

		database.doInTransaction(() -> {
			updateOrder(orderId, order);
			deleteOrderLines(orderId);
			insertOrderLines(orderId, order);
		});
	}

	private Order toOrder(Row row) throws SQLException {
		Order order = new Order(row.getString("title"));
		order.setId(row.getInt("id"));
		return order;
	}

	private OrderLine toOrderLine(Row row) throws SQLException {
		OrderLine orderLine = new OrderLine(row.getString("title"));
		orderLine.setProduct(ProductRepository.toProduct(row));
		orderLine.setAmount(row.getInt("amount"));
		return orderLine;
	}

	private Order toOrderWithOrderLines(int orderId, Row row) throws SQLException {
		Order order = toOrder(row);
		order.setOrderLines(queryForOrderLines(orderId));
		return order;
	}

	private List<OrderLine> queryForOrderLines(int orderId) {
		return database
				.queryForList(
						"select * from order_lines INNER JOIN products on products.id = order_lines.product_id where order_id = ?",
						this::toOrderLine, orderId);
	}

	private void deleteOrderLines(int orderId) {
		database.executeOperation("delete from order_lines where order_id = ?", orderId);
	}

	private void updateOrder(int orderId, Order order) {
		database.executeOperation("update orders set title = ? where id = ?", order.getTitle(), orderId);
	}

	private void insertOrderLines(int orderId, Order order) {
		for (OrderLine orderLine : order.getOrderLines()) {
			database.executeOperation(
					"insert into order_lines (amount, product_id, title, order_id) values (?, ?, ?, ?)",
					orderLine.getAmount(), orderLine.getProductId(), orderLine.getTitle(), orderId);
		}
	}

	private void validateOrder(Order order) {
		if (order.getTitle().equals("null")) {
			throw new RuntimeException("Null title is invalid");
		}
		if (order.getTitle().contains("foul")) {
			throw new BadRequestException("No foul language in orders, please!");
		}
	}
}
