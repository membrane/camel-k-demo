import org.apache.camel.Exchange.HTTP_PATH
import org.apache.camel.spi.Registry
import org.apache.commons.dbcp.BasicDataSource

val ds = BasicDataSource()
ds.setUrl("jdbc:postgresql://my-pg-postgresql.default.svc/postgres")
ds.setUsername("postgres")
ds.setPassword("abc")
ds.setDriverClassName("org.postgresql.Driver")

context.getRegistry(Registry::class.java).bind("ds", ds)

from("timer:kotlin?period=10000")
        .routeId("kotlin")
        .log("start run")
        .to("https://api.predic8.de/shop/orders/")
        .to("direct:process-order-page")
        .log("end run")

from("direct:process-order-page")
        .routeId("process-order-page")
        .convertBodyTo(String::class.java)
        .to("direct:process-orders")
        .removeHeader(HTTP_PATH)
        .setHeader(HTTP_PATH).jsonpath("$.meta.next_url", true)
        .log("next_url: \${header.CamelHttpPath}")
        .filter(header(HTTP_PATH).isNotNull())
            .setBody(constant(null))
            .to("https://api.predic8.de/")
            .to("direct:process-order-page")
        .end()

from("direct:process-orders")
        .split().jsonpath("$.orders")
            .removeHeaders(".*")
            .setHeader(HTTP_PATH).simple("\${body[order_url]}")
            .setBody(constant(null))
            .to("https://api.predic8.de/")
            .convertBodyTo(String::class.java)

            .removeHeaders(".*")
            .setHeader(HTTP_PATH).jsonpath("$.items_url")
            .setBody(constant(null))
            .to("https://api.predic8.de/")
            .convertBodyTo(String::class.java)

            .setHeader("orderId", jsonpath("$.order_url").regexReplaceAll("/shop/orders/", "").convertTo(Integer::class.java))
            .log("order: \${header.orderId}")

            .split().jsonpath("$.items")

                .setHeader("quantity", simple("\${body[quantity]}"))
                .setHeader("productId", simple("\${body[product_url].replaceAll('/shop/products/', '')}", Integer::class.java))

                //.to("log:info?showAll=true&multiline=true")

                .setBody(constant("INSERT INTO items (orderId, quantity, productId) VALUES (:?orderId, :?quantity, :?productId) ON CONFLICT DO NOTHING"))
                .to("jdbc:ds?useHeadersAsParameters=true")
