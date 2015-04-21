package net.openright.simpleserverseed.domain.products;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.openright.infrastructure.db.Database;
import net.openright.infrastructure.util.IOUtil;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
public class ProductsApiController {

	private ProductRepository repository;

	public ProductsApiController() {
		this.repository = new ProductRepository(new Database("jdbc/seedappDs"));
	}

	@GET
	public Response listJSON() {
		return Response.ok(toJsonList(repository.list())).build();
	}

	@GET
	@Path("{id}")
	public Response getJSON(@PathParam("id") String id) {
		return Response.ok().entity(toJson(repository.retrieve(Long.valueOf(id)))).build();
	}

	@POST
	public void postJSON(String jsonString) {
		repository.insert(toProduct(IOUtil.toJson(jsonString)));
	}

	@POST
	@Path("{id}")
	public void putJSON(@PathParam("id") String id, String jsonString) {
		repository.update(Long.parseLong(id), toProduct(IOUtil.toJson(jsonString)));
	}

	private Product toProduct(JsonObject json) {
		Product product = new Product();
		product.setTitle(json.getString("title"));
		product.setPrice(Double.parseDouble(json.getString("price")));
		product.setDescription(json.getString("description"));
		return product;
	}

	private JsonObject toJson(Product product) {
		JsonObjectBuilder obj = Json.createObjectBuilder().add("id", product.getId()).add("title", product.getTitle())
				.add("price", product.getPrice()).add("description", product.getDescription());
		return obj.build();
	}

	private JsonObject toJsonList(List<Product> list) {
		JsonArrayBuilder array = Json.createArrayBuilder();
		for (Product order : list) {
			array.add(toJson(order));
		}
		JsonObjectBuilder obj = Json.createObjectBuilder().add("products", array);
		return obj.build();
	}
}
