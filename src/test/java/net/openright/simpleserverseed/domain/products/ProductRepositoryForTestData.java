package net.openright.simpleserverseed.domain.products;

import java.util.List;

import net.openright.infrastructure.db.Database;

/**
 * Helper class for accessing protected methods in ProductRepository. Used for setting up data in JUnit tests.
 */
public class ProductRepositoryForTestData {
	private ProductRepository productRepository;
	
	public ProductRepositoryForTestData(Database database) {
		productRepository = new ProductRepository(database);
	}

	public void insert(Product product) {
		productRepository.insert(product);
	}

	public List<Product> list() {
		return productRepository.list();
	}

	public Product retrieve(Long id) {
		return productRepository.retrieve(id);
	}
}
