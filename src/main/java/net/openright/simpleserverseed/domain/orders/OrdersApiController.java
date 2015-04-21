package net.openright.simpleserverseed.domain.orders;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
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

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
public class OrdersApiController {

	private OrdersRepository repository;

	public OrdersApiController() {
		this.repository = new OrdersRepository(new Database("jdbc/seedappDs"));
	}

	@GET
	@Path("{id}")
	public Response get(@PathParam("id") Integer id) {
		return Response.ok().entity(toJson(repository.retrieve(id))).build();
	}

	@GET
	public Response list() {
		return Response.ok(toJsonList(repository.list())).build();
	}

	@POST
	public void post(String json) {
		repository.insert(toOrder(IOUtil.toJson(json)));
	}

	@POST
	@Path("{id}")
	public void put(@PathParam("id") String id, String json) {
		repository.update(Integer.parseInt(id), toOrder(IOUtil.toJson(json)));
	}

	private Order toOrder(JsonObject json) {
		Order order = new Order(json.getString("title"));
		JsonArray array = json.getJsonArray("orderlines");
		for (int i = 0; i < array.size(); i++) {
			JsonObject orderline = array.getJsonObject(i);
			if (orderline.getString("amount").isEmpty()) {
				continue;
			}
			long id = 0;
			if (orderline.containsKey("product")) {
				id = Long.parseLong(orderline.getString("product"));
			}
			int amount = Integer.parseInt(orderline.getString("amount"));
			order.addOrderLine(id, amount);
		}
		return order;
	}

	private JsonObject toJson(Order order) {
		JsonObjectBuilder obj = Json.createObjectBuilder().add("id", order.getId()).add("title", order.getTitle());
		JsonArrayBuilder array = Json.createArrayBuilder();
		for (OrderLine orderLine : order.getOrderLines()) {
			array.add(toJson(orderLine));
		}
		obj.add("orderlines", array);
		JsonObject json = obj.build();
		return json;
	}

	private JsonObject toJson(OrderLine orderLine) {
		JsonObjectBuilder obj = Json.createObjectBuilder().add("productId", orderLine.getProductId())
				.add("amount", orderLine.getAmount());
		return obj.build();
	}

	private JsonObject toJsonList(List<Order> list) {
		JsonArrayBuilder array = Json.createArrayBuilder();
		for (Order order : list) {
			array.add(toJson(order));
		}
		JsonObjectBuilder obj = Json.createObjectBuilder().add("orders", array);
		return obj.build();
	}
}
