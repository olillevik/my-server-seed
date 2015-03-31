package net.openright.simpleserverseed.domain.products;

import java.sql.SQLException;
import java.util.List;

import net.openright.infrastructure.db.PgsqlDatabase;
import net.openright.infrastructure.db.PgsqlDatabase.DatabaseTable;
import net.openright.infrastructure.db.PgsqlDatabase.Row;

public class ProductRepository {

	private DatabaseTable table;

	public ProductRepository(PgsqlDatabase database) {
		table = database.table("products");
	}

	public void insert(Product product) {
		long id = table.insertValues((row) -> {
			row.put("title", product.getTitle());
			row.put("active", product.isActive());
			row.put("price", product.getPrice());
		});
		product.setId(id);
	}

	public Product retrieve(long id) {
		return table.where("id", id).single(ProductRepository::toProduct);
	}

	public List<Product> list() {
		return table.where("active", true)
				.orderBy("title")
				.list(ProductRepository::toProduct);
	}

	public static Product toProduct(Row rs) throws SQLException {
		Product product = new Product();
		product.setId(rs.getLong("products", "id"));
		product.setTitle(rs.getString("products", "title"));
		product.setDescription(rs.getString("products", "description"));
		product.setActive(rs.getBoolean("products", "active"));
		product.setPrice(rs.getDouble("products", "price"));
		return product;
	}


}